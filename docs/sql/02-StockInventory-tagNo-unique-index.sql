-- =============================================================
-- STK-3 修复：StockInventory.tagNo 唯一过滤索引
-- 需求：deleted = 0（在库）的 tagNo 不能重复出现；deleted = 1（已出库）的历史记录允许重复
-- 数据库：SQL Server
-- 说明：这是"防重复入库"的数据库层兜底，与文档规则一致
--       （唯一索引 IX_StockInventory_tagNo WHERE deleted=0）
-- =============================================================

-- -------------------------------------------------------------
-- 第 1 步（必做）预检：查找 deleted=0 的重复 tagNo
-- 若查询有结果，需先人工处理重复数据，否则建索引会失败
-- -------------------------------------------------------------
SELECT tagNo, COUNT(*) AS dupCount
FROM StockInventory
WHERE deleted = 0
GROUP BY tagNo
HAVING COUNT(*) > 1;

-- -------------------------------------------------------------
-- 第 2 步（建议）预检：deleted 列是否允许 NULL
-- 过滤索引对 NULL 不生效；若 allows_null = 1，建议执行第 3 步
-- -------------------------------------------------------------
SELECT COLUMNPROPERTY(OBJECT_ID('StockInventory'), 'deleted', 'AllowsNull') AS deleted_allows_null;

-- -------------------------------------------------------------
-- 第 3 步（按需）清理 deleted 为 NULL 的历史数据，并加固列约束
-- 注意：若表中已有默认值约束，ALTER TABLE 加 DEFAULT 会冲突，跳过即可
-- -------------------------------------------------------------
UPDATE StockInventory SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE StockInventory ALTER COLUMN deleted INT NOT NULL;

-- -------------------------------------------------------------
-- 第 4 步 创建唯一过滤索引（幂等：已存在则先删）
-- -------------------------------------------------------------
IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_StockInventory_tagNo' AND object_id = OBJECT_ID('StockInventory')
)
BEGIN
    DROP INDEX IX_StockInventory_tagNo ON StockInventory;
END;

CREATE UNIQUE INDEX IX_StockInventory_tagNo
ON StockInventory (tagNo)
WHERE deleted = 0;

-- -------------------------------------------------------------
-- 回滚脚本（如需撤销）
-- DROP INDEX IX_StockInventory_tagNo ON StockInventory;
-- -------------------------------------------------------------
