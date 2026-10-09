# XG-WMS 仓库管理系统

XG-WMS 是一套面向制造企业的仓库管理系统。系统以生产标签为实物流转载体，连接用友 U8 ERP 的订单与库存单据，覆盖生产订单同步、标签打印、入库、移库、备料、调拨、发货和库存查询等业务。

项目采用 Spring Boot 3、Java 17 与 Kotlin 的 Maven 多模块架构，服务端同时支持 PC 管理端和 PDA 作业端。

## 核心能力

- 用户、角色、菜单及 PDA 功能权限管理
- JWT 无状态认证、请求链路标识和操作日志审计
- 从 U8 同步销售订单、采购订单、生产订单、发货单和调拨单
- 生产标签批量生成、补打、拆箱、组托和拆托
- 采购入库、产成品入库、退货入库及实时库存维护
- 普通移库、备料移库、调拨移库和销售发货出库
- 库位库存汇总及库存标签追踪
- Excel 标签模板管理、Code 128 条码生成和 PDF 批量输出
- 业务单号的数据库乐观锁并发生成
- 条码级进程内互斥，防止重复入库、出库和移库

## 技术栈

| 类别 | 技术 |
| --- | --- |
| 应用框架 | Spring Boot 3.5.14、Spring MVC、Spring AOP |
| 开发语言 | Java 17、Kotlin 2.2.0 |
| 数据访问 | MyBatis-Plus 3.5.15、MyBatis-Plus-Join 1.5.7 |
| 数据库 | Microsoft SQL Server、mssql-jdbc 13.2.1 |
| 认证与密码 | JJWT 0.12.6、Argon2 |
| 对象映射 | MapStruct 1.5.5.Final |
| ERP 集成 | RestTemplate、Gson 2.13.2 |
| 报表打印 | EasyPOI 4.5.0、Aspose Cells 8.5.2、ZXing 3.5.3 |
| 构建工具 | Maven Wrapper、Maven 多模块 |

## 模块说明

```text
xg
├── xg-app        # 应用入口、运行配置、Web 与 MyBatis 全局配置
├── xg-shared     # 通用响应、异常、用户上下文、基础类和公共工具
├── xg-system     # 用户、角色、权限、JWT、操作日志和业务序列号
├── xg-basic      # 库位档案、打印模板和打印日志
├── xg-u8         # 用友 U8 HTTP API 适配与单据模型转换
├── xg-order      # 生产、备料、发货、调拨订单及 U8 同步任务
├── xg-print      # Excel 模板填充、条码生成和 PDF 渲染
└── xg-warehouse  # 标签、托盘、入库、出库、移库和实时库存
```

| 模块 | 主要职责 |
| --- | --- |
| `xg-app` | 启动 Spring Boot，聚合业务模块，配置 CORS、认证拦截、分页插件、异常处理和 Thin JAR 打包 |
| `xg-shared` | 提供 `ResponseResult`、`WebException`、`UserContext`、`BaseController`、`BaseService`、`BarcodeLockUtil` 等公共能力 |
| `xg-system` | 实现用户与 RBAC 权限管理、JWT 签发与解析、请求追踪、操作审计和业务单号生成 |
| `xg-basic` | 管理库位档案、标签模板文件及打印记录 |
| `xg-u8` | 封装 U8 REST 接口，负责订单查询、入库单、出库单和货位调整单的数据转换 |
| `xg-order` | 将 U8 订单转换为 WMS 作业指令，维护备料、发货和调拨状态，并提供 PDA 待办汇总 |
| `xg-print` | 使用 Excel 模板填充业务数据，生成 Code 128 条码，并将单张或批量标签转换为 PDF |
| `xg-warehouse` | 执行标签、托盘、入库、出库、移库、备料、调拨、发货及库存查询业务 |

主要依赖方向如下：

```text
xg-app
  ├── xg-system ──┐
  ├── xg-basic  ──┤
  ├── xg-order  ──┼──> xg-shared
  └── xg-warehouse┤
                  │
xg-order ───────> xg-u8
xg-warehouse ───> xg-order / xg-basic / xg-system / xg-u8 / xg-print
```

## 架构约定

业务模块通常采用以下分层：

