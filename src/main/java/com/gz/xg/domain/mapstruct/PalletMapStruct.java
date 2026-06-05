package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.PalletDto;
import com.gz.xg.domain.entity.Pallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PalletMapStruct {

    PalletDto toDto(Pallet pallet);

    Pallet toEntity(PalletDto palletDto);
}
