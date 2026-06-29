package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.StockMoveDto;
import com.gz.xg.domain.entity.StockMove;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMoveMapStruct {

    StockMoveDto toDto(StockMove stockMove);

    StockMove toEntity(StockMoveDto stockMoveDto);

    List<StockMoveDto> toDtoList(List<StockMove> list);

    List<StockMove> toEntityList(List<StockMoveDto> list);
}
