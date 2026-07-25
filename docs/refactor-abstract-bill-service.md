# AbstractBillService 重构方案

## 现状：继承情况分析

当前**仅有 3 个类**继承了 `AbstractBillService`：

| 服务 | 继承状态 | 特殊参数 | 说明 |
|------|---------|---------|------|
| **StockInService** | ✅ 继承 | 默认 | 入库服务，需要校验标签占用 |
| **StockMoveService** | ✅ 继承 | `checkExits=false` | 移库服务，不检查占用 |
| **PalletService** | ✅ 继承 | 默认 | 托盘服务 |
| **StockOutService** | ❌ **未继承** | - | **完全绕过父类，自己实现** |

### 关键发现：StockOutService 的"逃离"

`StockOutService` 的开发者选择**完全不使用** `AbstractBillService`，而是自己实现了：
- `calcTotal()` - 标签汇总逻辑（与父类 `doAdd()` 重复）
- `createStockOut()` - 构建单据（与父类 `buildBill()` 重复）  
- `saveOut()` - 保存逻辑（与父类 `saveBill()` 重复）

**这证明了 `AbstractBillService` 的设计失败**——当开发者宁愿重复代码也不愿继承时，说明这个抽象类的成本已经超过了收益。

## 问题总结

当前 `AbstractBillService` 存在以下问题：
1. **类型不安全**（Any、Map<String, Any>、强制转换）
2. **强耦合**（强制依赖 ProdTagPlusService、PlatformTransactionManager）
3. **职责过多**（违反单一职责原则）
4. **抽象不够通用**（子类需要绕过父类逻辑，甚至完全不用）
5. **难以测试和扩展**
6. **代码重复**（StockOutService 重复实现了父类逻辑）

## 重构方案：组合优于继承

### 方案一：职责分离 + 组合模式（推荐）

