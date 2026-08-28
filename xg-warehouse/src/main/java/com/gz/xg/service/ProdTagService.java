package com.gz.xg.service;

import com.gz.xg.UserContext;
import com.gz.xg.base.BaseService;
import com.gz.xg.domain.auth.LoginUser;
import com.gz.xg.domain.dto.ProdTagDto;
import com.gz.xg.domain.entity.ProdOrder;
import com.gz.xg.domain.entity.ProdTag;
import com.gz.xg.domain.mapstruct.ProdTagMapStruct;
import com.gz.xg.domain.search.ProdTagSearch;
import com.gz.xg.domain.view.VProdTag;
import com.gz.xg.config.resource.StaticYmlConfig;
import com.gz.xg.exception.WebException;
import com.gz.xg.mapper.PrintLogMapper;
import com.gz.xg.report.ProdTagReport;
import com.gz.xg.service.plus.*;
import com.gz.xg.util.IdUtil;
import com.gz.xg.domain.entity.PrintLog;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 纸箱标签服务，负责标签生成、按生产单查询以及扫码校验。
 */
@Service
public class ProdTagService extends BaseService {


    @Resource
    private ProdTagPlusService prodTagPlusService;
    @Resource
    private ProductionOrderPlusService productionOrderPlusService;
    @Resource
    private SysSequenceService sysSequenceService;
    @Resource
    private ProdTagMapStruct prodTagMapStruct;
    @Resource
    private PalletTagPlusService palletTagPlusService;
    @Resource
    private StockInTagPlusService stockInTagPlusService;
    @Resource
    private StockInventoryPlusService stockInventoryPlusService;
    @Resource
    private ProdOrderService prodOrderService;
    @Resource
    private FileTempPlusService fileTempPlusService;
    @Resource
    private StaticYmlConfig staticYmlConfig;
    @Resource
    private PlatformTransactionManager pmt;
    @Resource
    private PrintLogMapper  printLogMapper;

    /**
     * 新增纸箱标签，生成标签号并打印标签。
     * @return PDF 字节数组（如果配置了模板），否则返回 null
     */
    public byte[] add(ProdTagDto dto) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = pmt.getTransaction(def);

