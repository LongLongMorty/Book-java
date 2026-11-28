package com.bit.dbs.service.impl;

import com.bit.dbs.exception.BasicException;
import com.bit.dbs.service.BackupService;
import com.bit.dbs.vo.BackupFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 应用内数据库备份/恢复。
 *
 * 说明：
 * - 备份文件落在本地目录，不新增数据库表；
 * - 使用 mysqldump / mysql 客户端，连接信息从当前数据源读取；
 * - 恢复前自动生成一份 before-restore 安全备份。
 */
@Slf4j
@Service
public class BackupServiceImpl implements BackupService {

    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]*\\.sql$");
    private static final Pattern JDBC_URL = Pattern.compile("jdbc:mysql://([^/:?]+)(?::(\\d+))?/([^?]+)");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    @Value("${book.backup.dir:./backup}")
    private String backupDir;

    @Value("${book.backup.mysqldump:mysqldump}")
    private String mysqldumpCommand;

    @Value("${book.backup.mysql:mysql}")
    private String mysqlCommand;

    /**
     * 可选的恢复专用账号。MySQL 开启 binlog 时，恢复包含触发器的备份需要 SUPER 权限，
     * 普通业务账号通常没有，因此允许单独配置只用于恢复的账号。
     */
    @Value("${book.backup.restore-username:}")
    private String restoreUsername;

    @Value("${book.backup.restore-password:}")
    private String restorePassword;

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<BackupFileInfo> list() {
        Path root = backupRoot();
        try (Stream<Path> stream = Files.list(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".sql"))
                    .sorted(Comparator.comparingLong((Path path) -> path.toFile().lastModified()).reversed())
                    .map(this::toInfo)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("读取备份目录失败", e);
            throw new BasicException(500, "读取备份目录失败");
        }
    }

    @Override
    public BackupFileInfo create() {
        return dump("backup");
    }

    @Override
    public void delete(String name) {
        Path file = resolveBackup(name);
        try {
            Files.delete(file);
        } catch (IOException e) {
            log.error("删除备份失败: {}", name, e);
            throw new BasicException(500, "删除备份失败");
        }
    }

    @Override
    public void restore(String name) {
        Path file = resolveBackup(name);
        boolean useRestoreAccount = StringUtils.hasText(restoreUsername);
        preflightRestore(file, useRestoreAccount);

        // 恢复前先做一份安全备份，避免恢复错误后无法回退。
        BackupFileInfo safety = dump("before-restore");
        log.info("恢复前安全备份: {}", safety.getName());

        Path optionFile = null;
        try {
            String restoreUser = useRestoreAccount ? restoreUsername : dataSourceProperties.getUsername();
            String restorePwd = useRestoreAccount ? restorePassword : dataSourceProperties.getPassword();
            optionFile = createClientOptionFile(restoreUser, restorePwd);
            List<String> command = new ArrayList<>();
            command.add(mysqlCommand);
            command.add("--defaults-extra-file=" + optionFile.toAbsolutePath());
            command.add("--default-character-set=utf8mb4");
            command.add(databaseName());

            CommandResult result = run(command, file);
            if (result.exitCode != 0) {
                log.error("恢复数据库失败: {}", result.trimmedOutput());
                throw new BasicException(500, "恢复失败：" + result.trimmedOutput());
            }
            log.info("数据库恢复完成，来源备份: {}", name);
        } catch (IOException e) {
            log.error("准备恢复环境失败", e);
            throw new BasicException(500, "恢复失败：无法准备数据库客户端");
        } finally {
            deleteQuietly(optionFile);
        }
    }

    /**
     * 恢复前检查：binlog 开启且备份包含触发器/函数时，普通业务账号没有 SUPER 权限，
     * 会导致恢复中途失败。这里提前拒绝，避免数据库被恢复一半。
     */
    private void preflightRestore(Path file, boolean useRestoreAccount) {
        if (useRestoreAccount) {
            return;
        }
        try {
            String sql = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            boolean containsProgram = sql.contains("TRIGGER") || sql.contains("FUNCTION") || sql.contains("PROCEDURE");
            if (!containsProgram) {
                return;
            }
            Integer logBin = jdbcTemplate.queryForObject("SELECT @@log_bin", Integer.class);
            Integer trustFunctionCreators = jdbcTemplate.queryForObject(
                    "SELECT @@log_bin_trust_function_creators", Integer.class);
            if (Integer.valueOf(1).equals(logBin) && !Integer.valueOf(1).equals(trustFunctionCreators)) {
                throw new BasicException(400,
                        "恢复包含触发器的备份需要 SUPER 权限：请在 application-local.yml 配置 "
                                + "book.backup.restore-username / restore-password，"
                                + "或让 DBA 开启 log_bin_trust_function_creators");
            }
        } catch (IOException e) {
            throw new BasicException(500, "读取备份文件失败");
        }
    }

    @Override
    public File getFile(String name) {
        return resolveBackup(name).toFile();
    }

    private BackupFileInfo dump(String prefix) {
        Path root = backupRoot();
        String name = prefix + "-" + LocalDateTime.now().format(FILE_TIME_FORMAT) + ".sql";
        Path target = root.resolve(name);
        Path optionFile = null;
        try {
            optionFile = createClientOptionFile();
            List<String> command = new ArrayList<>();
            command.add(mysqldumpCommand);
            command.add("--defaults-extra-file=" + optionFile.toAbsolutePath());
            command.add("--no-tablespaces");
            command.add("--single-transaction");
            command.add("--routines");
            command.add("--triggers");
            command.add("--set-gtid-purged=OFF");
            command.add("--default-character-set=utf8mb4");
            command.add("--result-file=" + target.toAbsolutePath());
            command.add(databaseName());

            CommandResult result = run(command, null);
            if (result.exitCode != 0 || !Files.exists(target) || Files.size(target) == 0) {
                Files.deleteIfExists(target);
                log.error("备份数据库失败: {}", result.trimmedOutput());
                throw new BasicException(500, "备份失败：" + result.trimmedOutput());
            }
            return toInfo(target);
        } catch (IOException e) {
            log.error("创建备份失败", e);
            deleteQuietly(target);
            throw new BasicException(500, "备份失败：无法写入备份文件");
        } finally {
            deleteQuietly(optionFile);
        }
    }

    private Path createClientOptionFile() throws IOException {
        return createClientOptionFile(dataSourceProperties.getUsername(), dataSourceProperties.getPassword());
    }

    private Path createClientOptionFile(String user, String password) throws IOException {
        JdbcInfo info = parseJdbcUrl(dataSourceProperties.getUrl());
        String config = "[client]\n"
                + "host=" + info.host + "\n"
                + "port=" + info.port + "\n"
                + "user=" + quote(user) + "\n"
                + "password=" + quote(password) + "\n"
                + "default-character-set=utf8mb4\n";

        Path file = Files.createTempFile("book-backup-", ".cnf");
        Files.write(file, config.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
        return file;
    }

    private JdbcInfo parseJdbcUrl(String url) {
        Matcher matcher = JDBC_URL.matcher(url == null ? "" : url);
        if (!matcher.find()) {
            throw new BasicException(500, "无法解析数据库连接地址");
        }
        String host = matcher.group(1);
        int port = matcher.group(2) == null ? 3306 : Integer.parseInt(matcher.group(2));
        String database = matcher.group(3);
        return new JdbcInfo(host, port, database);
    }

    private String databaseName() {
        return parseJdbcUrl(dataSourceProperties.getUrl()).database;
    }

    private String quote(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private CommandResult run(List<String> command, Path input) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        if (input != null) {
            builder.redirectInput(input.toFile());
        }

        Process process = null;
        try {
            process = builder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (output.length() < 4000) {
                        output.append(line).append('\n');
                    }
                }
            }
            boolean finished = process.waitFor(180, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BasicException(500, "数据库操作超时");
            }
            return new CommandResult(process.exitValue(), output.toString());
        } catch (IOException e) {
            log.error("执行数据库客户端失败", e);
            throw new BasicException(500, "执行数据库客户端失败，请确认 mysqldump/mysql 已安装");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            throw new BasicException(500, "数据库操作被中断");
        }
    }

    private Path backupRoot() {
        Path root = Paths.get(backupDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
            return root;
        } catch (IOException e) {
            log.error("创建备份目录失败: {}", root, e);
            throw new BasicException(500, "无法创建备份目录");
        }
    }

    private Path resolveBackup(String name) {
        if (name == null || !SAFE_NAME.matcher(name).matches()) {
            throw new BasicException(400, "备份文件不合法");
        }
        Path root = backupRoot();
        Path file = root.resolve(name).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            throw new BasicException(400, "备份文件不存在");
        }
        return file;
    }

    private BackupFileInfo toInfo(Path path) {
        long size = 0L;
        try {
            size = Files.size(path);
        } catch (IOException ignored) {
            // 文件刚被删除时保持 0，不阻断列表
        }
        String createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(path.toFile().lastModified()), ZoneId.systemDefault()).format(TIME_FORMAT);
        return new BackupFileInfo(path.getFileName().toString(), size, createdAt);
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 临时文件清理失败不影响主流程
        }
    }

    private static class JdbcInfo {
        private final String host;
        private final int port;
        private final String database;

        private JdbcInfo(String host, int port, String database) {
            this.host = host;
            this.port = port;
            this.database = database;
        }
    }

    private static class CommandResult {
        private final int exitCode;
        private final String output;

        private CommandResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        private String trimmedOutput() {
            if (output == null || output.trim().isEmpty()) {
                return "未知错误";
            }
            String value = output.trim().replaceAll("\\s+", " ");
            return value.length() > 300 ? value.substring(0, 300) : value;
        }
    }
}
