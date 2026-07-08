package com.gz.xg;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gz.xg.domain.dto.ProdTagDto;
import com.gz.xg.domain.dto.SysUserDto;
import com.gz.xg.domain.entity.ProdOrder;
import com.gz.xg.service.ProdTagService;
import com.gz.xg.service.ProdOrderService;
import com.gz.xg.service.SysSequenceService;
import com.gz.xg.service.SysUserService;
import com.gz.xg.service.plus.ProductionOrderPlusService;
import jakarta.annotation.Resource;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class XgApplicationTests {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ProdOrderService prodOrderService;

    @Resource
    private SysSequenceService sysSequenceService;

    @Resource
    private ProdTagService  prodTagService;

    @Resource
    private ProductionOrderPlusService plusService;

	@Test
	void contextLoads() throws JsonProcessingException {
       /* String s = sysSequenceService.generateSequence(SequenceType.PRODUCTION_ORDER, "20260602");
        System.err.println(s);*/
        int current = 1;
        int size = 15;
        Page<ProdOrder> page =  new Page<ProdOrder>(current, size);

        QueryWrapper<ProdOrder> wrapper = new QueryWrapper<>();

        Page<ProdOrder> orderPage = plusService.page(page, wrapper);

    }

    @Resource
    private SysUserService sysUserService;

    @Test
    void prodTagAdd(){
        val dto = new SysUserDto();
        dto.setUsername("admin");
        dto.setPwd("123456");
        dto.setRealName("admin");

        sysUserService.add(dto);
        System.err.println("插入成功");
    }

}
