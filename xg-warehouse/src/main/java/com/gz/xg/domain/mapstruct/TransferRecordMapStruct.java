package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.TransferRecordDto;
import com.gz.xg.domain.entity.TransferOrderDetail;
import com.gz.xg.domain.entity.TransferRecord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferRecordMapStruct {

    TransferRecordMapStruct INSTANCE = Mappers.getMapper(TransferRecordMapStruct.class);

    TransferRecordDto toDto(TransferRecord entity);

    TransferRecord toEntity(TransferRecordDto dto);

    TransferRecord detailToEntity(TransferOrderDetail detail);

    List<TransferRecordDto> toDtoList(List<TransferRecord> list);

    List<TransferRecord> toEntityList(List<TransferRecordDto> list);
}
