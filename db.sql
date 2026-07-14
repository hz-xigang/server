/*
 Navicat Premium Data Transfer

 Source Server         : MSSQL2025
 Source Server Type    : SQL Server
 Source Server Version : 17001000 (17.00.1000)
 Source Host           : 127.0.01:1433
 Source Catalog        : xg_wms
 Source Schema         : dbo

 Target Server Type    : SQL Server
 Target Server Version : 17001000 (17.00.1000)
 File Encoding         : 65001

 Date: 11/07/2026 18:12:19
*/


-- ----------------------------
-- Table structure for LocArchive
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[LocArchive]') AND type IN ('U'))
	DROP TABLE [dbo].[LocArchive]
GO

CREATE TABLE [dbo].[LocArchive] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [locCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [locType] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [status] nvarchar(20) COLLATE Chinese_PRC_CI_AS DEFAULT '空闲' NOT NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[LocArchive] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库位/货位唯一编码(如: A-02-05, Z01)',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'locCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库位区域类型(如: 收料区、货架区、发货区、打包区)',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'locType'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库位当前占用状态(空闲/占用/禁用)',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库位档案创建录入时间',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常, 1:已删除)',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段1',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'm1'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段2',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'm2'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段3',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'm3'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段4',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'm4'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段5',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive',
'COLUMN', N'm5'
GO

EXEC sp_addextendedproperty
'MS_Description', N'仓库物理库位档案配置表',
'SCHEMA', N'dbo',
'TABLE', N'LocArchive'
GO


-- ----------------------------
-- Table structure for Pallet
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[Pallet]') AND type IN ('U'))
	DROP TABLE [dbo].[Pallet]
GO

CREATE TABLE [dbo].[Pallet] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [palletNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [qty] int DEFAULT 0 NOT NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [grossWeight] numeric(18,4)  NULL,
  [netWeight] numeric(18,4)  NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [username] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [userId] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[Pallet] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'Pallet',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'系统唯一托盘号/大箱号(格式如TP-xxxx)',
'SCHEMA', N'dbo',
'TABLE', N'Pallet',
'COLUMN', N'palletNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'该托盘内包含的纸箱总件数',
'SCHEMA', N'dbo',
'TABLE', N'Pallet',
'COLUMN', N'qty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'打托绑定/标签打印时间',
'SCHEMA', N'dbo',
'TABLE', N'Pallet',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常, 1:已删除)',
'SCHEMA', N'dbo',
'TABLE', N'Pallet',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'货品毛重(kg)，保留两位小数',
'SCHEMA', N'dbo',
'TABLE', N'Pallet',
'COLUMN', N'grossWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'货品净重(kg)，保留两位小数',
'SCHEMA', N'dbo',
'TABLE', N'Pallet',
'COLUMN', N'netWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户名',
'SCHEMA', N'dbo',
'TABLE', N'Pallet',
'COLUMN', N'username'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户id',
'SCHEMA', N'dbo',
'TABLE', N'Pallet',
'COLUMN', N'userId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'打托单据主表(托盘档案表)',
'SCHEMA', N'dbo',
'TABLE', N'Pallet'
GO


-- ----------------------------
-- Table structure for PalletTag
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[PalletTag]') AND type IN ('U'))
	DROP TABLE [dbo].[PalletTag]
GO

CREATE TABLE [dbo].[PalletTag] (
  [pId] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [tagNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL
)
GO

ALTER TABLE [dbo].[PalletTag] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'关联的打托主表(Pallet)唯一主键ID',
'SCHEMA', N'dbo',
'TABLE', N'PalletTag',
'COLUMN', N'pId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'纳入该托盘的纸箱单品条码号',
'SCHEMA', N'dbo',
'TABLE', N'PalletTag',
'COLUMN', N'tagNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'托盘关联的产品纸箱条码明细表',
'SCHEMA', N'dbo',
'TABLE', N'PalletTag'
GO


-- ----------------------------
-- Table structure for PrepOrder
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[PrepOrder]') AND type IN ('U'))
	DROP TABLE [dbo].[PrepOrder]
GO

CREATE TABLE [dbo].[PrepOrder] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [prepNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [customerCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [packingMethod] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [dispatchMode] nvarchar(100) COLLATE Chinese_PRC_CI_AS  NULL,
  [creator] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [deliveryTime] datetime  NULL,
  [remark] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [orderType] int  NOT NULL,
  [status] int DEFAULT 0 NOT NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [username] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [userId] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[PrepOrder] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'WMS系统唯一备料指令单号(通过流水号表派生)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'prepNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统同步或手工录入的客户唯一编码',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'customerCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'要求的货品包装方式(如:纸箱、木箱、托盘裸装)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'packingMethod'
GO

EXEC sp_addextendedproperty
'MS_Description', N'物流发运方式说明(如:汽运自提、顺丰速运)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'dispatchMode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'在后台系统创建或同步该备料单的制单人姓名',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'排程计划要求的最佳发货装车时间',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'deliveryTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'备料业务单据的说明信息',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单据贸易类型分类标记(1代表国内单, 2代表外贸单)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'orderType'
GO

EXEC sp_addextendedproperty
'MS_Description', N'手持PDA端执行的备料状态(0=未开始, 1=备料中, 2=已备料)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'WMS系统内该单据记录的同步创建时间',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常状态, 1:已删除/作废)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户名',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'username'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户id',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder',
'COLUMN', N'userId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'产线或出库备料-备料指令主表',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrder'
GO


-- ----------------------------
-- Table structure for PrepOrderDetail
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[PrepOrderDetail]') AND type IN ('U'))
	DROP TABLE [dbo].[PrepOrderDetail]
GO

CREATE TABLE [dbo].[PrepOrderDetail] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [orderId] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [orderType] int  NOT NULL,
  [spec] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [material] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [weight] decimal(18,4)  NULL,
  [palletNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [palletSize] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [seqNo] int  NULL,
  [cartonSize] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [coreSize] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [piecesPerCarton] int  NULL,
  [totalCartons] int  NULL,
  [totalQty] decimal(18,4)  NULL,
  [seaWeight] decimal(18,4)  NULL,
  [grossWeight] decimal(18,4)  NULL,
  [qty] decimal(18,4)  NULL,
  [orderNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [inventoryCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [remark] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[PrepOrderDetail] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'关联的备料主表(PrepOrder)唯一主键ID',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'orderId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'冗余单据类型：1 = 国内，2 = 外贸，用于快速筛选免去关联主表',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'orderType'
GO

EXEC sp_addextendedproperty
'MS_Description', N'国内专用：产品规格',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'spec'
GO

EXEC sp_addextendedproperty
'MS_Description', N'国内专用：材质属性说明',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'material'
GO

EXEC sp_addextendedproperty
'MS_Description', N'国内专用：产品结算或计划出库总重量(千克)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'weight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外贸专用：外贸大托盘编号',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'palletNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外贸专用：托盘物理规格尺寸',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'palletSize'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外贸专用：集装箱装箱单明细行序号',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'seqNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外贸专用：单只纸箱物理尺寸规格',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'cartonSize'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外贸专用：内衬铁芯或管芯物理尺寸规格',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'coreSize'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外贸专用：每个标准纸箱内包含的产品小件数量',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'piecesPerCarton'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外贸专用：本次备料出库的产品打包总箱数',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'totalCartons'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外贸专用：外贸批次出库的产品总数量(件数)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'totalQty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外贸专用：订舱海运提单所关联的理论申报重量',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'seaWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外贸专用：包含托盘重量在内的整托实测毛重',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'grossWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'公共通用：数量字段。国内单 = 总数量；外贸单 = 单箱包装数量',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'qty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'关联的源头加工生产订单号',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'orderNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的存货编码',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'inventoryCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'针对当前明细行的业务特殊备注说明',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
'MS_Description', N'记录同步或录入系统的时间',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常状态, 1:已删除)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'产线或出库备料-备料指令明细表(国内/外贸双模式混合表)',
'SCHEMA', N'dbo',
'TABLE', N'PrepOrderDetail'
GO


-- ----------------------------
-- Table structure for ProdOrder
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[ProdOrder]') AND type IN ('U'))
	DROP TABLE [dbo].[ProdOrder]
GO

CREATE TABLE [dbo].[ProdOrder] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [prodNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [erpOrderNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [inventoryCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [inventoryName] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [customerCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [productCategory] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [spec] nvarchar(100) COLLATE Chinese_PRC_CI_AS  NULL,
  [deleted] bit DEFAULT 0 NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [type] smallint  NULL,
  [specWidth] numeric(18,2)  NULL,
  [unitWeight] numeric(18,2)  NULL,
  [material] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[ProdOrder] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自动生成的系统唯一生产单号',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'prodNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'对应对接用友的销售单号/采购单号',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'erpOrderNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的存货编码',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'inventoryCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的存货名称',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'inventoryName'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的客户编码',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'customerCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'产品类别(手工填写)',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'productCategory'
GO

EXEC sp_addextendedproperty
'MS_Description', N'规格型号',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'spec'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'WMS系统单据创建时间',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段1',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'm1'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段2',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'm2'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段3',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'm3'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段4',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'm4'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段5',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'm5'
GO

EXEC sp_addextendedproperty
'MS_Description', N'0-销售订单
1-采购订单',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'规格厚度',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'specWidth'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单位重量',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'unitWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'材料',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder',
'COLUMN', N'material'
GO

EXEC sp_addextendedproperty
'MS_Description', N'生产订单表(精简页面版)',
'SCHEMA', N'dbo',
'TABLE', N'ProdOrder'
GO


-- ----------------------------
-- Table structure for ProdTag
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[ProdTag]') AND type IN ('U'))
	DROP TABLE [dbo].[ProdTag]
GO

CREATE TABLE [dbo].[ProdTag] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [tagNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [prodOrderId] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [qty] decimal(18,4)  NOT NULL,
  [grossWeight] decimal(18,4)  NULL,
  [netWeight] decimal(18,4)  NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [username] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [userId] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[ProdTag] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'条码号',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'tagNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'关联的生产订单表主键ID',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'prodOrderId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单箱物料数量',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'qty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'货品毛重(kg)，保留两位小数',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'grossWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'货品净重(kg)，保留两位小数',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'netWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'标签打印生成时间',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常, 1:已删除)',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段1',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'm1'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段2',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'm2'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段3',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'm3'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段4',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'm4'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段5',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'm5'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户名',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'username'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户id',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag',
'COLUMN', N'userId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'纸箱单品标签打印记录与流水表',
'SCHEMA', N'dbo',
'TABLE', N'ProdTag'
GO


-- ----------------------------
-- Table structure for ShipOrder
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[ShipOrder]') AND type IN ('U'))
	DROP TABLE [dbo].[ShipOrder]
GO

CREATE TABLE [dbo].[ShipOrder] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [shipNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [type] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [salesType] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [erpOrderNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [isTax] bit DEFAULT 0 NOT NULL,
  [salesDept] nvarchar(100) COLLATE Chinese_PRC_CI_AS  NULL,
  [dispatchMode] nvarchar(100) COLLATE Chinese_PRC_CI_AS  NULL,
  [customerCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [contactPerson] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [salesman] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [status] int DEFAULT 0 NULL
)
GO

ALTER TABLE [dbo].[ShipOrder] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'WMS系统唯一发货指令单号(可对应流水号表)',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'shipNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发货类型说明',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售类型说明',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'salesType'
GO

EXEC sp_addextendedproperty
'MS_Description', N'对应对接用友ERP的已审核销售单号/发货单号',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'erpOrderNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'价格是否含税(1:含税, 0:未税)',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'isTax'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统同步过来的销售部门名称',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'salesDept'
GO

EXEC sp_addextendedproperty
'MS_Description', N'物流发运方式说明',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'dispatchMode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的客户唯一编码',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'customerCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'收货联系人姓名',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'contactPerson'
GO

EXEC sp_addextendedproperty
'MS_Description', N'负责该单据的用友销售业务员姓名',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'salesman'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单据同步或创建时间',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常单据, 1:已撤销/软删除)',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'状态
0-创建
1-完成',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外部销售出库-发货指令单表',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrder'
GO


-- ----------------------------
-- Table structure for ShipOrderDetail
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[ShipOrderDetail]') AND type IN ('U'))
	DROP TABLE [dbo].[ShipOrderDetail]
GO

CREATE TABLE [dbo].[ShipOrderDetail] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [pId] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [orderNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [batchNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [warehouseName] varchar(100) COLLATE Chinese_PRC_CI_AS  NULL,
  [inventoryCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [inventoryName] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [spec] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [material] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [unit] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [qty] decimal(18,4)  NOT NULL,
  [weight] decimal(18,4)  NULL,
  [unitWeight] decimal(18,4)  NULL,
  [packingMethod] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [specWidth] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [customerCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[ShipOrderDetail] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'关联的发货单主表(ShippingInstruction)唯一主键ID',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'pId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'对应对接用友ERP的源头订单号',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'orderNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'货品的产品批号',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'batchNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'执行出库作业的物理仓库名称',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'warehouseName'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的存货编码',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'inventoryCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的存货名称',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'inventoryName'
GO

EXEC sp_addextendedproperty
'MS_Description', N'产品的规格型号说明',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'spec'
GO

EXEC sp_addextendedproperty
'MS_Description', N'产品材质属性',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'material'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存盘点与出库的库存计量单位',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'unit'
GO

EXEC sp_addextendedproperty
'MS_Description', N'计划发货的物料数量',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'qty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'计划发货的物料总重量',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'weight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单件货品的单品单重',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'unitWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统同步或指定的包装方式',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'packingMethod'
GO

EXEC sp_addextendedproperty
'MS_Description', N'货品规格片宽尺寸说明',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'specWidth'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的客户唯一编码',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'customerCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'明细单据记录的创建或同步时间',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常明细, 1:已删除)',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'外部销售出库-发货指令单明细表',
'SCHEMA', N'dbo',
'TABLE', N'ShipOrderDetail'
GO


-- ----------------------------
-- Table structure for ShipRecord
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[ShipRecord]') AND type IN ('U'))
	DROP TABLE [dbo].[ShipRecord]
GO

CREATE TABLE [dbo].[ShipRecord] (
  [id] varchar(255) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [shipOrder] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [inventoryCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [inventoryName] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [spec] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [material] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [unit] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [qty] decimal(18,4)  NOT NULL,
  [weight] decimal(18,4)  NULL,
  [unitWeight] decimal(18,4)  NULL,
  [packingMethod] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [specWidth] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [customerCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [outNo] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [loc] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [tagNo] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[ShipRecord] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'指令单',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'shipOrder'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的存货编码',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'inventoryCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的存货名称',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'inventoryName'
GO

EXEC sp_addextendedproperty
'MS_Description', N'产品的规格型号说明',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'spec'
GO

EXEC sp_addextendedproperty
'MS_Description', N'产品材质属性',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'material'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存盘点与出库的库存计量单位',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'unit'
GO

EXEC sp_addextendedproperty
'MS_Description', N'计划发货的物料数量',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'qty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'计划发货的物料总重量',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'weight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单件货品的单品单重',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'unitWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统同步或指定的包装方式',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'packingMethod'
GO

EXEC sp_addextendedproperty
'MS_Description', N'货品规格片宽尺寸说明',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'specWidth'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的客户唯一编码',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'customerCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'明细单据记录的创建或同步时间',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常明细, 1:已删除)',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'出库单号',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'outNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'出库仓库',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'loc'
GO

EXEC sp_addextendedproperty
'MS_Description', N'条码',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord',
'COLUMN', N'tagNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发货记录表',
'SCHEMA', N'dbo',
'TABLE', N'ShipRecord'
GO


-- ----------------------------
-- Table structure for StockIn
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[StockIn]') AND type IN ('U'))
	DROP TABLE [dbo].[StockIn]
GO

CREATE TABLE [dbo].[StockIn] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [receiptNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [qty] int DEFAULT 0 NOT NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [grossWeight] numeric(18,4)  NULL,
  [netWeight] numeric(18,4)  NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [username] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [userId] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[StockIn] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'WMS系统生成的唯一入库单号(通过流水号表派生)',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'receiptNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'本次扫描入库的物料总数量',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'qty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'PDA端确认提交入库的时间',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常, 1:已删除)',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'入库总货品毛重(kg)',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'grossWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'入库总货品净重(kg)',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'netWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段1',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'm1'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段2',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'm2'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段3',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'm3'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段4',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'm4'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段5',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'm5'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户名',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'username'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户id',
'SCHEMA', N'dbo',
'TABLE', N'StockIn',
'COLUMN', N'userId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'扫描入库记录主表',
'SCHEMA', N'dbo',
'TABLE', N'StockIn'
GO


-- ----------------------------
-- Table structure for StockInTag
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[StockInTag]') AND type IN ('U'))
	DROP TABLE [dbo].[StockInTag]
GO

CREATE TABLE [dbo].[StockInTag] (
  [pId] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [tagNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [locId] varchar(64) COLLATE Chinese_PRC_CI_AS  NULL,
  [locCode] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[StockInTag] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'关联的打托主表(Pallet)唯一主键ID',
'SCHEMA', N'dbo',
'TABLE', N'StockInTag',
'COLUMN', N'pId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'纳入该托盘的纸箱单品条码号',
'SCHEMA', N'dbo',
'TABLE', N'StockInTag',
'COLUMN', N'tagNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库位Id',
'SCHEMA', N'dbo',
'TABLE', N'StockInTag',
'COLUMN', N'locId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库位编号',
'SCHEMA', N'dbo',
'TABLE', N'StockInTag',
'COLUMN', N'locCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'入库关联的产品纸箱条码明细表',
'SCHEMA', N'dbo',
'TABLE', N'StockInTag'
GO


-- ----------------------------
-- Table structure for StockInventory
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[StockInventory]') AND type IN ('U'))
	DROP TABLE [dbo].[StockInventory]
GO

CREATE TABLE [dbo].[StockInventory] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [tagNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [locCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [locId] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [qty] int DEFAULT 0 NOT NULL,
  [grossWeight] numeric(18,4)  NULL,
  [netWeight] numeric(18,4)  NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [updateTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[StockInventory] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'StockInventory',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'在库的唯一纸箱标签条码号',
'SCHEMA', N'dbo',
'TABLE', N'StockInventory',
'COLUMN', N'tagNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'货品当前存放的数字化库位编码(如:A-01-02)',
'SCHEMA', N'dbo',
'TABLE', N'StockInventory',
'COLUMN', N'locCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'当前库位该条码的在库实物数量',
'SCHEMA', N'dbo',
'TABLE', N'StockInventory',
'COLUMN', N'qty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单箱货品毛重(kg)',
'SCHEMA', N'dbo',
'TABLE', N'StockInventory',
'COLUMN', N'grossWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单箱货品净重(kg)',
'SCHEMA', N'dbo',
'TABLE', N'StockInventory',
'COLUMN', N'netWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'该条码首次上架入库的物理时间',
'SCHEMA', N'dbo',
'TABLE', N'StockInventory',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'该条码最后一次发生变动(如PDA移库完成)的时间',
'SCHEMA', N'dbo',
'TABLE', N'StockInventory',
'COLUMN', N'updateTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常在库, 1:已出库核销/删除)',
'SCHEMA', N'dbo',
'TABLE', N'StockInventory',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'核心业务-实时动态库存表',
'SCHEMA', N'dbo',
'TABLE', N'StockInventory'
GO


-- ----------------------------
-- Table structure for StockMove
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[StockMove]') AND type IN ('U'))
	DROP TABLE [dbo].[StockMove]
GO

CREATE TABLE [dbo].[StockMove] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [receiptNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [qty] int DEFAULT 0 NOT NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [grossWeight] numeric(18,4)  NULL,
  [netWeight] numeric(18,4)  NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [locId] varchar(64) COLLATE Chinese_PRC_CI_AS  NULL,
  [locCode] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [username] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [userId] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[StockMove] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'WMS系统生成的唯一移库单号(通过流水号表派生)',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'receiptNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'本次扫描移库的物料总数量',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'qty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'PDA端确认提交移库的时间',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常, 1:已删除)',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'移库总货品毛重(kg)',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'grossWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'移库总货品净重(kg)',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'netWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段1',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'm1'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段2',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'm2'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段3',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'm3'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段4',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'm4'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段5',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'm5'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库位Id',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'locId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库位编号',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'locCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户名',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'username'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户id',
'SCHEMA', N'dbo',
'TABLE', N'StockMove',
'COLUMN', N'userId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'扫描移库记录主表',
'SCHEMA', N'dbo',
'TABLE', N'StockMove'
GO


-- ----------------------------
-- Table structure for StockMoveTag
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[StockMoveTag]') AND type IN ('U'))
	DROP TABLE [dbo].[StockMoveTag]
GO

CREATE TABLE [dbo].[StockMoveTag] (
  [pId] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [tagNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL
)
GO

ALTER TABLE [dbo].[StockMoveTag] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'关联的移库主表唯一主键ID',
'SCHEMA', N'dbo',
'TABLE', N'StockMoveTag',
'COLUMN', N'pId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'纳移库的纸箱单品条码号',
'SCHEMA', N'dbo',
'TABLE', N'StockMoveTag',
'COLUMN', N'tagNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'移库关联的产品纸箱条码明细表',
'SCHEMA', N'dbo',
'TABLE', N'StockMoveTag'
GO


-- ----------------------------
-- Table structure for StockOut
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[StockOut]') AND type IN ('U'))
	DROP TABLE [dbo].[StockOut]
GO

CREATE TABLE [dbo].[StockOut] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [receiptNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [qty] int DEFAULT 0 NOT NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [grossWeight] numeric(18,4)  NULL,
  [netWeight] numeric(18,4)  NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [username] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [userId] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[StockOut] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'WMS系统生成的唯一出库单号(通过流水号表派生)',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'receiptNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'本次扫描入库的物料总数量',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'qty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'PDA端确认提交出库的时间',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常, 1:已删除)',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'出库总货品毛重(kg)',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'grossWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'出库总货品净重(kg)',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'netWeight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段1',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'm1'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段2',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'm2'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段3',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'm3'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段4',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'm4'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义扩展备注字段5',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'm5'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户名',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'username'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户id',
'SCHEMA', N'dbo',
'TABLE', N'StockOut',
'COLUMN', N'userId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'出库记录主表',
'SCHEMA', N'dbo',
'TABLE', N'StockOut'
GO


-- ----------------------------
-- Table structure for StockOutTag
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[StockOutTag]') AND type IN ('U'))
	DROP TABLE [dbo].[StockOutTag]
GO

CREATE TABLE [dbo].[StockOutTag] (
  [pId] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [tagNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [locId] varchar(64) COLLATE Chinese_PRC_CI_AS  NULL,
  [locCode] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[StockOutTag] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'出库主表Id',
'SCHEMA', N'dbo',
'TABLE', N'StockOutTag',
'COLUMN', N'pId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'纳入该托盘的纸箱单品条码号',
'SCHEMA', N'dbo',
'TABLE', N'StockOutTag',
'COLUMN', N'tagNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库位Id',
'SCHEMA', N'dbo',
'TABLE', N'StockOutTag',
'COLUMN', N'locId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库位编号',
'SCHEMA', N'dbo',
'TABLE', N'StockOutTag',
'COLUMN', N'locCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'入库关联的产品纸箱条码明细表',
'SCHEMA', N'dbo',
'TABLE', N'StockOutTag'
GO


-- ----------------------------
-- Table structure for TransferOrder
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[TransferOrder]') AND type IN ('U'))
	DROP TABLE [dbo].[TransferOrder]
GO

CREATE TABLE [dbo].[TransferOrder] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [orderNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [orderDate] datetime  NULL,
  [applyNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [fromDept] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [toDept] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [fromWarehouse] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [toWarehouse] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [outCategory] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [inCategory] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [handler] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [auditTime] datetime  NULL,
  [orderType] int  NOT NULL,
  [remark] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [status] int DEFAULT 0 NULL
)
GO

ALTER TABLE [dbo].[TransferOrder] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'系统唯一或者对接用友ERP的调拨单号',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'orderNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'调拨业务单据生成的具体日期',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'orderDate'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统上游的调拨申请单号',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'applyNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'物料资产调出的源头部门名称',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'fromDept'
GO

EXEC sp_addextendedproperty
'MS_Description', N'物料资产调入的目标部门名称',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'toDept'
GO

EXEC sp_addextendedproperty
'MS_Description', N'调拨转出的物理仓库名称',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'fromWarehouse'
GO

EXEC sp_addextendedproperty
'MS_Description', N'调拨转入的目标仓库名称',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'toWarehouse'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友ERP定义的出库事务类别',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'outCategory'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友ERP定义的入库事务类别',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'inCategory'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单据在ERP系统内的经手操作员',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'handler'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友ERP审核确认该单据的物理时间',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'auditTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'调拨业务类型分类标记(1代表调入入库, 2代表调出发出)',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'orderType'
GO

EXEC sp_addextendedproperty
'MS_Description', N'调拨业务的备注说明(例如：返工、车间转仓)',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
'MS_Description', N'WMS系统内该单据记录的同步创建时间',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常状态, 1:已删除/作废)',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'状态
0-创建
1-完成',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'车间返工/库间调拨-调拨指令主表',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrder'
GO


-- ----------------------------
-- Table structure for TransferOrderDetail
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[TransferOrderDetail]') AND type IN ('U'))
	DROP TABLE [dbo].[TransferOrderDetail]
GO

CREATE TABLE [dbo].[TransferOrderDetail] (
  [id] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [pId] varchar(64) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [batchNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [inventoryCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [inventoryName] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [spec] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [unit] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [qty] decimal(18,4)  NOT NULL,
  [weight] decimal(18,4)  NULL,
  [specWidth] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [customerCode] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [pBatchNo] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [packingMethod] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [createTime] datetime DEFAULT getdate() NOT NULL,
  [deleted] int DEFAULT 0 NOT NULL,
  [m1] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m2] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m3] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m4] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [m5] varchar(255) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[TransferOrderDetail] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'全局唯一主键ID(字符串类型)',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'关联的调拨主表(TransferOrder)唯一主键ID',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'pId'
GO

EXEC sp_addextendedproperty
'MS_Description', N'调拨产品批号',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'batchNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的存货编码',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'inventoryCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的存货名称',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'inventoryName'
GO

EXEC sp_addextendedproperty
'MS_Description', N'产品的规格型号说明',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'spec'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存计量与调拨结算的单位',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'unit'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单项计划调拨的物料数量',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'qty'
GO

EXEC sp_addextendedproperty
'MS_Description', N'单项计划调拨的物料总重量',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'weight'
GO

EXEC sp_addextendedproperty
'MS_Description', N'货品规格片宽尺寸说明',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'specWidth'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统的客户唯一编码',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'customerCode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'该批成品所追踪追溯的源头母材批号',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'pBatchNo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用友系统同步或指定的包装方式',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'packingMethod'
GO

EXEC sp_addextendedproperty
'MS_Description', N'明细单据记录的创建或同步时间',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'createTime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'软删除逻辑标记(0:正常明细, 1:已删除)',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'车间返工/库间调拨-调拨指令明细表',
'SCHEMA', N'dbo',
'TABLE', N'TransferOrderDetail'
GO


-- ----------------------------
-- Indexes structure for table LocArchive
-- ----------------------------
CREATE UNIQUE NONCLUSTERED INDEX [IX_LocArchive_locCode]
ON [dbo].[LocArchive] (
  [locCode] ASC
)
WHERE ([deleted]=(0))
GO


-- ----------------------------
-- Primary Key structure for table LocArchive
-- ----------------------------
ALTER TABLE [dbo].[LocArchive] ADD CONSTRAINT [PK_LocArchive] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table Pallet
-- ----------------------------
CREATE UNIQUE NONCLUSTERED INDEX [IX_Pallet_palletNo]
ON [dbo].[Pallet] (
  [palletNo] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table Pallet
-- ----------------------------
ALTER TABLE [dbo].[Pallet] ADD CONSTRAINT [PK_Pallet] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table PalletTag
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_PalletTag_pId_tagNo]
ON [dbo].[PalletTag] (
  [pId] ASC,
  [tagNo] ASC
)
GO


-- ----------------------------
-- Indexes structure for table PrepOrder
-- ----------------------------
CREATE UNIQUE NONCLUSTERED INDEX [IX_PrepOrder_prepNo]
ON [dbo].[PrepOrder] (
  [prepNo] ASC
)
WHERE ([deleted]=(0))
GO


-- ----------------------------
-- Primary Key structure for table PrepOrder
-- ----------------------------
ALTER TABLE [dbo].[PrepOrder] ADD CONSTRAINT [PK_PrepOrder] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table PrepOrderDetail
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_PrepOrderDetail_orderId]
ON [dbo].[PrepOrderDetail] (
  [orderId] ASC
)
WHERE ([deleted]=(0))
GO


-- ----------------------------
-- Primary Key structure for table PrepOrderDetail
-- ----------------------------
ALTER TABLE [dbo].[PrepOrderDetail] ADD CONSTRAINT [PK_PrepOrderDetail] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table ProdOrder
-- ----------------------------
CREATE UNIQUE NONCLUSTERED INDEX [IX_wms_prodNo]
ON [dbo].[ProdOrder] (
  [prodNo] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table ProdOrder
-- ----------------------------
ALTER TABLE [dbo].[ProdOrder] ADD CONSTRAINT [PK_wmsProductionOrder] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table ProdTag
-- ----------------------------
CREATE UNIQUE NONCLUSTERED INDEX [IX_ProdTag_tagNo]
ON [dbo].[ProdTag] (
  [tagNo] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table ProdTag
-- ----------------------------
ALTER TABLE [dbo].[ProdTag] ADD CONSTRAINT [PK_ProdTag] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table ShipOrder
-- ----------------------------
CREATE UNIQUE NONCLUSTERED INDEX [IX_ShippingInstruction_shippingNo]
ON [dbo].[ShipOrder] (
  [shipNo] ASC
)
WHERE ([deleted]=(0))
GO


-- ----------------------------
-- Primary Key structure for table ShipOrder
-- ----------------------------
ALTER TABLE [dbo].[ShipOrder] ADD CONSTRAINT [PK_ShippingInstruction] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table ShipOrderDetail
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_ShippingInstructionDetail_shipId]
ON [dbo].[ShipOrderDetail] (
  [pId] ASC
)
WHERE ([deleted]=(0))
GO


-- ----------------------------
-- Primary Key structure for table ShipOrderDetail
-- ----------------------------
ALTER TABLE [dbo].[ShipOrderDetail] ADD CONSTRAINT [PK_ShippingInstructionDetail] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table ShipRecord
-- ----------------------------
ALTER TABLE [dbo].[ShipRecord] ADD CONSTRAINT [PK__ShipReco__3213E83F64B52484] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table StockIn
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_StockIn_receiptNo]
ON [dbo].[StockIn] (
  [receiptNo] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table StockIn
-- ----------------------------
ALTER TABLE [dbo].[StockIn] ADD CONSTRAINT [PK_StockIn] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table StockInTag
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_PalletTag_pId_tagNo_copy1]
ON [dbo].[StockInTag] (
  [pId] ASC,
  [tagNo] ASC
)
GO


-- ----------------------------
-- Indexes structure for table StockInventory
-- ----------------------------
CREATE UNIQUE NONCLUSTERED INDEX [IX_StockInventory_tagNo]
ON [dbo].[StockInventory] (
  [tagNo] ASC
)
WHERE ([deleted]=(0))
GO

CREATE NONCLUSTERED INDEX [IX_StockInventory_locCode]
ON [dbo].[StockInventory] (
  [locCode] ASC
)
WHERE ([deleted]=(0))
GO


-- ----------------------------
-- Primary Key structure for table StockInventory
-- ----------------------------
ALTER TABLE [dbo].[StockInventory] ADD CONSTRAINT [PK_StockInventory] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table StockMove
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_StockIn_receiptNo_copy1]
ON [dbo].[StockMove] (
  [receiptNo] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table StockMove
-- ----------------------------
ALTER TABLE [dbo].[StockMove] ADD CONSTRAINT [PK__stockIn___3213E83FA5B8A73D] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table StockMoveTag
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_PalletTag_pId_tagNo_copy1_copy1]
ON [dbo].[StockMoveTag] (
  [pId] ASC,
  [tagNo] ASC
)
GO


-- ----------------------------
-- Indexes structure for table StockOut
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_StockIn_receiptNo_copy2]
ON [dbo].[StockOut] (
  [receiptNo] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table StockOut
-- ----------------------------
ALTER TABLE [dbo].[StockOut] ADD CONSTRAINT [PK__StockIn___3213E83F76D46DF7] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table StockOutTag
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_PalletTag_pId_tagNo_copy1_copy2]
ON [dbo].[StockOutTag] (
  [pId] ASC,
  [tagNo] ASC
)
GO


-- ----------------------------
-- Indexes structure for table TransferOrder
-- ----------------------------
CREATE UNIQUE NONCLUSTERED INDEX [IX_TransferOrder_orderNo]
ON [dbo].[TransferOrder] (
  [orderNo] ASC
)
WHERE ([deleted]=(0))
GO


-- ----------------------------
-- Primary Key structure for table TransferOrder
-- ----------------------------
ALTER TABLE [dbo].[TransferOrder] ADD CONSTRAINT [PK_TransferOrder] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table TransferOrderDetail
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_TransferOrderDetail_orderId]
ON [dbo].[TransferOrderDetail] (
  [pId] ASC
)
WHERE ([deleted]=(0))
GO


-- ----------------------------
-- Primary Key structure for table TransferOrderDetail
-- ----------------------------
ALTER TABLE [dbo].[TransferOrderDetail] ADD CONSTRAINT [PK_TransferOrderDetail] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO

