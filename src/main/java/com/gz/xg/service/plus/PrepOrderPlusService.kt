package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.PrepOrder
import com.gz.xg.mapper.PrepOrderMapper
import org.springframework.stereotype.Service

@Service
class PrepOrderPlusService : ServiceImpl<PrepOrderMapper, PrepOrder>()
