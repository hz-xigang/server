# PalletService / StockInService 执行流程

## 类结构

```
AbstractBillService          ← 模板方法基类，持有公共流程骨架
├── PalletService            ← 打托业务，入口：add(tagNos)
└── StockInService           ← 入库业务，入口：add(tagNos, locId)
```

---

## PalletService.add(tagNos)

```
调用方
  │
  ▼
PalletService.add(tagNos)
  │
  ├─[tagNos 为空]──→ throw WebException("请扫描纸箱标签")
  │
  ▼
AbstractBillService.doAdd(tagNos, context = {})
  │
  ├─ 1. 去重：distinctTagNos = tagNos.distinct()
  │
  ├─ 2. 查询纸箱标签是否存在
  │       ProdTagPlusService.listByTagNos(distinctTagNos)
  │       ├─[结果为空]──→ throw WebException("纸箱标签不存在")
  │       └─[部分不存在]──→ errMsg += "【xxx】 不存在"
  │
  ├─ 3. 查询条码是否已打包
  │       PalletTagPlusService.listByTagNos(distinctTagNos)   ← tagService() 返回
  │       └─[存在已打包条码]──→ errMsg += "【xxx】 已打包"
  │
  ├─[errMsg 不为空]──→ throw WebException(errMsg)
  │
  ├─ 4. 聚合统计
  │       fold(prodTags) → ProdTagTotal(qty, grossWeight, netWeight)
  │
  ├─ 5. 开启事务（PROPAGATION_REQUIRED）
  │
  ├─ 6. 生成主键 id = IdUtil.generateId()
  │
  ├─ 7. 生成托盘单号
  │       SysSequenceService.generatePallet()   ← generateNo() 实现
  │
  ├─ 8. 构建并保存 Pallet 主表
  │       PalletService.buildBill(id, no, total, context={})
  │         → Pallet { id, palletNo, qty, grossWeight, netWeight, printUser }
  │       PalletPlusService.save(pallet)         ← saveBill() 实现
  │
  ├─ 9. 构建并批量保存 PalletTag 关联表
  │       distinctTagNos.map { PalletTag(pId=id, tagNo=it) }   ← buildTagEntry() 实现
  │       PalletTagPlusService.saveBatch(palletTags)            ← saveTagBatch() 实现
  │
  ├─ 10. 提交事务
  │
  └─[异常]──→ 回滚事务 → throw WebException(e.message)
```

---

## StockInService.add(tagNos, locId)

```
调用方
  │
  ▼
StockInService.add(tagNos, locId)
  │
  ├─[tagNos 为空]──→ throw WebException("请扫描纸箱标签")
  │
  ├─ 前置校验：LocArchivePlusService.getById(locId)
  │       └─[库位不存在]──→ throw WebException("该库位不存在")
  │
  ├─ 填入 context = { "locId": locArchive.id, "locCode": locArchive.locCode }
  │
  ▼
AbstractBillService.doAdd(tagNos, context)
  │
  ├─ 1. 去重：distinctTagNos = tagNos.distinct()
  │
  ├─ 2. 查询纸箱标签是否存在
  │       ProdTagPlusService.listByTagNos(distinctTagNos)
  │       ├─[结果为空]──→ throw WebException("纸箱标签不存在")
  │       └─[部分不存在]──→ errMsg += "【xxx】 不存在"
  │
  ├─ 3. 查询条码是否已入库
  │       StockInTagPlusService.listByTagNos(distinctTagNos)   ← tagService() 返回
  │       └─[存在已入库条码]──→ errMsg += "【xxx】 已入库"
  │
  ├─[errMsg 不为空]──→ throw WebException(errMsg)
  │
  ├─ 4. 聚合统计
  │       fold(prodTags) → ProdTagTotal(qty, grossWeight, netWeight)
  │
  ├─ 5. 开启事务（PROPAGATION_REQUIRED）
  │
  ├─ 6. 生成主键 id = IdUtil.generateId()
  │
  ├─ 7. 生成入库单号
  │       SysSequenceService.generateStockIn()   ← generateNo() 实现
  │
  ├─ 8. 构建并保存 StockIn 主表
  │       StockInService.buildBill(id, no, total, context)
  │         → StockIn { id, receiptNo, qty, grossWeight, netWeight,
  │                     printUser, locId, locCode }      ← 从 context 取库位信息
  │       StockInPlusService.save(stockIn)               ← saveBill() 实现
  │
  ├─ 9. 构建并批量保存 StockInTag 关联表
  │       distinctTagNos.map { StockInTag(pId=id, tagNo=it) }   ← buildTagEntry() 实现
  │       StockInTagPlusService.saveBatch(stockInTags)           ← saveTagBatch() 实现
  │
  ├─ 10. 提交事务
  │
  └─[异常]──→ 回滚事务 → throw WebException(e.message)
```

---

## 两者差异对比

| 环节 | PalletService | StockInService |
|------|--------------|----------------|
| 入口签名 | `add(tagNos)` | `add(tagNos, locId)` |
| 前置校验 | 无 | 校验库位是否存在 |
| context 内容 | 空 | `{ locId, locCode }` |
| 占用校验来源 | `PalletTagPlusService` | `StockInTagPlusService` |
| 占用错误文案 | "已打包" | "已入库" |
| 单号生成 | `generatePallet()` | `generateStockIn()` |
| 主表实体 | `Pallet`（无库位字段） | `StockIn`（含 locId/locCode） |
| 关联表实体 | `PalletTag` | `StockInTag` |

---

## 扩展：新增 StockOutService 只需实现以下内容

```kotlin
@Service
class StockOutService(...) : AbstractBillService(prodTagPlusService, pmt) {

    override val tagOccupiedMessage = "已出库"
    override fun generateNo() = sysSequenceService.generateStockOut()
    override fun tagService() = stockOutTagPlusService
    override fun buildBill(id, no, total, context) = StockOut().also { /* 填字段 */ }
    override fun saveBill(entity: Any) { stockOutPlusService.save(entity as StockOut) }
    override fun buildTagEntry(pId, tagNo) = StockOutTag().also { it.pId = pId; it.tagNo = tagNo }
    override fun saveTagBatch(tags) { stockOutTagPlusService.saveBatch(tags as List<StockOutTag>) }

    // 如有额外前置校验，重写 add 后调 doAdd(tagNos, context)
}
```
