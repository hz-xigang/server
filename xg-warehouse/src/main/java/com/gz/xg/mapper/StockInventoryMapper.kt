package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.gz.xg.domain.entity.StockInventory
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import java.math.BigDecimal

@Mapper
interface StockInventoryMapper : BaseMapper<StockInventory> {

    @Select("""
        SELECT COALESCE(SUM(si.qty), 0)
        FROM stockInventory si
        INNER JOIN prodTag pt ON si.tagNo = pt.tagNo
        WHERE pt.prodOrderId = #{prodOrderId}
          AND si.warehouseCode = #{warehouseCode}
          AND si.deleted = 0
          AND pt.deleted = 0
    """)
    fun sumQtyByProdOrderIdAndWarehouse(
        @Param("prodOrderId") prodOrderId: String,
        @Param("warehouseCode") warehouseCode: String
    ): BigDecimal?

}