package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.StockOutDto;
import com.gz.xg.domain.entity.StockOut;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockOutMapStruct {

    StockOutMapStruct INSTANCE = Mappers.getMapper(StockOutMapStruct.class);

    StockOutDto toDto(StockOut entity);

    StockOut toEntity(StockOutDto dto);

    List<StockOutDto> toDtoList(List<StockOut> list);

    List<StockOut> toEntityList(List<StockOutDto> list);
}
