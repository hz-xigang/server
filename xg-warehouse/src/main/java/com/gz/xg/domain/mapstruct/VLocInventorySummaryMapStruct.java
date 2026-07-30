package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.VLocInventorySummaryDto;
import com.gz.xg.domain.view.VLocInventorySummary;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VLocInventorySummaryMapStruct {

    VLocInventorySummaryDto toDto(VLocInventorySummary entity);

    VLocInventorySummary toEntity(VLocInventorySummaryDto dto);

    List<VLocInventorySummaryDto> toDtoList(List<VLocInventorySummary> list);

    List<VLocInventorySummary> toEntityList(List<VLocInventorySummaryDto> list);
}
