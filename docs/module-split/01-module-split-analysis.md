# 单体拆分为多 Module 的分析记录

## 背景

当前项目 `hz-xg` 是一个单 `pom.xml` 的 Spring Boot Maven 单体工程，暂时没有 Maven 子模块。

现状特征：

- 启动入口集中在 `XgApplication`
- 构建方式是单模块 Maven
- 代码按技术层分包：`controller / service / mapper / domain / common / exception / util`
- 持久层以 MyBatis-Plus 为主
- 项目混用 Java 和 Kotlin

这类结构在项目初期推进很快，但随着业务增长，会出现下面几个问题：

- 一个业务功能变更需要跨很多目录一起改
- 基础能力和业务能力混在一起，边界不清
- 依赖方向容易失控，后面很容易出现循环依赖
- 想做权限、基础资料、仓储执行等分治时，缺少天然承载结构


## 当前代码观察

结合现有代码，项目虽然还是单体，但已经出现了比较清晰的业务边界。

### 1. 系统能力

这一块主要包括：

- 用户登录
- JWT 鉴权
- 权限相关实体
- 流水号生成

代表类：

- `SysUserService`
- `JwtService`
- `AuthInterceptor`
- `SysSequenceService`
- `SysUser / SysRole / SysRight / SysSequence`

这一组能力比较稳定，适合单独形成系统模块。

### 2. 基础资料

当前最明显的是库位资料：

- `LocArchiveController`
- `LocArchiveService`
- `LocArchive` 及其 Mapper、Dto、MapStruct

这类数据天然适合归入基础资料模块，后续如果增加仓库、客户、供应商、产品主数据，也可以继续往这里扩展。

### 3. 业务单据域

当前已有一批典型业务单据：

- `ProdOrder`
- `PrepOrder`
- `ShipOrder`
- `TransferOrder`

这些对象大多以“主表 + 明细 + 查询/分页”为核心，属于偏业务协同层，而不是仓库执行层。

代表服务：

- `ProdOrderService`
- `PrepOrderService`
- `ShipOrderService`
- `TransferOrderService`

### 4. 仓储执行域

这一块是当前项目最重的业务核心，包含：

- 纸箱标签
- 托盘
- 入库
- 移库
- 库存
- 出库记录

代表类：

- `ProdTagService`
- `PalletService`
- `StockInService`
- `StockMoveService`
- `StockInventoryService`
- `ShipRecordService`

这部分还有一个明显特征：存在统一抽象流程。

`AbstractBillService` 已经把以下流程沉淀出来：

- 标签校验
- 汇总标签数据
- 生成单据主表
- 保存标签关联
- 事务提交

这说明仓储执行域已经具备形成独立模块的基础。


## 为什么不建议按技术层拆 Module

不建议拆成下面这种结构：

- `xg-api`
- `xg-service`
- `xg-dao`
- `xg-domain`

原因很直接：

- 这种拆法只是把现在的目录换成多个 jar
- 业务变更仍然要横跨多个 module
- 依赖方向容易变成所有模块互相引用
- 后期定位责任边界依然困难

这种拆法通常“看起来模块化”，但实际收益很有限。


## 推荐目标：模块化单体

建议采用“按业务域拆模块，但仍保持单体部署”的方式。

目标结构如下：

```text
hz-xg
├─ pom.xml
├─ xg-app
├─ xg-shared
├─ xg-system
├─ xg-basic
├─ xg-order
└─ xg-warehouse
```


## 各 Module 职责建议

### 1. xg-app

职责：

- 启动工程
- Spring Boot 自动装配入口
- 全局配置聚合
- 统一打包运行

建议放入：

- `XgApplication`
- `application.yaml`
- `WebMvcConfig`
- `CorsConfig`
- `ContextConfig`
- `GlobalExceptionHandler`

说明：

- `spring-boot-maven-plugin` 只放在这个模块
- 其他子模块都作为普通 jar 被引用

### 2. xg-shared

职责：

- 通用基础能力
- 稳定横切代码

建议放入：

- `ResponseResult`
- `WebException`
- `IdUtil`
- `DateUtil`
- `UserContext`
- `LoginUser`
- `BaseSearch`

注意事项：

- 不要把具体业务实体放到这里
- 不要把 Mapper、Controller、业务 Service 放到这里
- 这里只放高复用、低波动的公共能力

### 3. xg-system

职责：

- 用户体系
- 角色权限
- JWT 鉴权
- 流水号配置与生成

建议放入：

- `SysUser*`
- `SysRole*`
- `SysRight*`
- `SysSequence*`
- `JwtService`
- `AuthInterceptor`

说明：

- `SysSequenceService` 虽然被很多业务调用，但它本质上仍然属于系统能力
- 权限拦截与登录认证也应统一归到这里

### 4. xg-basic

职责：

- 基础资料维护

当前建议先放：

- `LocArchive*`

未来可以继续扩展：

- 仓库
- 库区
- 客户
- 供应商
- 产品主数据

### 5. xg-order

职责：

- 面向业务协同的订单/单据域

建议放入：

- `ProdOrder*`
- `PrepOrder*`
- `ShipOrder*`
- `TransferOrder*`

说明：

- 这一层偏业务指令和业务记录
- 主从表、查询分页、对外业务视图等，适合放这里

### 6. xg-warehouse

职责：

