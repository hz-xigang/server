package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.TransferOrderDto;
import com.gz.xg.domain.entity.TransferOrder;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferOrderMapStruct {

    TransferOrderDto toDto(TransferOrder transferOrder);

    TransferOrder toEntity(TransferOrderDto transferOrderDto);

    List<TransferOrderDto> toDtoList(List<TransferOrder> list);

    List<TransferOrder> toEntityList(List<TransferOrderDto> list);
}
