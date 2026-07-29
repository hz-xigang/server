package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.FileTemp
import com.gz.xg.mapper.FileTempMapper
import org.springframework.stereotype.Service

@Service
class FileTempPlusService : ServiceImpl<FileTempMapper, FileTemp>()
