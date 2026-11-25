# 数据可视化大屏实施计划

> 目标：新增一个面向演示的数据大屏。
> 硬约束：**0 数据表改动**——不建表、不改字段、不写数据，只对现有表做只读聚合查询。
> 现有数据：24 本书、10 条在借记录、8 个用户、20 条操作日志。

## 1. 大屏内容

### 1.1 顶部 KPI

| 指标 | 来源 |
| --- | --- |
| 馆藏总量 | `COUNT(*) FROM book` |
| 馆藏总库存 | `SUM(stock) FROM book` |
| 在借册数 | `COUNT(*) FROM borrow_info` |
| 注册用户 | `COUNT(*) FROM u_user` |
| 今日操作 | `COUNT(*) FROM user_log WHERE action_time >= CURDATE()` |

### 1.2 图表

1. **近 14 天借阅趋势**（折线/面积图）
   - `borrow_info.borrow_time` 为毫秒时间戳，用 `DATE(FROM_UNIXTIME(borrow_time/1000))` 聚合。
   - 缺失日期在 Java 侧补 0，保证横轴连续。
2. **借阅排行 Top 8**（横向柱状图）
   - `GROUP BY book_id, book_name ORDER BY COUNT(*) DESC`。
3. **库存 Top 8**（横向柱状图）
   - `book.stock` 降序；颜色区分库存紧张（<=1 朱砂）与充足（黛青）。
4. **角色分布**（环形图）
   - `user_role JOIN r_role`，统计管理员/读者人数。
5. **操作类型分布**（环形图或柱状图）
   - `user_log.action_type`：新增/修改/删除/上传/登录/退出。
6. **最近动态**（滚动列表）
   - 最近 8 条 `user_log`：时间、操作人、描述。

### 1.3 交互与氛围

- 全屏深墨底、宣纸色文字、朱砂与藤黄点缀，延续新中式风格。
- 顶部显示当前时间，每 60 秒自动刷新数据。
- 左上角「返回系统」，右上角「刷新」。
- 数字使用宋体大字，图表网格线极淡，避免霓虹大屏的廉价感。

## 2. 后端设计（只读）

### 2.1 新增文件

| 文件 | 作用 |
| --- | --- |
| `controller/DashboardController.java` | `GET /dashboard/overview` |
| `service/DashboardService.java` + `impl/DashboardServiceImpl.java` | 聚合与补零 |
| `mapper/DashboardMapper.java` | 只读查询 |
| `src/main/resources/mapper/DashboardMapper.xml` | 聚合 SQL |
| `vo/DashboardData.java` | 大屏返回结构 |

### 2.2 返回结构

```json
{
  "kpi": { "bookTotal": 24, "stockTotal": 63, "borrowTotal": 10, "userTotal": 8, "todayActions": 6 },
  "borrowTrend": [ { "date": "11-12", "count": 1 } ],
  "topBooks": [ { "name": "百年孤独", "value": 2 } ],
  "stockTop": [ { "name": "三体", "value": 5 } ],
  "roleDistribution": [ { "name": "读者", "value": 7 } ],
  "actionDistribution": [ { "name": "登录", "value": 5 } ],
  "recentLogs": [ { "time": "2026-11-25 20:51:24", "userName": "admin", "description": "修改书籍：《人类简史》" } ]
}
```

- 权限：`UserUtils.checkPrivilege(Privilege.PRI_READ, ...)`。
- 只执行 `SELECT`，不写 `user_log`，不产生任何数据变更。

## 3. 前端设计

### 3.1 依赖

- `npm install echarts`（仅前端依赖，不涉及数据库）。
- 使用 ECharts 5 直接按需引入折线、柱状、环形图。

### 3.2 新增页面

`src/views/Dashboard.vue`，路由 `/dashboard`，独立全屏页面（不套 Home 侧栏）。

布局：

```
┌──────────────────────────────────────────────────────┐
│ 数据大屏 · 数字化书籍服务系统        时间   返回 刷新 │
├────────────┬────────────┬────────────┬───────────────┤
│ 馆藏总量   │ 在借册数   │ 注册用户   │ 今日操作       │
├────────────┴────────────┼────────────┴───────────────┤
│ 近 14 天借阅趋势（大面积）│ 角色分布 / 操作类型（环形） │
├────────────┬────────────┴────────────┬───────────────┤
│ 借阅排行   │ 库存 Top                 │ 最近动态       │
└────────────┴──────────────────────────┴───────────────┘
```

- 登录校验：进入时调用 `$root.checkLogin()`，未登录跳 `/login`。
- ECharts 实例在 `mounted` 初始化，`beforeDestroy` 销毁，窗口 resize 时重绘。
- 数据为空时显示「暂无数据」，不报错。

### 3.3 入口

- `Home.vue` 顶栏「更多」旁增加「数据大屏」链接。
- 路由 `src/router/index.js` 增加 `/dashboard`。

## 4. 实施阶段与验证

### 阶段 1：后端

- 新增 Mapper/Service/Controller/VO。
- 构建：`mvn "-Dmaven.compiler.source=1.8" "-Dmaven.compiler.target=1.8" clean package -DskipTests`。
- 验证：登录后请求 `/dashboard/overview`，核对：
  - `bookTotal=24`、`borrowTotal=10`、`userTotal=8`
  - `topBooks`、`stockTop` 数量正确
  - `borrowTrend` 有 14 个点

### 阶段 2：前端

- 安装 echarts，新增 Dashboard 页面与路由、入口。
- `npm run build` 通过。
- 浏览器验证：
  1. 登录后从顶栏进入数据大屏。
  2. KPI、折线、柱状、环形、动态列表全部渲染。
  3. 刷新后数据一致；窗口缩放图表自适应。
  4. 控制台无新增错误。

### 阶段 3：回归

- 登录、书籍列表/搜索、详情、借阅、用户、日志页面不受影响。
- 数据库表结构与数据量保持不变（执行前后对比 `SHOW TABLES` 与行数）。

## 5. 风险与约束

- **时间戳**：`borrow_time/deadline` 是毫秒 bigint，聚合时除以 1000。
- **时区**：MySQL 与 JVM 均为 `Asia/Shanghai`，趋势日期按本地日期。
- **无数据日期**：Java 侧补齐 14 天，避免折线断裂。
- **只读**：不新增表、不写库；`user_log` 不记录大屏访问，避免演示时数据被污染。
- **ECharts 体积**：按需引入，避免整包引入导致构建体积过大。

## 6. 预计改动清单

后端：新增 6 个文件（1 Controller、1 Service + Impl、1 Mapper、1 XML、1 VO）。
前端：新增 1 个页面，修改路由、Home 顶栏入口，新增 echarts 依赖。
数据库：**0 改动**。

> 确认后按「后端 → 前端 → 回归」实施。
