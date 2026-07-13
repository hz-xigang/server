package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.TransferRecord
import com.gz.xg.mapper.TransferRecordMapper
import org.springframework.stereotype.Service

@Service
class TransferRecordPlusService : ServiceImpl<TransferRecordMapper, TransferRecord>()
