package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.SysRole
import com.gz.xg.mapper.SysRoleMapper
import org.springframework.stereotype.Service

@Service
class SysRolePlusService : ServiceImpl<SysRoleMapper, SysRole>()
