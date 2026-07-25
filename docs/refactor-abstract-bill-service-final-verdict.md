# AbstractBillService 重构最终结论

## 结论：采用 Codex 轻量方案

Codex 方案更适合当前代码库规模（3 个继承类），引入 1 个组件而非 6 个，避免了用新的中心抽象替换旧的中心抽象。

---

## 现状诊断

### 继承情况

| 服务 | 状态 | 说明 |
|------|------|------|
| `StockInService` | 继承 | `addByTransfer()` 绕过父类 |
| `StockMoveService` | 继承 | `addByPrep()` 绕过父类，传 `checkExits=false` |
| `PalletService` | 继承 | 唯一完全按父类逻辑的 |
| `StockOutService` | **未继承** | 宁愿重复代码也不用父类 |

`StockOutService` 的"逃离"本身就是最有力的证据：这个抽象的成本已超过收益。

### 问题清单（按严重程度）

**1. 类型安全（紧急）**
```kotlin
protected abstract fun saveBill(entity: Any)          // 零编译时保障
protected abstract fun buildBill(...): Any             // 子类必须强转
protected abstract fun buildTagEntry(..., context: Map<String, Any>)  // 运行时才报错
```

**2. 异常处理（紧急）**
```kotlin
catch (e: Exception) {
    pmt.rollback(status)
    throw WebException(e.message)  // e.message 可能为 null，原始堆栈丢失
}
```

**3. 领域模型被反向塑形（严重，需独立 PR）**
```java
// StockMove 是聚合根，却被迫继承标签关联表基类
public class StockMove extends TagEntity {  // ❌
    private String pId;    // 本该属于 StockMoveTag
    private String tagNo;  // 本该属于 StockMoveTag
}
```
原因链：`StockMovePlusService : AbstractTagPlusService<StockMoveMapper, StockMove>`
为了复用 `listByTagNos()` 查询，主表被迫伪装成关联表实体。

**4. 职责过重（8 个职责揉在 `doAdd()` 里）**
标签去重 / 空值校验 / 存在性校验 / 占用校验 / 汇总 / 主表构建保存 / 明细构建保存 / 事务管理

**5. 代码重复**
`calcTotal()` 在 `StockOutService` 和 `StockMoveService` 各自实现了一遍，与父类 `doAdd()` 中的 fold 逻辑完全相同。

**6. 其他**
- `checkExits` 拼写错误（应为 `checkExists`）
- `@Suppress("UNCHECKED_CAST")` 扩散到每个子类

---

## 重构方案

### 核心思路

只抽真正稳定的共性：**标签准备逻辑**。其余业务流程保留在各 Service 中显式编排。

### 唯一新增组件：`BillTagResolver`

```kotlin
data class ResolvedTags(
    val tagNos: List<String>,
    val prodTags: List<VProdTag>,
    val total: ProdTagTotal
)

@Component
class BillTagResolver(private val prodTagPlusService: ProdTagPlusService) {

    fun resolve(tagNos: List<String>): ResolvedTags {
        if (tagNos.isEmpty()) throw WebException("请扫描纸箱标签")
        val distinctTagNos = tagNos.distinct()
        val prodTags = prodTagPlusService.listByTagNos(distinctTagNos)
        if (prodTags.size != distinctTagNos.size) {
            val missing = distinctTagNos - prodTags.map { it.tagNo }.toSet()
            throw WebException("【${missing.joinToString(",")}】不存在")
        }
        val total = prodTags.fold(ProdTagTotal(0, BigDecimal.ZERO, BigDecimal.ZERO)) { acc, it ->
            ProdTagTotal(acc.qty + it.qty, acc.grossWeight + it.grossWeight, acc.netWeight + it.netWeight)
        }
        return ResolvedTags(distinctTagNos, prodTags, total)
    }
}
```

职责边界：只负责"标签准备"，不负责占用检查、主表构建、保存、库存操作。

