# 数据库 ER 图

说明：这份 ER 图仅基于 [db.sql](D:/code/java/hz-xg/db.sql) 梳理，没有读取其他代码文件。  
另外，库里几乎没有 `FOREIGN KEY`，所以图中的大部分连线是根据字段名、索引和表职责推断出的逻辑关系，不是数据库硬约束。

```mermaid
flowchart LR
  subgraph M["主数据"]
    LocArchive["LocArchive\n库位档案"]
  end

  subgraph P["生产与标签"]
    ProdOrder["ProdOrder\n生产订单"]
    ProdTag["ProdTag\n纸箱条码 tagNo"]
    ProdOrder -->|"1:N  prodOrderId"| ProdTag
  end

  subgraph T["打托"]
    Pallet["Pallet\n托盘主表"]
    PalletTag["PalletTag\n托盘-条码关联"]
    Pallet -->|"1:N  pId"| PalletTag
    ProdTag -->|"1:N  tagNo"| PalletTag
  end

  subgraph O["业务指令"]
    PrepOrder["PrepOrder\n备料主表"]
    PrepOrderDetail["PrepOrderDetail\n备料明细"]
    ShipOrder["ShipOrder\n发货指令主表"]
    ShipOrderDetail["ShipOrderDetail\n发货明细"]
    TransferOrder["TransferOrder\n调拨主表"]
    TransferOrderDetail["TransferOrderDetail\n调拨明细"]

    PrepOrder -->|"1:N  orderId"| PrepOrderDetail
    ShipOrder -->|"1:N  pId"| ShipOrderDetail
    TransferOrder -->|"1:N  pId"| TransferOrderDetail
  end

  subgraph S["库存执行与当前库存"]
    StockIn["StockIn\n入库主表"]
    StockInTag["StockInTag\n入库条码明细"]
    StockMove["StockMove\n移库主表"]
    StockMoveTag["StockMoveTag\n移库条码明细"]
    StockOut["StockOut\n出库主表"]
    StockOutTag["StockOutTag\n出库条码明细"]
    StockInventory["StockInventory\n实时库存"]
    ShipRecord["ShipRecord\n发货记录"]

    StockIn -->|"1:N  pId（推断）"| StockInTag
    StockMove -->|"1:N  pId（推断）"| StockMoveTag
    StockOut -->|"1:N  pId（推断）"| StockOutTag

    ProdTag -->|"1:0..1  tagNo"| StockInventory
    ProdTag -->|"1:N  tagNo"| StockInTag
    ProdTag -->|"1:N  tagNo"| StockMoveTag
    ProdTag -->|"1:N  tagNo"| StockOutTag
    ProdTag -->|"1:N  tagNo"| ShipRecord

    LocArchive -->|"1:N  locId/locCode"| StockInventory
    LocArchive -->|"1:N  locId/locCode"| StockInTag
    LocArchive -->|"1:N  locId/locCode"| StockMove
    LocArchive -->|"1:N  locId/locCode"| StockOutTag
    LocArchive -->|"1:N  loc/locCode（推断）"| ShipRecord
  end

  PrepOrder -.->|"业务执行通常驱动"| StockOut
  ShipOrder -.->|"业务执行通常驱动"| StockOut
  TransferOrder -.->|"调出通常驱动"| StockOut
  TransferOrder -.->|"调入通常驱动"| StockIn
  StockOut -->|"发货落地后形成"| ShipRecord
  ShipOrder -->|"指令来源"| ShipRecord
```

## 关键说明

- `ProdTag.tagNo` 是整套 WMS 里最核心的追踪键，库存和扫码动作基本都围绕它展开。
- `StockInventory` 表示当前态库存；`StockIn`、`StockMove`、`StockOut`、`ShipRecord` 更偏过程记录和历史记录。
- `Pallet` 是容器，真实货物最小粒度仍然是 `tagNo`。
- `PrepOrder`、`ShipOrder`、`TransferOrder` 更像业务指令单，和库存执行之间主要是逻辑关系，不是数据库外键约束。
- `StockInTag.pId`、`StockOutTag.pId` 的注释疑似有复制痕迹，实际更可能分别关联 `StockIn.id`、`StockOut.id`。
