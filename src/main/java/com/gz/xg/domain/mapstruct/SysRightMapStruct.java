package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.SysRightDto;
import com.gz.xg.domain.entity.SysRight;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysRightMapStruct {

    SysRightMapStruct INSTANCE = Mappers.getMapper(SysRightMapStruct.class);

    SysRightDto toDto(SysRight entity);

    SysRight toEntity(SysRightDto dto);

    List<SysRightDto> toDtoList(List<SysRight> list);

    List<SysRight> toEntityList(List<SysRightDto> list);
}
