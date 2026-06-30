package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.ShipOrderDetailDto;
import com.gz.xg.domain.entity.ShipOrderDetail;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ShipOrderDetailMapStruct {

    ShipOrderDetailDto toDto(ShipOrderDetail shipOrderDetail);

    ShipOrderDetail toEntity(ShipOrderDetailDto shipOrderDetailDto);

    List<ShipOrderDetailDto> toDtoList(List<ShipOrderDetail> list);

    List<ShipOrderDetail> toEntityList(List<ShipOrderDetailDto> list);
}