### 重构后的 Service 形态（以 StockInService 为例）

```kotlin
@Service
class StockInService(
    private val plusService: StockInPlusService,
    private val stockInTagPlusService: StockInTagPlusService,
    private val locArchivePlusService: LocArchivePlusService,
    private val stockInventoryService: StockInventoryService,
    private val sysSequenceService: SysSequenceService,
    private val billTagResolver: BillTagResolver,
) {
    @Transactional
    fun add(req: AddStockIn) {
        val locArchive = locArchivePlusService.getById(req.locId) ?: throw WebException("该库位不存在")
        val resolved = billTagResolver.resolve(req.tagNos)

        // 占用检查：StockInService 自己控制，不依赖父类参数
        val occupied = stockInTagPlusService.listByTagNos(resolved.tagNos)
        if (occupied.isNotEmpty()) throw WebException("【${occupied.joinToString(",") { it.tagNo }}】已入库")

        val id = IdUtil.generateId()
        val (userId, username, realName) = UserContext.require()

        val stockIn = StockIn().apply {
            this.id = id
            receiptNo = sysSequenceService.generateStockIn()
            qty = resolved.total.qty
            grossWeight = resolved.total.grossWeight
            netWeight = resolved.total.netWeight
            loc = locArchive.locCode
            this.userId = userId; this.username = username; this.realName = realName
        }
        val tags = resolved.tagNos.map { tagNo ->
            StockInTag().apply { pId = id; this.tagNo = tagNo; locId = locArchive.id; locCode = locArchive.locCode }
        }

        plusService.save(stockIn)
        stockInTagPlusService.saveBatch(tags)
        stockInventoryService.addBatch(resolved.prodTags, locArchive)
    }
}
```

对比原来：去掉了 `AbstractBillService` 依赖、`PlatformTransactionManager`、`Map<String, Any>`、7 个抽象方法实现、`@Suppress("UNCHECKED_CAST")`。

---

## 落地顺序

### 第一阶段：紧急修复（可独立 PR，无风险）

1. 提取 `BillTagResolver`
2. `PalletService` 迁出父类（最简单，先验证方案）
3. `StockInService` 迁出父类
4. `StockMoveService` 迁出父类
5. 删除 `AbstractBillService`
6. `StockOutService` 接入 `BillTagResolver`（消除 `calcTotal` 重复）

所有 Service 方法改为 `@Transactional`，删除手动事务块。

### 第二阶段：修复领域模型（独立 PR + 数据库迁移）

1. 新建 `StockMoveTag` 实体和 mapper
2. 编写 Flyway 迁移脚本（将 `StockMove` 中的 `pId`/`tagNo` 数据迁移到新表）
3. `StockMove` 移除 `extends TagEntity`
4. `StockMovePlusService` 改回普通 `ServiceImpl`
5. 更新 `StockMoveService` 中相关查询逻辑

**必须与第一阶段分离**：混在一起会导致 PR 无法交付。

---

## 两个方案对比

| 维度 | Codex 方案 | Claude 方案 |
|------|-----------|------------|
| 新增组件数 | 1（`BillTagResolver`） | 6（Validator/Aggregator/Checker/Builder/Context/Orchestrator） |
| 抽象范围 | 只抽标签准备 | 统一整条建单链路 |
| 业务流程 | 保留在各 Service | 统一编排在 Orchestrator |
| 扩展性 | 适合 2-3 种单据 | 适合 5+ 种单据 |
| 引入风险 | 低 | 中 |
| Claude 方案缺陷 | — | `BillOrchestrator` 用反射取 `bill.id`，重现了 `Any` 的问题 |

Claude 方案的反射问题根因：泛型边界围绕持久层设计，而非领域契约。正确做法是一个接口：

```kotlin
interface BillEntity { val id: String }
```

但这个修复反而证明了 Codex 的判断：当前规模不需要 `BillOrchestrator`，`BillTagResolver` 已经足够。
