package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.FileTempDto;
import com.gz.xg.domain.entity.FileTemp;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FileTempMapStruct {

    FileTempMapStruct INSTANCE = Mappers.getMapper(FileTempMapStruct.class);

    FileTempDto toDto(FileTemp entity);

    FileTemp toEntity(FileTempDto dto);

    List<FileTempDto> toDtoList(List<FileTemp> list);

    List<FileTemp> toEntityList(List<FileTempDto> list);
}
