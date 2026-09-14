package com.gz.xg.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * 细粒度条码并发锁工具类（基于 JVM 内存分段锁）。
 * 针对入库/出库/移库等业务中同一条码的并发重复提交进行互斥保护。
 */
object BarcodeLockUtil {

    private class RefCountedLock {
        val lock = ReentrantLock()
        var refCount = 0
    }

    private val locks = ConcurrentHashMap<String, RefCountedLock>()

    /**
     * 尝试对一组条码加锁（自动按字典序升序排序，严格避免死锁）。
     *
     * @param barcodes 需要加锁的条码列表
     * @return Pair(是否全部加锁成功, 冲突的条码或 null)
     */
    fun tryAcquire(barcodes: Collection<String>): Pair<Boolean, String?> {
        val sortedList = barcodes.filter { it.isNotBlank() }.distinct().sorted()
        if (sortedList.isEmpty()) {
            return Pair(true, null)
        }

        val acquiredList = mutableListOf<String>()

        for (barcode in sortedList) {
            val refCountedLock = synchronized(locks) {
                locks.compute(barcode) { _, existing ->
                    val entry = existing ?: RefCountedLock()
                    entry.refCount++
                    entry
                }!!
            }

            val locked = refCountedLock.lock.tryLock()
            if (locked) {
                acquiredList.add(barcode)
            } else {
                // 竞争失败：扣减当前条码引用计数，并回退释放前面已获取的锁
                synchronized(locks) {
                    refCountedLock.refCount--
                    if (refCountedLock.refCount <= 0) {
                        locks.remove(barcode)
                    }
                }
                release(acquiredList)
                return Pair(false, barcode)
            }
        }

        return Pair(true, null)
    }

    /**
     * 释放指定条码列表的锁，并自动清理引用计数为 0 的锁对象以防内存泄漏。
     */
    fun release(barcodes: Collection<String>) {
        val distinctList = barcodes.filter { it.isNotBlank() }.distinct()
        for (barcode in distinctList) {
            synchronized(locks) {
                val refCountedLock = locks[barcode]
                if (refCountedLock != null) {
                    if (refCountedLock.lock.isHeldByCurrentThread) {
                        refCountedLock.lock.unlock()
                    }
                    refCountedLock.refCount--
                    if (refCountedLock.refCount <= 0) {
                        locks.remove(barcode)
                    }
                }
            }
        }
    }

    /**
     * 在条码锁保护下执行代码块。
     * 若条码正在被其他线程处理，则抛出带有具体冲突条码的异常。
     */
    inline fun <T> withLock(barcodes: Collection<String>, onConflict: (String) -> Nothing, block: () -> T): T {
        val (acquired, conflictBarcode) = tryAcquire(barcodes)
        if (!acquired) {
            onConflict(conflictBarcode ?: "条码")
        }
        try {
            return block()
        } finally {
            release(barcodes)
        }
    }
}
