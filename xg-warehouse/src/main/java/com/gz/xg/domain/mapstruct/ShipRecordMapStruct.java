package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.ShipRecordDto;
import com.gz.xg.domain.entity.ShipRecord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShipRecordMapStruct {

    ShipRecordMapStruct INSTANCE = Mappers.getMapper(ShipRecordMapStruct.class);

    ShipRecordDto toDto(ShipRecord entity);

    ShipRecord toEntity(ShipRecordDto dto);

    List<ShipRecordDto> toDtoList(List<ShipRecord> list);

    List<ShipRecord> toEntityList(List<ShipRecordDto> list);
}
