-- =============================================================
-- 04-ProdOrder 新增 U8 同步字段（13 列）
-- 需求：对接用友销售/采购订单，实体类 ProdOrder 已新增 13 个字段
-- 数据库：SQL Server
-- 说明：幂等脚本，已存在的列会自动跳过；执行完建议补充 MS_Description
-- =============================================================

-- -------------------------------------------------------------
-- 第 1 步 新增列（COL_LENGTH 判断，已存在则跳过）
-- -------------------------------------------------------------
IF COL_LENGTH('dbo.ProdOrder', 'customerOrderNo') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [customerOrderNo] nvarchar(50) NULL;   -- 客户订单号 cdefine10
GO
IF COL_LENGTH('dbo.ProdOrder', 'orderTypeName') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [orderTypeName] nvarchar(50) NULL;     -- 订单类型 cdefine3
GO
IF COL_LENGTH('dbo.ProdOrder', 'saleType') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [saleType] nvarchar(50) NULL;          -- 销售类型 cstname
GO
IF COL_LENGTH('dbo.ProdOrder', 'salesperson') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [salesperson] nvarchar(50) NULL;       -- 业务员 cpersonname
GO
IF COL_LENGTH('dbo.ProdOrder', 'bom') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [bom] nvarchar(100) NULL;              -- BOM cdefine23
GO
IF COL_LENGTH('dbo.ProdOrder', 'packingRequirement') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [packingRequirement] nvarchar(200) NULL; -- 包装要求 cfree8
GO
IF COL_LENGTH('dbo.ProdOrder', 'annealingMethod') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [annealingMethod] nvarchar(50) NULL;   -- 退火方式 cinvdefine9
GO
IF COL_LENGTH('dbo.ProdOrder', 'sprayCutting') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [sprayCutting] nvarchar(50) NULL;      -- 喷涂切割 cinvdefine10
GO
IF COL_LENGTH('dbo.ProdOrder', 'technicalRequirement') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [technicalRequirement] nvarchar(200) NULL; -- 技术要求 cinvdefine8
GO
IF COL_LENGTH('dbo.ProdOrder', 'po') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [po] nvarchar(50) NULL;                -- PO cdefine32
GO
IF COL_LENGTH('dbo.ProdOrder', 'processRoute') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [processRoute] nvarchar(100) NULL;     -- 工艺路线 cdefine28
GO
IF COL_LENGTH('dbo.ProdOrder', 'plannedCompletionDate') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [plannedCompletionDate] date NULL;     -- 预完工日期 dpremodate
GO
IF COL_LENGTH('dbo.ProdOrder', 'plannedDeliveryDate') IS NULL
    ALTER TABLE [dbo].[ProdOrder] ADD [plannedDeliveryDate] date NULL;       -- 预发货日期 dpredate
GO

-- -------------------------------------------------------------
-- 第 2 步 补充列说明（MS_Description，可选）
-- -------------------------------------------------------------
EXEC sp_addextendedproperty 'MS_Description', N'客户订单号', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'customerOrderNo';
EXEC sp_addextendedproperty 'MS_Description', N'订单类型', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'orderTypeName';
EXEC sp_addextendedproperty 'MS_Description', N'销售类型', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'saleType';
EXEC sp_addextendedproperty 'MS_Description', N'业务员', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'salesperson';
EXEC sp_addextendedproperty 'MS_Description', N'BOM', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'bom';
EXEC sp_addextendedproperty 'MS_Description', N'包装要求', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'packingRequirement';
EXEC sp_addextendedproperty 'MS_Description', N'退火方式', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'annealingMethod';
EXEC sp_addextendedproperty 'MS_Description', N'喷涂切割', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'sprayCutting';
EXEC sp_addextendedproperty 'MS_Description', N'技术要求', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'technicalRequirement';
EXEC sp_addextendedproperty 'MS_Description', N'PO', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'po';
EXEC sp_addextendedproperty 'MS_Description', N'工艺路线', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'processRoute';
EXEC sp_addextendedproperty 'MS_Description', N'预完工日期', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'plannedCompletionDate';
EXEC sp_addextendedproperty 'MS_Description', N'预发货日期', 'SCHEMA', N'dbo', 'TABLE', N'ProdOrder', 'COLUMN', N'plannedDeliveryDate';

-- -------------------------------------------------------------
-- 回滚脚本（如需撤销）
-- ALTER TABLE [dbo].[ProdOrder] DROP COLUMN plannedDeliveryDate, plannedCompletionDate, processRoute, po, technicalRequirement, sprayCutting, annealingMethod, packingRequirement, bom, salesperson, saleType, orderTypeName, customerOrderNo;
-- -------------------------------------------------------------
