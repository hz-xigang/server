# XG-WMS 仓库管理系统

> 基于 Spring Boot 3 + Kotlin/Java 混合开发的现代化仓库管理系统

## 📋 项目简介

XG-WMS 是一套企业级仓库管理系统（Warehouse Management System），采用模块化架构设计，提供生产订单管理、库存管理、标签打印、库位管理等核心功能。

### 核心特性

- 🏗️ **模块化架构** - 基于 Maven 多模块设计，职责清晰，易于维护
- 🔐 **JWT 认证** - 安全可靠的用户认证机制
- 📦 **生产订单管理** - 完整的生产订单流程
- 🏷️ **标签打印** - EasyPOI 模板 + Aspose PDF 自动生成标签
- 📊 **库存管理** - 实时库存跟踪、库位管理
- 🎨 **模板管理** - 动态模板配置，支持自定义打印格式

## 🛠️ 技术栈

### 后端框架
- **Spring Boot** 3.5.14 - 应用框架
- **Kotlin** 2.2.0 + **Java** 17 - 混合开发语言
- **MyBatis-Plus** 3.5.15 - ORM 框架
- **MyBatis-Plus-Join** 1.5.7 - 关联查询增强

### 数据库
- **SQL Server** - 主数据库（通过 mssql-jdbc 13.2.1 连接）

### 工具库
- **Lombok** 1.18.42 - 简化代码
- **MapStruct** 1.5.5 - 对象映射
- **JJWT** 0.12.6 - JWT 认证
- **EasyPOI** 4.5.0 - Excel 模板导出
- **Aspose Cells** 8.5.2 - Excel 转 PDF
- **Gson** 2.13.2 - JSON 序列化

## 📦 模块架构

```
xg (root)
├── xg-app           # 应用启动模块（主入口）
├── xg-shared        # 共享基础模块（工具类、异常、常量）
├── xg-system        # 系统管理模块（用户、权限、序列号生成）
├── xg-basic         # 基础数据模块（库位档案、文件模板管理）
├── xg-order         # 订单管理模块（生产订单）
├── xg-warehouse     # 仓储业务模块（入库、出库、盘点、标签管理）
└── xg-print         # 打印模块（报表生成、PDF 转换）
```

### 模块说明

| 模块 | 职责 | 核心功能 |
|-----|------|---------|
| `xg-app` | 应用启动 | Spring Boot 主启动类、配置文件 |
| `xg-shared` | 共享基础 | 异常处理、响应封装、工具类 |
| `xg-system` | 系统管理 | 用户认证、JWT、序列号生成 |
| `xg-basic` | 基础数据 | 库位档案、文件模板管理 |
| `xg-order` | 订单管理 | 生产订单 CRUD |
| `xg-warehouse` | 仓储业务 | 入库、出库、移库、盘点、标签管理 |
| `xg-print` | 打印报表 | EasyPOI 模板填充、PDF 生成 |

## 🚀 快速开始

### 环境要求

- **JDK** 17+
- **Maven** 3.8+
- **SQL Server** 2016+
- **IDE** IntelliJ IDEA（推荐）

### 本地开发

1. **克隆项目**
   ```bash
   git clone https://github.com/hz-xigang/server.git
   cd server
   ```

2. **配置数据库**
   
   修改 `xg-app/src/main/resources/application-dev.yaml`：
   ```yaml
   spring:
     datasource:
       url: jdbc:sqlserver://localhost:1433;databaseName=xg_wms;encrypt=false;trustServerCertificate=true
       username: your_username
       password: your_password
   ```

3. **配置文件路径**
   
   修改 `application-dev.yaml` 中的静态文件路径：
   ```yaml
   static:
     base: D://code//upload//xg//
     template: template/
     tmp: tmp/
   ```

