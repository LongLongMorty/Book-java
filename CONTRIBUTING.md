# 协作指南

本文档面向参与本仓库开发的成员。提交信息规范见 [COMMIT_CONVENTION.md](COMMIT_CONVENTION.md)。

## 1. 安全约定

- **禁止提交任何明文凭证**：数据库账号密码、GitHub Token、服务器密钥等一律通过环境变量或本地私有配置提供。
- 数据源使用环境变量（两个 profile 通用）：

  | 变量 | 说明 |
  | --- | --- |
  | `BOOK_DB_URL` | JDBC 连接串 |
  | `BOOK_DB_USERNAME` | 数据库用户名 |
  | `BOOK_DB_PASSWORD` | 数据库密码 |

  未设置时默认连接 `localhost`，不会读取仓库内的任何固定账号。

- 也可以在 `book-java/src/main/resources/application-local.yml` 中写本地配置，该文件已被 `.gitignore` 忽略：

  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://<host>:<port>/book?currentSchema=book
      username: <user>
      password: <password>
  ```

  启动时覆盖 profile：设置环境变量 `SPRING_PROFILES_ACTIVE=local`（IDEA 中在 Run Configuration 的 Environment variables 里配置）。

- 如果发现凭证曾被提交：先改密码，再评估是否需要清理 Git 历史；历史中的旧密码不会因为删除文件而消失。

## 2. 环境准备

1. 安装 JDK 8、Maven 3.6+、Node.js（原项目基于 Node 14 / vue-cli 4.5）。
2. 初始化数据库：
   - GaussDB / PostgreSQL：先执行 `book-java/book-gauss.sql`，再执行 `book-java/database.sql`（函数与触发器不能漏）。
   - MySQL：`book-java/book.sql` 为历史导出，表名与实体不一致，使用前需自行调整。
3. 后端：`cd book-java && mvn clean package`，产物为 `target/book.war`，端口 `8088`。
4. 前端：`cd book-vue && npm install && npm run serve`，端口 `8081`；后端地址在 `book-vue/public/api.config.json`。

## 3. 分支与 PR

`main` 是稳定分支，应开启分支保护（Require a pull request before merging，至少 1 个 approval），不要直接推送。

分支命名：

- `feat/<功能>`：新功能，如 `feat/book-search`
- `fix/<问题>`：缺陷修复，如 `fix/borrow-stock`
- `docs/<主题>`：文档
- `chore/<主题>`：构建、配置、依赖

流程：

```bash
# 从最新 main 开始
git switch main
git pull --rebase origin main
git switch -c feat/book-search

# 开发、自测、提交
git add <files>
git commit -F commit-msg.txt

# 推送前先同步，保持线性历史
git fetch origin
git rebase origin/main
git push -u origin feat/book-search
```

然后在 GitHub 发起 Pull Request，标题按提交规范书写，合并方式建议 **Squash and merge**。PR 描述使用仓库自带的模板，写清变更内容、关联 Issue 与自测结果。

## 4. 提交规范

- 遵循 Conventional Commits：`<type>(<scope>): <中文描述>`。
- 每条提交末尾必须带一行 `Co-Authored-By`，AI 参与时写实际模型名称。
- 一次提交只做一件事，避免把格式化、重构和功能混在一起。
- 详细规则与示例见 [COMMIT_CONVENTION.md](COMMIT_CONVENTION.md)。

## 5. 代码约定

**后端**

- 分层：`controller` → `service`/`impl` → `mapper`，实体放在 `entity`，请求/响应对象放在 `vo`。
- 接口统一返回 `ResultBody`，业务失败抛 `BasicException`，由 `GlobalExceptionHandler` 兜底。
- 需要权限的接口调用 `UserUtils.checkPrivilege(...)`；关键操作补 `UserUtils.log(...)`。
- 分页统一使用 `PageRspData`，不要手写 `map` 返回结构。
- 修改表结构或业务 SQL 时，同步更新 `book-gauss.sql` / `database.sql`，并在 PR 里说明。

**前端**

- 请求统一使用 `this.$http`，不要另起 axios 实例。
- 登录态与权限读取 `$root.$data.userStatus`、`$root.$data.canEdit`；按钮级权限用 `v-if` 控制，但不要把它当安全边界。
- 表格、分页、对话框沿用现有 Element UI 写法，保持页面风格一致。
- 新增接口地址时，先确认后端路径，再修改 `public/api.config.json` 的 `BASE_URL`（仅本地调整时不要提交个人地址）。

## 6. 提交前自测清单

- [ ] 后端能启动，前端 `npm run serve` 能打开
- [ ] 登录 / 退出正常（`admin`、`bob`）
- [ ] 书籍列表分页、按书名/作者搜索、新增、编辑、删除
- [ ] 图书详情新增/编辑、封面上传与预览
- [ ] 借阅、归还，库存变化正确
- [ ] 用户列表、新增用户、删除用户
- [ ] 日志分页、删除、导出 Excel
- [ ] `git diff --cached` 中没有密码、Token、个人路径等敏感信息

## 7. 数据库协作

- 推荐每人使用本地数据库，避免互相覆盖数据。
- 如果共用同一个开发库：表结构变更先提交脚本并通知其他人，禁止同时修改同一张表；测试数据用完即清。