```kotlin
// 1. 标签校验器（单一职责）
interface TagValidator {
    fun validate(tagNos: List<String>): TagValidationResult
}

data class TagValidationResult(
    val validTags: List<VProdTag>,
    val errors: List<String> = emptyList()
) {
    val isValid: Boolean get() = errors.isEmpty()
}

@Component
class DefaultTagValidator(
    private val prodTagPlusService: ProdTagPlusService
) : TagValidator {
    override fun validate(tagNos: List<String>): TagValidationResult {
        if (tagNos.isEmpty()) {
            return TagValidationResult(emptyList(), listOf("请扫描纸箱标签"))
        }
        
        val distinctTagNos = tagNos.distinct()
        val prodTags = prodTagPlusService.listByTagNos(distinctTagNos)
        val errors = mutableListOf<String>()
        
        if (prodTags.isEmpty()) {
            errors.add("纸箱标签不存在")
        }
        
        if (prodTags.size != distinctTagNos.size) {
            val exists = prodTags.map { it.tagNo }.toSet()
            val missing = distinctTagNos - exists
            errors.add("【${missing.joinToString(",")}】不存在")
        }
        
        return TagValidationResult(prodTags, errors)
    }
}

// 2. 标签汇总器（单一职责）
@Component
class TagAggregator {
    fun aggregate(tags: List<VProdTag>): ProdTagTotal {
        return tags.fold(ProdTagTotal(0, BigDecimal.ZERO, BigDecimal.ZERO)) { acc, item ->
            ProdTagTotal(
                acc.qty + item.qty,
                acc.grossWeight + item.grossWeight,
                acc.netWeight + item.netWeight
            )
        }
    }
}

// 3. 标签占用检查器（单一职责）
interface TagOccupancyChecker {
    fun checkOccupied(tagNos: List<String>): List<String>  // 返回已占用的标签号
}

class StockInTagOccupancyChecker(
    private val stockInTagPlusService: StockInTagPlusService
) : TagOccupancyChecker {
    override fun checkOccupied(tagNos: List<String>): List<String> {
        return stockInTagPlusService.listByTagNos(tagNos).map { it.tagNo }
    }
}

// 4. 单据构建器（类型安全）
interface BillBuilder<TBill, TDetail> {
    fun buildBill(context: BillContext, total: ProdTagTotal): TBill
    fun buildDetails(billId: String, tagNos: List<String>, context: BillContext): List<TDetail>
}

// 类型安全的上下文对象（替代 Map<String, Any>）
data class StockInContext(
    val locId: String,
    val locCode: String,
    val type: String? = null
) : BillContext

interface BillContext  // 标记接口

class StockInBillBuilder(
    private val sequenceService: SysSequenceService
) : BillBuilder<StockIn, StockInTag> {
    
    override fun buildBill(context: BillContext, total: ProdTagTotal): StockIn {
        val ctx = context as StockInContext
        val (userId, username) = UserContext.require()
        
        return StockIn().apply {
            id = IdUtil.generateId()
            receiptNo = sequenceService.generateStockIn()
            qty = total.qty
            grossWeight = total.grossWeight
            netWeight = total.netWeight
            loc = ctx.locCode
            type = ctx.type
            this.userId = userId
            this.username = username
        }
    }
    
    override fun buildDetails(billId: String, tagNos: List<String>, context: BillContext): List<StockInTag> {
        val ctx = context as StockInContext
        return tagNos.map { tagNo ->
            StockInTag().apply {
                pId = billId
                this.tagNo = tagNo
                locId = ctx.locId
                locCode = ctx.locCode
            }
        }
    }
}

// 5. 通用单据服务（组合各个组件）
@Component
class BillOrchestrator(
    private val tagValidator: TagValidator,
    private val tagAggregator: TagAggregator
) {
    
    @Transactional  // 使用声明式事务，而非编程式
    fun <TBill, TDetail> createBill(
        tagNos: List<String>,
        context: BillContext,
        builder: BillBuilder<TBill, TDetail>,
        occupancyChecker: TagOccupancyChecker? = null,
        occupiedMessage: String = "已被占用",
        saveBill: (TBill) -> Unit,
        saveDetails: (List<TDetail>) -> Unit,
        afterSave: (List<VProdTag>, ProdTagTotal) -> Unit = { _, _ -> }
    ) {
        // 1. 校验标签
        val validationResult = tagValidator.validate(tagNos)
        val errors = mutableListOf<String>()
        errors.addAll(validationResult.errors)
        
        // 2. 检查占用
        if (occupancyChecker != null) {
            val occupied = occupancyChecker.checkOccupied(tagNos)
            if (occupied.isNotEmpty()) {
                errors.add("【${occupied.joinToString(",")}】$occupiedMessage")
            }
        }
        
        if (errors.isNotEmpty()) {
            throw WebException(errors.joinToString("\r\n"))
        }
        
        // 3. 汇总
        val total = tagAggregator.aggregate(validationResult.validTags)
        
        // 4. 构建并保存
        val bill = builder.buildBill(context, total)
        saveBill(bill)
        
        val billId = (bill as Any).let { 
            it::class.members.first { m -> m.name == "id" }.call(it) as String
        }
        val details = builder.buildDetails(billId, tagNos.distinct(), context)
        saveDetails(details)
        
        // 5. 后续业务处理
        afterSave(validationResult.validTags, total)
    }
}
```

### 重构后的 StockInService

