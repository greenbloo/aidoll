package com.digitalxiaoyao.gateway.infrastructure.metrics

import io.github.bucket4j.BucketListener
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry

/**
 * 仅统计“已消费 / 被拒绝”令牌数。
 * 其余回调（onParked / onInterrupted）目前仅占位，方便将来扩展。
 *
 * Prometheus 指标：
 *   bucket4j_consumed_tokens_total{bucket="<key>"}
 *   bucket4j_rejected_tokens_total{bucket="<key>"}
 */
class MicrometerBucketListener(
    registry: MeterRegistry,
    bucketName: String
) : BucketListener {

    private val consumed: Counter = Counter
        .builder("bucket4j_consumed_tokens_total")
        .tag("bucket", bucketName)
        .register(registry)           // Micrometer Counter 用法官方示例 :contentReference[oaicite:2]{index=2}

    private val rejected: Counter = Counter
        .builder("bucket4j_rejected_tokens_total")
        .tag("bucket", bucketName)
        .register(registry)

    /* --- 必须实现的 4 个抽象方法 --- */

    override fun onConsumed(tokens: Long) =
        consumed.increment(tokens.toDouble())

    override fun onRejected(tokens: Long) =
        rejected.increment(tokens.toDouble())

    /** 8.4.0+ 新增：线程实际被 `park` 等待回填时回调 :contentReference[oaicite:3]{index=3} */
    override fun onParked(nanos: Long) {
        /* 如需统计等待时长，可在此实现 Counter/Summary；此处先留空 */
    }

    /** 当 BlockingBucket 调用被线程中断时触发，默认忽略即可 */
    override fun onInterrupted(e: InterruptedException?) { /* no‑op */ }
    override fun onDelayed(nanos: Long) {
        TODO("Not yet implemented")
    }
}
