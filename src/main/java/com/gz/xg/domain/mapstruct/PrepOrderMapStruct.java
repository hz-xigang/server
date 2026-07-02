package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.PrepOrderDto;
import com.gz.xg.domain.entity.PrepOrder;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PrepOrderMapStruct {

    PrepOrderDto toDto(PrepOrder prepOrder);

    PrepOrder toEntity(PrepOrderDto prepOrderDto);

    List<PrepOrderDto> toDtoList(List<PrepOrder> list);

    List<PrepOrder> toEntityList(List<PrepOrderDto> list);
}
