package com.gz.xg.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产线或出库备料-备料指令明细表(国内/外贸双模式混合表)
 */
@Data
public class PrepOrderDetailDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    private String id;

    /**
     * 关联的备料主表(PrepOrder)唯一主键ID
     */
    private String orderId;

    /**
     * 冗余单据类型：1 = 国内，2 = 外贸，用于快速筛选免去关联主表
     */
    private Integer orderType;

    /**
     * 国内专用：产品规格
     */
    private String spec;

    /**
     * 国内专用：材质属性说明
     */
    private String material;

    /**
     * 国内专用：产品结算或计划出库总重量(千克)
     */
    private BigDecimal weight;

    /**
     * 外贸专用：外贸大托盘编号
     */
    private String palletNo;

    /**
     * 外贸专用：托盘物理规格尺寸
     */
    private String palletSize;

    /**
     * 外贸专用：集装箱装箱单明细行序号
     */
    private Integer seqNo;

    /**
     * 外贸专用：单只纸箱物理尺寸规格
     */
    private String cartonSize;

    /**
     * 外贸专用：内衬铁芯或管芯物理尺寸规格
     */
    private String coreSize;

    /**
     * 外贸专用：每个标准纸箱内包含的产品小件数量
     */
    private Integer piecesPerCarton;

    /**
     * 外贸专用：本次备料出库的产品打包总箱数
     */
    private Integer totalCartons;

    /**
     * 外贸专用：外贸批次出库的产品总数量(件数)
     */
    private BigDecimal totalQty;

    /**
     * 外贸专用：订舱海运提单所关联的理论申报重量
     */
    private BigDecimal seaWeight;

    /**
     * 外贸专用：包含托盘重量在内的整托实测毛重
     */
    private BigDecimal grossWeight;

    /**
     * 公共通用：数量字段。国内单 = 总数量；外贸单 = 单箱包装数量
     */
    private BigDecimal qty;

    /**
     * 关联的源头加工生产订单号
     */
    private String orderNo;

    /**
     * 用友系统的存货编码
     */
    private String inventoryCode;

    /**
     * 针对当前明细行的业务特殊备注说明
     */
    private String remark;

    /**
     * 记录同步或录入系统的时间
     */
    private LocalDateTime createTime;

    /**
     * 自定义扩展备注字段1
     */
    private String m1;

    /**
     * 自定义扩展备注字段2
     */
    private String m2;

    /**
     * 自定义扩展备注字段3
     */
    private String m3;

    /**
     * 自定义扩展备注字段4
     */
    private String m4;

    /**
     * 自定义扩展备注字段5
     */
    private String m5;
}
