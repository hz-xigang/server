package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.Pallet
import com.gz.xg.mapper.PalletMapper
import org.springframework.stereotype.Service

@Service
open class PalletPlusService : ServiceImpl<PalletMapper, Pallet>()
