package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.PrepOrderDetailDto;
import com.gz.xg.domain.entity.PrepOrderDetail;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PrepOrderDetailMapStruct {

    PrepOrderDetailDto toDto(PrepOrderDetail prepOrderDetail);

    PrepOrderDetail toEntity(PrepOrderDetailDto prepOrderDetailDto);

    List<PrepOrderDetailDto> toDtoList(List<PrepOrderDetail> list);

    List<PrepOrderDetail> toEntityList(List<PrepOrderDetailDto> list);
}
