package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.SysRight
import com.gz.xg.mapper.SysRightMapper
import org.springframework.stereotype.Service

@Service
class SysRightPlusService : ServiceImpl<SysRightMapper, SysRight>()
