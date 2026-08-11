-- =============================================================
-- PAL-1 修复：palletTag.tagNo 唯一过滤索引
-- 需求：deleted = 0 的 tagNo 不能重复出现；deleted = 1 的允许重复
-- 数据库：SQL Server
-- 说明：过滤索引（Filtered Index）只约束 deleted = 0 的行
-- =============================================================

-- -------------------------------------------------------------
-- 第 1 步（必做）预检：查找 deleted=0 的重复 tagNo
-- 若查询有结果，需先人工处理重复数据，否则建索引会失败
-- -------------------------------------------------------------
SELECT tagNo, COUNT(*) AS dupCount
FROM palletTag
WHERE deleted = 0
GROUP BY tagNo
HAVING COUNT(*) > 1;

-- -------------------------------------------------------------
-- 第 2 步（建议）预检：deleted 列是否允许 NULL
-- 过滤索引对 NULL 不生效；若 allows_null = 1，建议执行第 3 步
-- -------------------------------------------------------------
SELECT COLUMNPROPERTY(OBJECT_ID('palletTag'), 'deleted', 'AllowsNull') AS deleted_allows_null;

-- -------------------------------------------------------------
-- 第 3 步（按需）清理 deleted 为 NULL 的历史数据，并加固列约束
-- 注意：若表中已有默认值约束，ALTER TABLE 加 DEFAULT 会冲突，跳过即可
-- -------------------------------------------------------------
UPDATE palletTag SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE palletTag ALTER COLUMN deleted INT NOT NULL;

-- -------------------------------------------------------------
-- 第 4 步 创建唯一过滤索引（幂等：已存在则先删）
-- -------------------------------------------------------------
IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UQ_palletTag_tagNo_active' AND object_id = OBJECT_ID('palletTag')
)
BEGIN
    DROP INDEX UQ_palletTag_tagNo_active ON palletTag;
END;

CREATE UNIQUE INDEX UQ_palletTag_tagNo_active
ON palletTag (tagNo)
WHERE deleted = 0;

-- -------------------------------------------------------------
-- 回滚脚本（如需撤销）
-- DROP INDEX UQ_palletTag_tagNo_active ON palletTag;
-- -------------------------------------------------------------
