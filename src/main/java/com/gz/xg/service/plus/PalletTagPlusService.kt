package com.gz.xg.service.plus

import com.gz.xg.domain.entity.PalletTag
import com.gz.xg.mapper.PalletTagMapper
import org.springframework.stereotype.Service

@Service
class PalletTagPlusService : AbstractTagPlusService<PalletTagMapper, PalletTag>()