# AbstractBillService 重构建议（Codex 版）

## 1. 结论摘要

`AbstractBillService` 确实存在过度抽象的问题，但问题的核心不只是 `Any`、`Map<String, Any>` 这类类型不安全写法，而是它抽象出来的并不是一个稳定的领域模型，而只是几种单据“新增流程”的局部交集。

这会带来两个后果：

1. 能套进去的子类看起来复用了很多代码，但会被迫接受不自然的接口。
2. 不完全匹配的业务会绕开父类，或者为了适配父类而污染领域模型。

因此，我不建议继续围绕 `AbstractBillService` 迭代一个更大的通用抽象；更合适的方向是：

1. 删除这层父类。
2. 把真正稳定的共享能力抽成轻量组件。
3. 让各个 Service 自己显式编排各自的业务流程。

## 2. 现状诊断

### 2.1 父类职责过重

当前 `AbstractBillService` 同时承担了以下职责：

1. 标签去重与空值校验
2. 标签存在性校验
3. 标签占用校验
4. 标签汇总
5. 主表构建与保存
6. 明细构建与保存
7. 事务提交与回滚

这说明它不是一个单一职责的抽象，而是一个把多段流程揉在一起的“半编排器”。

### 2.2 抽象边界不稳定

父类的关键接口如下：

```kotlin
protected abstract fun buildBill(..., context: Map<String, Any>): Any
protected abstract fun saveBill(entity: Any)
protected abstract fun buildTagEntry(..., context: Map<String, Any>): TagEntity
```

这几个信号很明显：

1. 父类并不知道自己真正操作的是什么领域对象。
2. `context` 不是领域模型，只是一个临时参数袋。
3. 子类必须依赖强转和约定俗成的 key。

这类抽象通常说明“流程像是共用的，但数据模型并没有真正统一”。

### 2.3 业务差异已经突破了父类边界

三个继承类和一个未继承类，已经表现出明显分化：

| 服务 | 现状 | 说明 |
|------|------|------|
| `StockInService` | 继承 | 勉强适配成功 |
| `PalletService` | 继承 | 勉强适配成功 |
| `StockMoveService` | 继承 | 适配成本高，已经出现模型扭曲 |
| `StockOutService` | 不继承 | 直接绕过父类 |

`StockOutService` 没有复用父类，说明这层抽象并没有成为自然的业务基座。

### 2.4 最严重的问题是领域模型被反向塑形

最值得警惕的不是 `Any`，而是为了让 `StockMoveService` 适配父类，出现了下面这种设计：

1. `StockMoveService.tagService()` 返回 `stockMovePlusService`
2. `StockMovePlusService` 继承了 `AbstractTagPlusService`
3. `StockMove` 实体继承了 `TagEntity`

这意味着“主表”为了复用“标签关联表”的查询接口，被迫伪装成标签实体。

这已经不是代码风格问题，而是领域语义被抽象层反向污染。

## 3. 我的重构方向

## 3.1 目标

目标不是再造一个更大的 `AbstractBillService 2.0`，而是只抽真正稳定的共性，把可变的业务流程留在具体 Service 中。

我建议的目标形态是：

1. 删除 `AbstractBillService`
2. Service 恢复为普通业务服务
3. 公共逻辑只保留在轻量组件里
4. 事务改为声明式 `@Transactional`

## 3.2 只抽一层轻量共享能力

我建议新增一个轻量组件，例如 `BillTagResolver`，职责只包含：

1. 校验标签列表不能为空
2. 标签去重
3. 读取 `ProdTag`
4. 检查缺失标签
5. 汇总 `qty / grossWeight / netWeight`

返回结构可以是：

```kotlin
data class ResolvedTags(
    val tagNos: List<String>,
    val prodTags: List<VProdTag>,
    val total: ProdTagTotal
)
```

这个组件只解决“标签准备”问题，不负责：

1. 是否已入库
2. 是否已打包
3. 是否在库存中
4. 主表/明细如何落库
5. 落库后的库存动作

这些逻辑仍然由具体业务 Service 自己控制。

## 3.3 Service 端改为显式编排

### `PalletService`

这是最容易迁移的服务，基本流程很单纯：

1. 解析标签
2. 校验是否已打包
3. 构建 `Pallet`
4. 构建 `PalletTag`
5. 保存

它很适合作为第一批迁移对象。

### `StockInService`

这个服务也适合迁移，但保留自己的业务编排：

1. 查库位
2. 解析标签
3. 校验是否已入库
4. 构建 `StockIn`
5. 构建 `StockInTag`
6. 保存主表/明细
7. 调用 `stockInventoryService.addBatch(...)`

这样可以彻底去掉 `Map<String, Any>`，也不需要模板方法。

### `StockMoveService`

这个服务不应该再被当成“普通单据新增”看待，因为它比其它服务多一层关键语义：

1. 标签必须存在
2. 标签必须在库存中
3. 还要带出原库位信息

因此它应该显式拆成两段：

