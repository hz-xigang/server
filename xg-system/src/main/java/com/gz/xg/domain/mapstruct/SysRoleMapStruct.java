package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.SysRoleDto;
import com.gz.xg.domain.entity.SysRole;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysRoleMapStruct {

    SysRoleMapStruct INSTANCE = Mappers.getMapper(SysRoleMapStruct.class);

    SysRoleDto toDto(SysRole entity);

    SysRole toEntity(SysRoleDto dto);

    List<SysRoleDto> toDtoList(List<SysRole> list);

    List<SysRole> toEntityList(List<SysRoleDto> list);
}
