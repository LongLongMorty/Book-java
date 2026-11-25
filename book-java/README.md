# 数字化书籍服务系统后端

Spring Boot 2.3.4 + MyBatis-Plus 的书籍服务后端，默认使用 MySQL 8，保留 GaussDB profile。

## 运行环境

1. JDK 1.8
2. Maven 3.6+
3. MySQL 8.0

## 初始化数据库

```bash
mysql -uroot -p < book-mysql.sql
```

数据库连接通过环境变量或 `src/main/resources/application-local.yml`（已被 gitignore）配置：

```bash
BOOK_DB_URL=jdbc:mysql://localhost:3306/digital_book?useSSL=FALSE&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
BOOK_DB_USERNAME=root
BOOK_DB_PASSWORD=你的密码
```

## 启动

```bash
mvn clean package
java -jar target/digital-book-service.war
```

或在 IDE 中运行 `com.bit.dbs.BookApplication`，端口 8088。