4. **编译运行**
   ```bash
   # Maven 编译
   mvn clean install -DskipTests
   
   # 启动应用（开发环境）
   cd xg-app
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. **访问应用**
   ```
   http://localhost:7100
   ```

### 生产部署

详见 [Windows Server 部署指南](docs/deploy-windows-server.md)

## 📚 API 文档

### 核心接口

#### 库位管理
- `POST /api/loc` - 新增库位
- `PUT /api/loc` - 更新库位
- `DELETE /api/loc/{id}` - 删除库位
- `POST /api/loc/page` - 分页查询
- `GET /api/loc` - 查询列表

#### 文件模板管理
- `POST /api/file-temp` - 新增模板
- `PUT /api/file-temp` - 更新模板
- `POST /api/file-temp/{id}/upload` - 上传模板文件
- `DELETE /api/file-temp/{id}` - 删除模板
- `POST /api/file-temp/page` - 分页查询

#### 标签打印
- `POST /api/productionTag` - 创建标签（自动生成 PDF）
- `POST /api/productionTag/list` - 查询标签列表
- `GET /api/productionTag/tag/{tagNo}` - 查询标签详情
- `DELETE /api/productionTag/{id}` - 删除标签

## 🏗️ 架构设计

### 分层架构

```
┌─────────────────────────────────────┐
│         Controller 层               │  ← RESTful API
├─────────────────────────────────────┤
│         Service 层                  │  ← 业务流程编排
├─────────────────────────────────────┤
│         PlusService 层              │  ← 数据访问封装
├─────────────────────────────────────┤
│         Mapper 层                   │  ← MyBatis-Plus
├─────────────────────────────────────┤
│         Database (SQL Server)       │
└─────────────────────────────────────┘
```

### 设计原则

- **职责分离** - Controller 处理请求，Service 编排业务，PlusService 封装数据访问
- **数据传输对象** - DTO/VO/Entity 严格分离
- **MapStruct 映射** - 对象转换零反射，性能优异
- **编程式事务** - 使用 `TransactionTemplate`，代码更灵活可控

### 标签打印流程

```
用户创建标签
    ↓
保存标签数据（生成 tagNo）
    ↓
获取生产单关联的模板ID
    ↓
查询模板文件路径
    ↓
准备数据 Map（客户编号、工单号等）
    ↓
EasyPOI 填充 Excel 模板
    ↓
Aspose Cells 转换为 PDF
    ↓
返回 PDF 字节流（浏览器直接下载）
```

## 📁 项目结构

```
xg-warehouse/
├── controller/          # 控制器
│   ├── ProdTagController.kt
│   └── StockInController.kt
├── service/            # 业务服务
│   ├── ProdTagService.kt
│   └── StockInService.kt
├── service/plus/       # 数据访问服务
│   ├── ProdTagPlusService.kt
│   └── StockInPlusService.kt
├── domain/
│   ├── dto/           # 数据传输对象
│   ├── entity/        # 数据库实体
│   ├── view/          # 视图实体
│   ├── mapstruct/     # 对象映射
│   └── search/        # 搜索条件
└── mapper/            # MyBatis Mapper
```

## 🔧 配置文件说明

### 环境配置

- `application.yaml` - 通用配置
- `application-dev.yaml` - 开发环境（本地数据库、控制台日志）
- `application-prod.yaml` - 生产环境（环境变量注入、文件日志）
- `application-local.yaml` - 个人配置（Git 忽略）

### 关键配置项

```yaml
# JWT 配置
jwt:
  secret: ${JWT_SECRET}              # 通过环境变量注入
  expiration-minutes: 480            # Token 过期时间（分钟）

# 数据库配置
spring:
  datasource:
    url: jdbc:sqlserver://${DB_HOST}:${DB_PORT};databaseName=${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

# 静态文件路径
static:
  base: ${STATIC_BASE_PATH}          # 文件存储根路径
  template: template/                # 模板子目录
  tmp: tmp/                          # 临时文件目录
```

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m '新增功能: XXX'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

### 代码规范

- **命名约定**：遵循 Kotlin/Java 标准命名规范
- **注释**：关键业务逻辑必须添加注释
- **提交信息**：使用中文，格式为 `类型: 描述`
  - `功能: 新增XXX功能`
  - `修复: 修复XXX问题`
  - `优化: 优化XXX性能`
  - `文档: 更新XXX文档`

## 📄 许可证

本项目采用私有许可证，未经授权不得用于商业用途。

## 📮 联系方式

- **项目地址**: [https://github.com/hz-xigang/server](https://github.com/hz-xigang/server)
- **问题反馈**: 请在 GitHub Issues 中提交

---

⭐ 如果这个项目对你有帮助，欢迎 Star！
