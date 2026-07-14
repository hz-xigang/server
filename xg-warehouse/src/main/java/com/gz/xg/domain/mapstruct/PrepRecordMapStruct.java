package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.PrepRecordDto;
import com.gz.xg.domain.entity.PrepOrderDetail;
import com.gz.xg.domain.entity.PrepRecord;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PrepRecordMapStruct {

    PrepRecordDto toDto(PrepRecord prepRecord);

    PrepRecord toEntity(PrepRecordDto prepRecordDto);

    PrepRecord detailToEntity(PrepOrderDetail detail);

    List<PrepRecordDto> toDtoList(List<PrepRecord> list);

    List<PrepRecord> toEntityList(List<PrepRecordDto> list);
}