- 仓储执行能力
- 扫码、打托、入库、移库、库存变化等执行动作

建议放入：

- `AbstractBillService`
- `ProdTag*`
- `Pallet*`
- `StockIn*`
- `StockMove*`
- `StockInventory*`
- `StockOut*`
- `ShipRecord*`

说明：

- 这里是当前最重的业务模块
- 事务边界、标签校验、库存落账等都集中在这里


## 推荐依赖方向

建议依赖关系控制为：

- `xg-app -> xg-system, xg-basic, xg-order, xg-warehouse`
- `xg-system -> xg-shared`
- `xg-basic -> xg-shared`
- `xg-order -> xg-shared, xg-system`
- `xg-warehouse -> xg-shared, xg-system, xg-basic, xg-order`

重点原则：

- 尽量避免反向依赖
- 避免 `order` 反过来依赖 `warehouse`
- `shared` 不能依赖任何业务模块


## 模块内包结构建议

即使拆成多 module，也不建议模块内继续维持“大一统技术层分包”。

更建议按功能组织，例如：

```text
xg-system
└─ com.gz.xg.system
   ├─ auth
   ├─ user
   ├─ role
   ├─ right
   └─ sequence
```

```text
xg-warehouse
└─ com.gz.xg.warehouse
   ├─ core
   ├─ tag
   ├─ pallet
   ├─ stockin
   ├─ stockmove
   ├─ inventory
   └─ outbound
```

这样做的好处是：

- 一个功能的 controller/service/mapper/entity 可以放在相近位置
- 新人更容易理解一个功能完整落在哪里
- 未来继续扩展时更清晰


## 对当前代码的几个特别判断

### 1. `AbstractBillService` 是仓储模块核心抽象

这个类已经证明：

- 项目里存在统一的仓储执行流程
- `Pallet / StockIn / StockMove` 是一组同类业务

因此它应作为 `xg-warehouse` 的核心基类，而不是继续留在全局 `service` 目录中。

### 2. `BaseController` 不建议继续作为全局父类扩散

当前 `BaseController` 主要提供：

- 成功响应封装
- 当前用户获取

问题是它会推动所有控制器形成统一继承关系，进一步放大 web 层耦合。

建议后续逐步弱化它：

- 通用响应能力保留为工具或静态方法
- 控制器直接返回 `ResponseResult`
- 避免所有业务模块都依赖一个“全局控制器基类”

### 3. `SysSequenceService` 归 system，不归 warehouse

虽然托盘号、入库单号、移库单号都依赖流水号服务，但从职责上看，流水号生成仍是系统基础能力，不是仓储执行能力。

### 4. `ProdTag` 是关键跨域对象

`ProdTagService` 同时涉及：

- 生产单
- 标签生成
- 打托校验
- 入库校验

短期建议把 `ProdTag` 放在 `xg-warehouse`，因为它更偏执行介质。

长期如果标签能力继续膨胀，可以再单独演化出：

- `xg-label`


## 配置层需要提前注意的点

### 1. Mapper XML 路径

当前配置是：

```yaml
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
```

拆成多模块后建议改成：

```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
```

原因：

- 多模块下，Mapper XML 可能分布在不同 jar 内
- `classpath*:` 更适合扫描多个模块资源

### 2. Spring Boot 打包位置

建议：

- 只有 `xg-app` 使用 `spring-boot-maven-plugin`
- 其他模块都只输出普通 jar

### 3. 父 POM 统一插件管理

由于项目是：

- Java + Kotlin 混编
- MapStruct
- Lombok
- MyBatis-Plus

所以建议在父 `pom.xml` 统一：

- 版本管理
- Kotlin 编译插件
- Java 编译插件
- 注解处理相关配置


## 推荐迁移顺序

不建议一次性从单体直接大拆。

更稳妥的顺序是：

### 第一步：先在单模块内做包重构

目标：

- 先把包结构从技术层改成业务域
- 不先改 Maven 结构

原因：

- 风险最小
- 先把边界理顺，再切物理模块

### 第二步：先拆稳定模块

优先拆：

- `xg-shared`
- `xg-system`
- `xg-basic`

原因：

- 这几块边界最清晰
- 对主业务事务影响较小

### 第三步：拆 `xg-order`

原因：

- 大多是主从表和查询逻辑
- 相对容易抽离

### 第四步：最后拆 `xg-warehouse`

原因：

- 依赖最多
- 事务最重
- 和基础资料、系统、业务单据都有关系


## 当前阶段结论

结合现有仓库，最合适的方向不是“微服务化”，也不是“按技术层拆 jar”，而是：

按业务域拆成多个 Maven Module，保持单体部署，做成模块化单体。

推荐目标模块：

- `xg-app`
- `xg-shared`
- `xg-system`
- `xg-basic`
- `xg-order`
- `xg-warehouse`

这个拆法的好处是：

- 兼顾当前项目规模和复杂度
- 不会过早引入微服务运维成本
- 能立刻改善代码边界和团队协作效率
- 后续如果业务继续增长，也能平滑演进


## 下一步建议

下一步可以继续输出两类内容：

1. 多模块目录树和每个子模块的 `pom.xml` 骨架
2. 按现有类名生成一份“迁移清单”，列出每个文件应该迁移到哪个 module

如果继续推进，建议先做第 2 项，因为它最贴近当前仓库实际落地。
