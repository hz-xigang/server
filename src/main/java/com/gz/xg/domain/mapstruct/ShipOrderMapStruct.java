package com.gz.xg.domain.mapstruct;
import com.gz.xg.domain.dto.ShipOrderDto;
import com.gz.xg.domain.entity.ShipOrder;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ShipOrderMapStruct {
    ShipOrderDto toDto(ShipOrder shipOrder);
    ShipOrder toEntity(ShipOrderDto shipOrderDto);
    List<ShipOrderDto> toDtoList(List<ShipOrder> list);
    List<ShipOrder> toEntityList(List<ShipOrderDto> list);
}