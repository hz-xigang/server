package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.gz.xg.domain.entity.SysSequence
import org.apache.ibatis.annotations.Mapper

@Mapper
interface SysSequenceMapper : BaseMapper<SysSequence>
