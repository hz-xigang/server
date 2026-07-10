package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.ProdTagDto;
import com.gz.xg.domain.entity.ProdTag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProdTagMapStruct {

    ProdTagDto toDto(ProdTag prodTag);

    ProdTag toEntity(ProdTagDto prodTagDto);
}
