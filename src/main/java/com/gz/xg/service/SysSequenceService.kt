package com.gz.xg.service

import com.gz.xg.domain.entity.SysSequence
import com.gz.xg.domain.enums.SequenceType
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.SysSequencePlusService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat
import java.util.Date

@Service
open class SysSequenceService(
    private val plusService: SysSequencePlusService
) {


    /**
     * 生产纸箱标签
     */
    fun generateCarton() : String{
        val sdf = SimpleDateFormat("yyMMdd")
        val format = sdf.format(Date())
        return generateSequence(SequenceType.CARTON_LABEL,format)

    }


    fun generateSequence(sequenceType: SequenceType, value: String): String {
        return generateSequences(sequenceType, value, 1).first()
    }


    fun generateSequences(sequenceType: SequenceType, value: String, count: Int): List<String> {
        if (count <= 0) {
            throw WebException("count must be greater than 0")
        }

        val sequence = getSequenceConfig(sequenceType)
        val currentValue = sequence.currentValue
        val sequenceNumbers = (1..count).map { offset ->
            val nextValue = currentValue + offset
            buildSequenceNo(sequence, value, nextValue)
        }

        sequence.currentValue = currentValue + count
        if (!plusService.updateById(sequence)) {
            throw WebException("Failed to update sequence current value")
        }

        return sequenceNumbers
    }

    private fun getSequenceConfig(sequenceType: SequenceType): SysSequence {
        val sequence = plusService.query()
            .eq("bizType", sequenceType.code)
            .one()
            ?: throw WebException("Sequence config not found: ${sequenceType.name}")

        if (sequence.status != true) {
            throw WebException("Sequence config is disabled: ${sequenceType.name}")
        }

        return sequence
    }

    private fun buildSequenceNo(sequence: SysSequence, value: String, currentValue: Int): String {
        val prefix = sequence.prefix ?: ""
        return prefix + value + formatCurrentValue(sequence, currentValue)
    }

    private fun formatCurrentValue(sequence: SysSequence, currentValue: Int): String {
        val seqLength = sequence.seqLength ?: 0
        return if (seqLength > 0) {
            currentValue.toString().padStart(seqLength, '0')
        } else {
            currentValue.toString()
        }
    }
}
