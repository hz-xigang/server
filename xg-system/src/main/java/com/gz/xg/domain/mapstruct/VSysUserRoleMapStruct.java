package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.VSysUserRoleDto;
import com.gz.xg.domain.view.VSysUserRole;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VSysUserRoleMapStruct {

    VSysUserRoleMapStruct INSTANCE = Mappers.getMapper(VSysUserRoleMapStruct.class);

    VSysUserRoleDto toDto(VSysUserRole entity);

    VSysUserRole toEntity(VSysUserRoleDto dto);

    List<VSysUserRoleDto> toDtoList(List<VSysUserRole> list);

    List<VSysUserRole> toEntityList(List<VSysUserRoleDto> list);
}
