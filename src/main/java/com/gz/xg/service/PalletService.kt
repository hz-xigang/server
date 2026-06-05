package com.gz.xg.service

import com.gz.xg.domain.entity.Pallet
import com.gz.xg.service.plus.PalletPlusService
import org.springframework.stereotype.Service

@Service
open class PalletService(
    private val palletPlusService: PalletPlusService
) {

    fun findById(id: String): Pallet? {
        return palletPlusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return palletPlusService.removeById(id)
    }

    fun updateById(entity: Pallet): Boolean {
        return palletPlusService.updateById(entity)
    }
}
