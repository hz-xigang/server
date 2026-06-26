package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.StockInDto;
import com.gz.xg.domain.entity.StockIn;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StockInMapStruct {

    StockInDto toDto(StockIn stockIn);

    StockIn toEntity(StockInDto stockInDto);
}
