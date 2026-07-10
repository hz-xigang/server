package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.TransferOrderDetailDto;
import com.gz.xg.domain.entity.TransferOrderDetail;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferOrderDetailMapStruct {

    TransferOrderDetailDto toDto(TransferOrderDetail transferOrderDetail);

    TransferOrderDetail toEntity(TransferOrderDetailDto transferOrderDetailDto);

    List<TransferOrderDetailDto> toDtoList(List<TransferOrderDetail> list);

    List<TransferOrderDetail> toEntityList(List<TransferOrderDetailDto> list);
}
