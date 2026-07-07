package com.gz.xg.service.plus

import com.gz.xg.domain.entity.PalletTag
import com.gz.xg.mapper.PalletTagMapper
import org.springframework.stereotype.Service

/**
 * 托盘标签关联底层服务。
 */
@Service
class PalletTagPlusService : AbstractTagPlusService<PalletTagMapper, PalletTag>()
