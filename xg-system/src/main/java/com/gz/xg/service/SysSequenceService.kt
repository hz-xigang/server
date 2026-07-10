package com.gz.xg.service

import com.gz.xg.domain.entity.SysSequence
import com.gz.xg.domain.enums.SequenceType
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.SysSequencePlusService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * 流水号服务，负责各类业务单号和标签号的生成。
 */
@Service
 class SysSequenceService(
    private val plusService: SysSequencePlusService
) {

    /**
     * 生成纸箱标签号。
     */
    fun generateCarton() = generateByDate(SequenceType.CARTON_LABEL) +
            Random.nextInt(0, 100).toString().padStart(2, '0')

    /**
     * 生成托盘号。
     */
    fun generatePallet() = generateByDate(SequenceType.PALLET)

    /**
     * 生成入库单号。
     */
    fun generateStockIn() = generateByDate(SequenceType.STOCK_IN,"yyyyMMdd")

    /**
     * 生成移库单号。
     */
    fun generateMoves() = generateByDate(SequenceType.MOVE_STOCK,"yyyyMMdd")

    /**
     * 生成备料单号。
     */
    fun generatePrep() = generateByDate(SequenceType.PREP_MATERIAL, "yyyyMMdd")

    /**
     * 生成单个流水号。
     */
    fun generateSequence(sequenceType: SequenceType, value: String): String {
        return generateSequences(sequenceType, value, 1).first()
    }

    /**
     * 按数量批量生成流水号，并递增当前序列值。
     */
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

    /**
     * 查询指定业务类型的流水号配置，并校验启用状态。
     */
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

    /**
     * 按配置拼装最终流水号。
     */
    private fun buildSequenceNo(sequence: SysSequence, value: String, currentValue: Int): String {
        val prefix = sequence.prefix ?: ""
        return prefix + value + formatCurrentValue(sequence, currentValue)
    }

    /**
     * 按位数格式化当前流水值。
     */
    private fun formatCurrentValue(sequence: SysSequence, currentValue: Int): String {
        val seqLength = sequence.seqLength ?: 0
        return if (seqLength > 0) {
            currentValue.toString().padStart(seqLength, '0')
        } else {
            currentValue.toString()
        }
    }

    /**
     * 按日期前缀生成流水号。
     */
    private fun generateByDate(
        type: SequenceType,
        pattern: String = "yyMMdd"
    ): String {

        val prefix = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern(pattern))

        return generateSequence(type, prefix)
    }
}