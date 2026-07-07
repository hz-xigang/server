package com.gz.xg.service.plus

import com.github.yulichang.base.MPJBaseServiceImpl
import com.gz.xg.domain.entity.SysUser
import com.gz.xg.mapper.SysUserMapper
import org.springframework.stereotype.Service

@Service
class SysUserPlusService : MPJBaseServiceImpl<SysUserMapper, SysUser>()
