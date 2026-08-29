package com.gz.xg.domain.mapstruct;


import com.gz.xg.domain.dto.PrintLogDto;
import com.gz.xg.domain.entity.PrintLog;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PrintLogMapStruct {

    PrintLogMapStruct INSTANCE = Mappers.getMapper(PrintLogMapStruct.class);

    PrintLogDto toDto(PrintLog entity);

    PrintLog toEntity(PrintLogDto dto);

    List<PrintLogDto> toDtoList(List<PrintLog> list);

    List<PrintLog> toEntityList(List<PrintLogDto> list);
}
