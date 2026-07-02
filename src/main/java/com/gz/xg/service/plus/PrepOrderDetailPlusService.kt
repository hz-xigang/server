package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.PrepOrderDetail
import com.gz.xg.mapper.PrepOrderDetailMapper
import org.springframework.stereotype.Service

@Service
open class PrepOrderDetailPlusService : ServiceImpl<PrepOrderDetailMapper, PrepOrderDetail>()
