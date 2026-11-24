# 图书管理系统

基于 Spring Boot + Vue 2 的前后端分离图书管理系统，包含图书信息、图书详情与封面、借阅归还、用户角色、操作日志等模块。

## 技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Java 8、Spring Boot 2.3.4、MyBatis-Plus 3.4.1、Spring Session JDBC、Hutool、Lombok，打包为 war |
| 数据库 | GaussDB（默认，PostgreSQL 协议）/ MySQL（可选 profile） |
| 前端 | Vue 2.6、Vue Router 3、Element UI、axios、moment、xlsx、v-viewer |

## 目录结构

```
.
├─ book-java/                 # 后端 Spring Boot 工程
│  ├─ src/main/java/book/     # controller / service / mapper / entity / vo / utils
│  ├─ src/main/resources/     # application*.yml、MyBatis XML
│  ├─ book-gauss.sql          # 高斯建表与初始数据
│  ├─ database.sql            # 高斯函数与触发器
│  └─ book.sql                # 历史 MySQL 导出
├─ book-vue/                  # 前端 Vue CLI 工程
│  ├─ public/api.config.json  # 后端地址配置
│  └─ src/                    # components / views / router / utils
└─ COMMIT_CONVENTION.md       # Git 提交规范
```

## 功能概览

- 登录、退出、修改密码（Session 会话 + BCrypt 密码）
- 图书：分页、按书名/作者模糊搜索、新增、修改、删除、CSV 批量导入
- 图书详情：出版社、发行年份、简介、封面上传与预览
- 借阅：借书、还书、库存校验、重复借阅校验、30 天借期
- 用户：用户列表、注册、删除、角色绑定
- 日志：登录/操作日志、分页、单条删除、Excel 导出
- 权限：查看数据、增删改数据两级权限，按角色分配

## 快速开始

### 1. 准备数据库

后端默认启用 `gauss` profile（见 `book-java/src/main/resources/application.yml`）。数据库连接信息通过环境变量提供，仓库内不保存账号密码：

| 变量 | 说明 |
| --- | --- |
| `BOOK_DB_URL` | JDBC 连接串 |
| `BOOK_DB_USERNAME` | 数据库用户名 |
| `BOOK_DB_PASSWORD` | 数据库密码 |

也可以在 `book-java/src/main/resources/application-local.yml` 中写本地配置（已被 `.gitignore` 忽略），并通过 `SPRING_PROFILES_ACTIVE=local` 启用。

- GaussDB / PostgreSQL：依次执行 `book-java/book-gauss.sql`（建表与初始数据）和 `book-java/database.sql`（分页查询、库存判断、级联删除等函数与触发器）。
- MySQL：数据源见 `application-mysql.yml`。注意 `book.sql` 为历史导出，表名与当前实体（`u_user`、`r_role`）不一致，切换前需先调整脚本，并把 `spring.profiles.active` 改为 `mysql`。

### 2. 启动后端

```bash
cd book-java
mvn clean package          # 生成 target/book.war
```

项目按外置 Tomcat 方式配置（`spring-boot-starter-tomcat` 为 provided），可将 war 部署到 Tomcat，或在 IDE 中直接运行 `book.BookApplication`。服务端口为 **8088**。

### 3. 启动前端

```bash
cd book-vue
npm install
npm run serve              # 开发端口 8081
npm run build              # 生产构建，输出 dist/
```

前端通过 `book-vue/public/api.config.json` 读取后端地址，默认：

```json
{ "BASE_URL": "http://localhost:8088" }
```

原项目基于 Node 14 / vue-cli 4.5，若使用更高版本 Node 构建异常，可切换到 Node 14 环境。

## 默认账号与权限

- 初始用户：`admin`（管理员）、`bob`（普通用户），密码以 BCrypt 哈希存于 `u_user.password`。
- 登录页预填 `admin / 123456`，实际密码以数据库中哈希对应的明文为准。
- 权限码：`1` 查看数据、`2` 增删改数据；管理员拥有全部权限，普通用户仅可查看和借阅。

## 多人协作

- 加入仓库：由仓库管理员在 GitHub `Settings → Collaborators` 中邀请。
- 分支命名、PR 流程、提交规范与自测清单见 [CONTRIBUTING.md](CONTRIBUTING.md)。
- `main` 分支建议开启保护，通过 Pull Request 合并，至少 1 人 review。
- 数据库连接使用环境变量或本地 `application-local.yml`，**不要提交任何账号密码**。
- 表结构或函数有变更时，同步更新 `book-gauss.sql` / `database.sql` 并在 PR 中说明。

## 相关文档

- `COMMIT_CONVENTION.md`：Git 提交规范（Conventional Commits + Co-Authored-By）
- `book-java/README.md`、`book-vue/README.md`：各子项目原有说明
- 仓库根目录下的项目文档、用户手册与答辩 PPT（当前未纳入版本控制）
