package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.SysSequence
import com.gz.xg.mapper.SysSequenceMapper
import org.springframework.stereotype.Service

@Service
open class SysSequencePlusService : ServiceImpl<SysSequenceMapper, SysSequence>()
