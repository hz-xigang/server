package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.SysRight
import com.gz.xg.mapper.SysRightMapper
import org.springframework.stereotype.Service

@Service
class SysRightPlusService : ServiceImpl<SysRightMapper, SysRight>() {

    /**
     * PDA 权限集合（type=4）
     */
    fun pdaList() : List<SysRight> {
        val wrapper = LambdaQueryWrapper<SysRight>()
            .eq(SysRight::getDeleted, 0)
            .eq(SysRight::getType, 4)

        return this.list(wrapper)
    }
}
