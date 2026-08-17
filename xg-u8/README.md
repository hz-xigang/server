# Module U8 - 用友接口模块

> 用于调用用友 U8 ERP 系统接口

## 📋 模块说明

本模块封装了与用友 U8 系统的对接接口，提供采购订单、入库单等业务数据的查询和同步功能。

## 🔧 配置

在 `application.yaml` 或 `application-*.yaml` 中配置：

```yaml
u8:
  url: http://服务器地址:3040/U8_API    # 用友接口基础地址
  cacc-id: 001                           # 账套编号
  connect-timeout: 5000                  # 连接超时（毫秒）
  read-timeout: 10000                    # 读取超时（毫秒）
```

## 📡 已实现接口

### 1. 查询采购订单主表 (UAP_Po_main_query)

**接口路径**: `/UAP_Po_main_query`

**请求示例**:
```java
@Autowired
private U8PurchaseOrderService u8PurchaseOrderService;

// 使用默认账套
U8Response<U8PurchaseOrderMain> response = u8PurchaseOrderService.queryPurchaseOrderMain(null);

// 指定账套
U8Response<U8PurchaseOrderMain> response = u8PurchaseOrderService.queryPurchaseOrderMain("002");
```

**Java 字段说明**（已通过 @SerializedName 优化命名）:
- `voucherState`: 单据状态 (add/update/delete)
- `purchaseOrderId`: 订单主键
- `orderCode`: 单据号
- `orderDate`: 单据日期
- `vendorCode/vendorName`: 供应商编码/名称
- `purchaseTypeCode`: 采购类型
- `departmentCode/departmentName`: 部门编码/名称
- `memo`: 单据备注
- `maker/verifier`: 制单人/审核人
- `auditDate`: 审核日期
- `details`: 订单明细列表
  - `detailId`: 明细主键（用于生成入库单）
  - `inventoryCode/inventoryName`: 存货编码/名称
  - `specification`: 规格型号
  - `auxiliaryQuantity`: 辅计量数量
  - `quantity`: 数量
  - `rowMemo`: 行备注
  - `arriveDate`: 计划到货日期

### 2. 查询销售订单主表 (UAP_SO_main_query)

**接口路径**: `/UAP_SO_main_query`

**请求示例**:
```java
@Autowired
private U8SalesOrderService u8SalesOrderService;

// 使用默认账套
U8Response<U8SalesOrderMain> response = u8SalesOrderService.querySalesOrderMain(null);

// 指定账套
U8Response<U8SalesOrderMain> response = u8SalesOrderService.querySalesOrderMain("002");
```

**Java 字段说明**（已通过 @SerializedName 优化命名）:
- `voucherState`: 单据状态 (add/update/delete)
- `salesOrderId`: 订单主键
- `orderCode`: 单据号
- `orderDate`: 单据日期
- `customerCode/customerName`: 客户编码/名称
- `salesTypeCode`: 销售类型
- `departmentCode/departmentName`: 部门编码/名称
- `memo`: 单据备注
- `maker/verifier`: 制单人/审核人
- `auditDate`: 审核日期
- `details`: 订单明细列表
  - `detailId`: 子表主键（用于与发货单关联）
  - `inventoryCode/inventoryName`: 存货编码/名称
  - `specification`: 规格型号
  - `auxiliaryQuantity`: 辅计量数量
  - `quantity`: 数量
  - `rowMemo`: 行备注
  - `deliveryDate`: 计划发货日期

## 🧪 测试

运行测试类：
```bash
cd module_u8
mvn test -Dtest=U8Test#testQueryPurchaseOrderMain
```

## 📦 依赖

- Spring Boot Web
- Gson (JSON 处理)
- Lombok

## 🏗️ 目录结构

```
module_u8/
├── src/main/java/com/gz/xg/u8/
│   ├── config/
│   │   ├── U8Config.java           # 用友配置类
│   │   └── U8BeanConfig.java       # Bean 配置
│   ├── dto/
│   │   ├── U8PoMainQueryRequest.java
│   │   ├── U8PurchaseOrderMain.java
│   │   ├── U8PurchaseOrderDetail.java
│   │   └── U8Response.java         # 统一响应格式
│   └── service/
│       └── U8PurchaseOrderService.java
└── src/test/java/com/gz/xg/
    └── U8Test.java                 # 接口测试
```

## 📝 使用说明

1. **配置用友服务器地址**
   - 在项目配置文件中设置 `u8.url`

2. **注入服务**
   ```java
   @Autowired
   private U8PurchaseOrderService u8PurchaseOrderService;
   ```

3. **调用接口**
   ```java
   U8Response<U8PurchaseOrderMain> response = 
       u8PurchaseOrderService.queryPurchaseOrderMain("001");
   
   if (response.isSuccess()) {
       List<U8PurchaseOrderMain> orders = response.getData();
       // 处理订单数据
   }
   ```

## ⚠️ 注意事项

- 接口采用 HTTP POST 方式，JSON 格式
- 编码类型：UTF-8
- 超时时间可通过配置文件调整
- 建议在生产环境配置合理的超时时间和重试机制
- 接口返回的日期格式为字符串 (yyyy-MM-dd)

## 🔜 待实现接口

- [ ] 采购入库单接口
- [ ] 其他业务接口...