1. 用 `BillTagResolver` 做基础标签解析
2. 用 `StockInventoryPlusService` 校验并加载 origin stock

然后自行构建 `StockMove` 与 `StockMoveTag`。

### `StockOutService`

这个服务本来就没有继承父类。重构后它可以选择只复用 `BillTagResolver`，统一标签汇总逻辑，但不需要被拉进新的统一抽象中。

## 3.4 事务处理

我建议把手工事务：

```kotlin
val status = pmt.getTransaction(definition)
try {
    ...
    pmt.commit(status)
} catch (e: Exception) {
    pmt.rollback(status)
    throw WebException(e.message)
}
```

改为业务 Service 方法上的 `@Transactional`。

原因：

1. 更符合 Spring 现有习惯
2. 业务流程更直观
3. 更容易做单元测试和集成测试
4. 少一层基础设施耦合

## 4. 和 Claude 方案的比较

## 4.1 一致的部分

Claude 方案里有几项判断我认同：

1. `AbstractBillService` 确实存在过度设计问题
2. 类型安全需要补上
3. 事务应该改回声明式事务
4. 组合优于继续扩张继承体系

这些方向性判断没有问题。

## 4.2 我认为 Claude 方案偏重的地方

Claude 方案的主线是：

1. `TagValidator`
2. `TagAggregator`
3. `TagOccupancyChecker`
4. `BillBuilder<TBill, TDetail>`
5. `BillContext`
6. `BillOrchestrator`

这套设计从工程理论上是完整的，但对当前仓库规模和业务复杂度来说，我认为有点偏重。

主要原因有三点：

### 1. 又形成了新的中心抽象

虽然不再是继承父类，但 `BillOrchestrator` 实际上仍然在承担“统一建单流程总控”的角色。

这意味着：

1. 原来是模板方法的复杂度，可能转移成 orchestrator 的复杂度
2. 后续一旦出现更多变体，`BillOrchestrator` 仍然会继续长大

### 2. 组件数量偏多

对当前 3 个继承类 + 1 个未继承类的场景来说，一次性引入太多抽象角色，团队后续理解成本会偏高。

尤其是当“共享逻辑”实际上只稳定在标签解析这一小段时，拆出整套 builder/checker/context/orchestrator，收益未必高于成本。

### 3. 新方案里仍然有不够自然的点

例如 Claude 示例里通过反射取 `bill.id`：

```kotlin
val billId = (bill as Any).let {
    it::class.members.first { m -> m.name == "id" }.call(it) as String
}
```

这说明即使换成了新架构，核心建模边界仍然没有完全收敛。

也就是说，它虽然解决了旧父类的一部分问题，但又引入了新的通用性成本。

## 4.3 我这版方案和 Claude 方案的差异

| 维度 | Claude 方案 | Codex 方案 |
|------|-------------|------------|
| 总体方向 | 组合替代继承 | 组合替代继承 |
| 抽象力度 | 中到高 | 低到中 |
| 中心组件 | `BillOrchestrator` | `BillTagResolver` |
| 共享范围 | 试图统一整条建单链路 | 只统一稳定的标签准备链路 |
| 业务流程 | 通过 Builder / Checker 编排 | 保留在具体 Service 中 |
| 类型安全 | 强 | 够用且更贴近当前代码 |
| 引入成本 | 较高 | 较低 |
| 迁移风险 | 中 | 低 |
| 适合阶段 | 业务形态高度稳定后 | 当前阶段更合适 |

## 4.4 我的取舍判断

如果团队准备做一轮比较大的架构整理，并且未来还会继续新增很多“同构单据”，Claude 方案可以作为中长期方向参考。

但结合当前项目现状，我更推荐优先落地一版轻量重构：

1. 先把最伤的抽象去掉
2. 先修复领域模型污染
3. 先提取真正稳定的共享逻辑
4. 等业务形态进一步稳定后，再判断是否需要更高层的 orchestrator

## 5. 建议的落地顺序

### 第一阶段：最小风险重构

1. 新增 `BillTagResolver`
2. `PalletService` 迁出父类
3. `StockInService` 迁出父类
4. 删除手工事务，改为 `@Transactional`

### 第二阶段：处理最不自然的模型

1. `StockMoveService` 迁出父类
2. `StockMovePlusService` 改回普通 `ServiceImpl`
3. `StockMove` 不再继承 `TagEntity`

### 第三阶段：统一零散重复逻辑

1. `StockOutService` 接入 `BillTagResolver`
2. 统一 `calcTotal` 类重复实现
3. 删除 `AbstractBillService`

## 6. 最终建议

这次重构我建议遵循一个原则：

不要把“父类抽象过重”的问题，替换成“组件编排过重”的问题。

当前代码库真正稳定、值得抽象出来的，是“标签准备逻辑”，不是“整条建单流程”。

因此我建议优先采用轻量方案：

1. 去父类
2. 留 Service
3. 抽小组件
4. 修正模型语义

这样更容易落地，也更符合当前仓库的真实复杂度。