```kotlin
@Service
class StockInService(
    private val plusService: StockInPlusService,
    private val stockInTagPlusService: StockInTagPlusService,
    private val locArchivePlusService: LocArchivePlusService,
    private val stockInMapStruct: StockInMapStruct,
    private val stockInventoryService: StockInventoryService,
    
    // 组合各个职责清晰的组件
    private val billOrchestrator: BillOrchestrator,
    private val stockInBuilder: StockInBillBuilder,
    private val tagOccupancyChecker: StockInTagOccupancyChecker
) {
    
    fun add(req: AddStockIn) {
        addByType(req)
    }
    
    fun addReturn(req: AddStockIn) {
        addByType(req, "退货入库")
    }
    
    private fun addByType(req: AddStockIn, type: String? = null) {
        val locArchive = locArchivePlusService.getById(req.locId) 
            ?: throw WebException("该库位不存在")
        
        val context = StockInContext(
            locId = locArchive.id,
            locCode = locArchive.locCode,
            type = type
        )
        
        billOrchestrator.createBill(
            tagNos = req.tagNos,
            context = context,
            builder = stockInBuilder,
            occupancyChecker = tagOccupancyChecker,
            occupiedMessage = "已入库",
            saveBill = { plusService.save(it) },
            saveDetails = { stockInTagPlusService.saveBatch(it) },
            afterSave = { prodTags, _ -> 
                stockInventoryService.addBatch(prodTags, locArchive)
            }
        )
    }
    
    // 其他方法保持不变...
}
```

### 优势对比

| 维度 | 原设计 | 重构后 |
|------|--------|--------|
| **类型安全** | ❌ Any、Map<String,Any> | ✅ 泛型 + 类型安全的Context |
| **职责分离** | ❌ 一个类承担6个职责 | ✅ 每个类单一职责 |
| **可测试性** | ❌ 难以 mock 和测试 | ✅ 每个组件独立可测 |
| **可扩展性** | ❌ 继承 + 模板方法 | ✅ 组合 + 策略模式 |
| **依赖注入** | ❌ 强制依赖父类组件 | ✅ 按需注入需要的组件 |
| **事务管理** | ❌ 编程式事务 | ✅ 声明式事务 |

---

## 方案二：保留继承但改进类型安全（折中方案）

如果团队不想大改，可以先改进类型安全：

```kotlin
abstract class AbstractBillService<TBill, TDetail, TContext : BillContext>(
    protected val tagValidator: TagValidator,
    protected val tagAggregator: TagAggregator
) {
    
    protected abstract fun generateNo(): String
    protected abstract fun buildBill(id: String, no: String, total: ProdTagTotal, context: TContext): TBill
    protected abstract fun saveBill(entity: TBill)
    protected abstract fun buildTagEntry(pId: String, tagNo: String, context: TContext): TDetail
    protected abstract fun saveTagBatch(tags: List<TDetail>)
    protected abstract fun getOccupancyChecker(): TagOccupancyChecker?
    
    @Transactional
    protected fun doAdd(
        tagNos: List<String>,
        context: TContext,
        afterSave: (List<VProdTag>, ProdTagTotal) -> Unit = { _, _ -> }
    ) {
        // 使用组件而非内联逻辑
        val validationResult = tagValidator.validate(tagNos)
        // ... 其余逻辑
    }
}
```

## 推荐实施步骤

1. ✅ **第一步**：引入独立的 TagValidator、TagAggregator（不破坏现有代码）
2. ✅ **第二步**：新增 BillOrchestrator，让新业务使用新方案
3. ✅ **第三步**：逐步迁移现有 Service 到新方案
4. ✅ **第四步**：废弃 AbstractBillService

## 测试示例

```kotlin
@Test
fun `should validate tags correctly`() {
    val validator = DefaultTagValidator(mockProdTagService)
    val result = validator.validate(listOf("TAG001", "TAG002"))
    
    assertTrue(result.isValid)
    assertEquals(2, result.validTags.size)
}

@Test
fun `should aggregate tags correctly`() {
    val aggregator = TagAggregator()
    val tags = listOf(
        VProdTag(qty = 10, grossWeight = 100.toBigDecimal(), netWeight = 90.toBigDecimal()),
        VProdTag(qty = 5, grossWeight = 50.toBigDecimal(), netWeight = 45.toBigDecimal())
    )
    
    val result = aggregator.aggregate(tags)
    
    assertEquals(15, result.qty)
    assertEquals(150.toBigDecimal(), result.grossWeight)
}
```