```text
Controller
    │  HTTP 参数接收、校验和响应封装
    ▼
Service
    │  业务编排、事务和跨模块协作
    ▼
PlusService
    │  MyBatis-Plus 查询、状态校验和数据访问封装
    ▼
Mapper / Mapper XML
    │
    ▼
SQL Server
```

- `Entity` 对应数据库表，`View` 对应数据库视图。
- `DTO`、`Request`、`Search` 与持久化实体分离。
- Entity 与 DTO 主要通过 MapStruct 转换。
- 主从表分页通常采用“主表分页、明细批量查询、内存分组装配”，避免 N+1 查询和联表分页计数失真。
- 复杂仓储流程使用 `TransactionTemplate` 或 `PlatformTransactionManager` 控制事务边界。
- 多条码操作通过 `BarcodeLockUtil` 排序加锁，避免同一进程内的重复作业和循环等待。

## 业务流程

### U8 订单同步

```text
用友 U8
  │  HTTP JSON API
  ▼
xg-u8
  │  单据查询与模型转换
  ▼
xg-order
  ├── ProdOrder
  ├── ShipOrder / ShipOrderDetail
  └── TransferOrder / TransferOrderDetail
  │
  ▼
PDA 与仓储作业
```

系统从 U8 拉取销售订单、采购订单、生产订单、发货单和调拨单。仓储作业完成后，再由 `xg-warehouse` 通过 `xg-u8` 回写采购入库、产成品入库、销售出库或货位调整单据。

### 标签打印

```text
生产订单
  ▼
生成纸箱标签与打印记录
  ▼
EasyPOI 填充 Excel 模板
  ▼
ZXing 生成 Code 128 条码
  ▼
Aspose Cells 替换模板图片并合并工作表
  ▼
返回 application/pdf
```

模板由 `xg-basic` 管理，标签数据由 `xg-warehouse` 生成，PDF 渲染由 `xg-print` 完成。批量打印会合并为一个多页 PDF。

### 仓储流转

```text
生产标签
  ▼
采购/产成品入库 ──> StockInventory 实时库存
  ▼
组托、拆托、拆箱、普通移库
  ▼
备料移库或调拨移库
  ▼
销售发货出库 ──> 库存核销与 U8 单据回写
```

库存以标签为追踪单位，记录当前库位、数量和 U8 批次。已入库或已组托标签受到引用保护，不能直接删除。

## 本地开发

### 环境要求

- JDK 17
- SQL Server 2016 或更高版本
- Git
- Maven 3.8+，或直接使用仓库自带的 Maven Wrapper

### 1. 获取代码

```bash
git clone https://github.com/hz-xigang/server.git
cd server
```

### 2. 准备数据库与文件目录

开发环境配置位于 `xg-app/src/main/resources/application-dev.yaml`。启动前至少需要调整数据库连接和静态文件根目录：

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=xg_wms;encrypt=false;trustServerCertificate=true;
    username: your_username
    password: your_password

static:
  base: D://code//upload//xg//
