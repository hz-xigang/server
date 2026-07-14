package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.PrepRecord
import com.gz.xg.mapper.PrepRecordMapper
import org.springframework.stereotype.Service

@Service
class PrepRecordPlusService : ServiceImpl<PrepRecordMapper, PrepRecord>()
