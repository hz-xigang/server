package com.gz.xg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.gz.xg.domain.dto.ProdTagDto;
import com.gz.xg.domain.entity.ProductionOrder;
import com.gz.xg.domain.enums.SequenceType;
import com.gz.xg.service.ProdTagService;
import com.gz.xg.service.ProductionOrderService;
import com.gz.xg.service.SysSequenceService;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class XgApplicationTests {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ProductionOrderService productionOrderService;

    @Resource
    private SysSequenceService sysSequenceService;

    @Resource
    private ProdTagService  prodTagService;

	@Test
	void contextLoads() throws JsonProcessingException {
       /* String s = sysSequenceService.generateSequence(SequenceType.PRODUCTION_ORDER, "20260602");
        System.err.println(s);*/

        ProductionOrder pg = productionOrderService.findByProgNo("PO20260001");
        System.err.println(objectMapper.writeValueAsString(pg));
    }

    @Test
    void prodTagAdd(){
        ProdTagDto dto = new ProdTagDto();
        dto.setProdOrderId("ID20260009");
        dto.setQty(20);
        dto.setNetWeight(new BigDecimal("0.01"));
        dto.setGrossWeight(new BigDecimal("0.02"));
        prodTagService.add(dto);
        System.err.println("插入完成");
    }

}