```

通用配置位于 `xg-app/src/main/resources/application.yaml`，其中 `u8.url` 是 U8 API 根地址。若本地不具备 U8 服务，需要避免触发订单同步和依赖 U8 的仓储操作。

> 仓库当前未包含数据库初始化或迁移脚本，需要准备与实体、Mapper 及数据库视图匹配的 SQL Server 数据库。

### 3. 编译

Windows：

```powershell
.\mvnw.cmd clean package -DskipTests
```

Linux 或 macOS：

```bash
./mvnw clean package -DskipTests
```

### 4. 启动

从项目根目录启动开发环境：

```powershell
.\mvnw.cmd -pl xg-app -am spring-boot:run "-Dspring-boot.run.profiles=dev"
```

服务默认监听：

```text
http://localhost:7100
```

除登录接口外，`/api/**` 默认要求携带 JWT：

```http
Authorization: Bearer <token>
```

## 配置说明

| 文件 | 用途 |
| --- | --- |
| `application.yaml` | 应用名、默认 Profile、MyBatis、日志格式、模板目录及 U8 地址 |
| `application-dev.yaml` | 本地数据库、开发端口、开发 JWT 和 SQL 控制台日志 |
| `application-prod.yaml` | 生产数据库、端口、JWT、文件目录及日志实现 |
| `logback-spring.xml` | 控制台日志和文件滚动策略 |

生产环境建议显式设置以下环境变量：

| 环境变量 | 说明 | 默认值 |
| --- | --- | --- |
| `DB_HOST` | SQL Server 地址 | `localhost` |
| `DB_PORT` | SQL Server 端口 | `1433` |
| `DB_NAME` | 数据库名 | `xg_wms` |
| `DB_USERNAME` | 数据库账号 | 配置文件内置值 |
| `DB_PASSWORD` | 数据库密码 | 配置文件内置值 |
| `SERVER_PORT` | HTTP 端口 | `8080` |
| `JWT_SECRET` | JWT HMAC 密钥，至少 32 字节 | 配置文件内置值 |
| `JWT_EXPIRATION_MINUTES` | Token 有效时间，单位分钟 | `480` |
| `STATIC_BASE_PATH` | 模板和临时文件根目录 | `E://xg-wms-server//static//` |

生产部署时不要依赖账号、密码和 JWT 密钥的默认值，应通过环境变量注入独立凭据。SQL Server 生产连接默认启用加密并校验证书，需要正确配置受信任证书。

## 构建产物

`xg-app` 使用 Thin JAR 方式打包。执行 `package` 后，主要产物位于：

```text
xg-app/target/
├── xg-app-0.0.1-SNAPSHOT.jar
└── lib/                             # 运行依赖
```

部署时业务 JAR 与 `lib/` 目录需要保持相对位置不变。可使用生产 Profile 启动：

```powershell
java -jar .\xg-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 核心 API

以下仅列出主要入口，具体请求结构以 Controller、DTO 和 Request 类为准。

| 领域 | 方法与路径 | 说明 |
| --- | --- | --- |
| 认证 | `POST /api/user/login` | 用户登录并获取 JWT |
| 用户 | `POST /api/user/page` | 用户分页查询 |
| 角色 | `POST /api/role/right` | 分配角色权限 |
| 权限 | `GET /api/right/menu/tree/current` | 当前用户菜单树 |
| 权限 | `GET /api/right/pda/list/current` | 当前用户 PDA 功能列表 |
| 库位 | `GET /api/loc` | 有效库位列表 |
| 模板 | `POST /api/file-temp/{id}/upload` | 上传打印模板 |
| 生产订单 | `POST /api/prod/page` | 生产订单分页查询 |
| 备料单 | `POST /api/prep/order` | 创建备料指令 |
| 发货单 | `POST /api/ship/order/sync` | 手动同步 U8 发货单 |
| 待办 | `GET /api/order/todo-summary` | PDA 作业待办汇总 |
| 标签 | `POST /api/productionTag` | 生成标签并返回 PDF |
| 标签 | `POST /api/productionTag/unpack` | 拆箱并生成新标签 |
| 托盘 | `POST /api/pallet` | 组托 |
| 入库 | `POST /api/stock/in` | 执行入库 |
| 移库 | `POST /api/stock/move` | 执行库位移动 |
| 发货 | `POST /api/ship` | 扫码发货并核销库存 |
| 库存 | `GET /api/inventory/summary` | 库位库存汇总 |

## 开发约定

- Controller 负责 HTTP 协议、参数校验和响应封装，不承载复杂业务逻辑。
- Service 负责流程编排、事务和跨模块调用。
- PlusService 负责数据访问组合、查询复用和持久化状态校验。
- 新增 Entity/DTO 映射时优先沿用现有 MapStruct 方式。
- 涉及多个标签的写操作必须复用 `BarcodeLockUtil` 的统一排序加锁策略。
- 涉及 U8 单据的字段调整，应同时检查 `xg-u8` DTO、转换服务及调用方。
- 提交信息使用中文，推荐格式：`功能: 描述`、`修复: 描述`、`优化: 描述`、`文档: 描述`。

## 注意事项

- `BarcodeLockUtil` 是 JVM 进程内锁。如果将应用部署为多个实例，需要增加数据库锁或分布式锁来保证跨实例互斥。
- 入库、移库和发货等流程会同步调用 U8；U8 不可用或返回失败时，相关本地事务可能回滚。
- 标签打印依赖有效的 Excel 模板和 Aspose Cells License 资源。
- 静态文件目录必须对应用运行账号具有读写权限。

## 许可证

本项目为私有项目，未经授权不得用于商业用途或对外分发。
