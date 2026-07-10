package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.SysUserDto;
import com.gz.xg.domain.entity.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysUserMapStruct {

    SysUserMapStruct INSTANCE = Mappers.getMapper(SysUserMapStruct.class);

    SysUserDto toDto(SysUser entity);

    SysUser toEntity(SysUserDto dto);

    List<SysUserDto> toDtoList(List<SysUser> list);

    List<SysUser> toEntityList(List<SysUserDto> list);
}
