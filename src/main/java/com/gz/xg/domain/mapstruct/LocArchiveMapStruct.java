package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.LocArchiveDto;
import com.gz.xg.domain.entity.LocArchive;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocArchiveMapStruct {

    LocArchiveMapStruct INSTANCE = Mappers.getMapper(LocArchiveMapStruct.class);

    LocArchiveDto toDto(LocArchive entity);

    LocArchive toEntity(LocArchiveDto dto);

    List<LocArchiveDto> toDtoList(List<LocArchive> list);

    List<LocArchive> toEntityList(List<LocArchiveDto> list);

}
