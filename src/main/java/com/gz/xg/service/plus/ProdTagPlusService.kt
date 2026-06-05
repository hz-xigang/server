package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.mapper.ProdTagMapper
import org.springframework.stereotype.Service

@Service
open class ProdTagPlusService : ServiceImpl<ProdTagMapper, ProdTag>()