        try {
            ProdOrder prodOrder = productionOrderPlusService.findById(dto.getProdOrderId());
            ProdTag prodTag = prodTagMapStruct.toEntity(dto);

            LoginUser userInfo = UserContext.INSTANCE.require();

            prodTag.setId(IdUtil.INSTANCE.generateId());
            prodTag.setUserId(userInfo.getUserId());
            prodTag.setUsername(userInfo.getUsername());
            prodTag.setRealName(userInfo.getRealName());

            String tagNo = sysSequenceService.generateCarton();
            prodTag.setTagNo(tagNo);
            prodTag.setCreateTime(LocalDateTime.now());
            prodTag.setDeleted(0);
            prodTagPlusService.save(prodTag);


            PrintLog printLog = new PrintLog();
            printLog.setId(IdUtil.INSTANCE.generateId());
            printLog.setNo(tagNo);
            printLog.setType(1);
            printLog.setTempId(prodOrder.getTempId());
            printLog.setUserId(userInfo.getUserId());
            printLog.setUsername(userInfo.getUsername());
            printLog.setRealName(userInfo.getRealName());
            printLogMapper.insert(printLog);

            pmt.commit(status);
            log.info("生成纸箱标签成功: prodNo={}, tagNo={}, qty={}, userId={}", prodOrder.getProdNo(), tagNo, dto.getQty(), userInfo.getUserId());

            if (prodOrder.getTempId() != null) {
                return generateExcel(prodTag, prodOrder);
            }

            return null;
        } catch (Exception e) {
            pmt.rollback(status);
            throw new WebException(e.getMessage());
        }
    }

    /**
     * 生成标签 PDF。
     * 流程：获取 tempId → 查询模板 path → 拼接完整路径 → 生成 PDF
     */
    private byte[] generateExcel(ProdTag prodTag, ProdOrder prodOrder) throws Exception {
        var fileTemp = fileTempPlusService.byId(prodOrder.getTempId());

        if (fileTemp.getPath() == null || fileTemp.getPath().isBlank()) {
            throw new WebException("打印模板文件未上传");
        }

        String templatePath = staticYmlConfig.fullTemplate() + File.separator + fileTemp.getPath();
        System.err.println("templatePath@@"+templatePath);
        Map<String, Object> data = prepareReportData(prodTag, prodOrder);

        return new ProdTagReport().generate(templatePath, data);
    }

    /**
     * 准备报表数据 Map。
     */
    private Map<String, Object> prepareReportData(ProdTag prodTag, ProdOrder prodOrder) {
        Map<String, Object> data = new HashMap<>();
        data.put("客户编号", prodOrder.getCustomerCode() != null ? prodOrder.getCustomerCode() : "");
        data.put("工单号", prodOrder.getProdNo() != null ? prodOrder.getProdNo() : "");
        data.put("客户订单号", prodOrder.getErpOrderNo() != null ? prodOrder.getErpOrderNo() : "");
        data.put("产品类别", prodOrder.getProductCategory() != null ? prodOrder.getProductCategory() : "");
        data.put("规格型号", prodOrder.getSpec() != null ? prodOrder.getSpec() : "");
        data.put("数量", String.valueOf(prodTag.getQty()));
        data.put("毛重", String.valueOf(prodTag.getGrossWeight()));
        data.put("净重", String.valueOf(prodTag.getNetWeight()));
        data.put("存货编码", prodOrder.getInventoryCode() != null ? prodOrder.getInventoryCode() : "");
        data.put("日期", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        data.put("生产单号", prodTag.getTagNo());
        return data;
    }

    /**
     * 按生产单号查询标签视图列表。
     */
    public List<VProdTag> listByProdNo(ProdTagSearch search) {
        return prodTagPlusService.listVo(search);
    }

    /**
     * 按标签号查询标签详情，并根据标志位执行占用校验。
     */
    public VProdTag findVoByTagNo(String tagNo, int flag) {
        switch (flag) {
            case 1:
                palletTagPlusService.assertNotExists(tagNo, "【" + tagNo + "】纸箱标签已打包");
                break;
            case 2:
                stockInventoryPlusService.assertNotExists(tagNo, "【" + tagNo + "】纸箱标签已入库");
                break;
            case 7:
                stockInventoryPlusService.assertNotExists(tagNo);
                break;
        }

        if (flag == 3) {
            return stockInTagPlusService.findVoByTagNo(tagNo);
        } else {
            return prodTagPlusService.findVoByTagNo(tagNo);
        }
    }

    /**
     * 补打标签，生成并返回标签 PDF。
     * @param tagId 标签主键 ID
     * @return PDF 字节数组
     */
    public byte[] reprint(String tagId) {
        VProdTag vProdTag = prodTagPlusService.findById(tagId);
        if (vProdTag == null) {
            throw new WebException("标签不存在");
        }
        if (vProdTag.getDeleted() != null && vProdTag.getDeleted() != 0) {
            throw new WebException("标签已作废，无法补打");
        }

        ProdOrder prodOrder = productionOrderPlusService.findById(vProdTag.getProdOrderId());
        if (prodOrder == null) {
            throw new WebException("关联的生产单不存在");
        }
        if (prodOrder.getTempId() == null) {
            throw new WebException("该生产单未绑定打印模板");
        }

        ProdTag prodTag = prodTagPlusService.getById(tagId);

        LoginUser userInfo = UserContext.INSTANCE.require();

        // 记录补打日志（type=1 纸箱标签）
        PrintLog printLog = new PrintLog();
        printLog.setId(IdUtil.INSTANCE.generateId());
        printLog.setNo(vProdTag.getTagNo());
        printLog.setType(1);
        printLog.setTempId(prodOrder.getTempId());
        printLog.setUserId(userInfo.getUserId());
        printLog.setUsername(userInfo.getUsername());
        printLog.setRealName(userInfo.getRealName());
        printLogMapper.insert(printLog);

        log.info("补打纸箱标签: tagNo={}, prodNo={}, userId={}", vProdTag.getTagNo(), prodOrder.getProdNo(), userInfo.getUserId());

        try {
            return generateExcel(prodTag, prodOrder);
        } catch (Exception e) {
            throw new WebException(e.getMessage());
        }
    }

    public void softDelById(String id) {
        com.gz.xg.domain.view.VProdTag tag = prodTagPlusService.findById(id);

        // TAG-7：删除前检查是否已打托或已入库，被引用时拒绝删除
        if (palletTagPlusService.findByTagNo(tag.getTagNo()) != null) {
            throw new WebException("【" + tag.getTagNo() + "】已打托，不能删除");
        }
        if (stockInTagPlusService.findByTagNo(tag.getTagNo()) != null) {
            throw new WebException("【" + tag.getTagNo() + "】已入库，不能删除");
        }

        changeDel(prodTagPlusService.getBaseMapper(), ProdTag::getDeleted, 1,
                wrapper -> {
                    wrapper.eq(ProdTag::getId, id);
                    return kotlin.Unit.INSTANCE;
                }
        );
        log.info("删除/作废纸箱标签: id={}, tagNo={}", id, tag.getTagNo());
    }
}
