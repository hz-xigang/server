package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.VRoleRightDto;
import com.gz.xg.domain.view.VRoleRight;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VRoleRightMapStruct {

    VRoleRightMapStruct INSTANCE = Mappers.getMapper(VRoleRightMapStruct.class);

    VRoleRightDto toDto(VRoleRight entity);

    VRoleRight toEntity(VRoleRightDto dto);

    List<VRoleRightDto> toDtoList(List<VRoleRight> list);

    List<VRoleRight> toEntityList(List<VRoleRightDto> list);
}
