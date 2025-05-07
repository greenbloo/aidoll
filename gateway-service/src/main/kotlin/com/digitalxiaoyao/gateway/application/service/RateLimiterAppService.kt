package com.digitalxiaoyao.gateway.application.service

import com.digitalxiaoyao.gateway.infrastructure.config.RateLimitProperties
import com.digitalxiaoyao.gateway.infrastructure.metrics.MicrometerBucketListener
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.function.Supplier

@Service
class RateLimiterAppService(
    private val props: RateLimitProperties,
    private val proxyManager: ProxyManager<String>,
    private val meterRegistry: MeterRegistry
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * @return  true = 允许通过；false = 被限流
     */
    fun isAllowed(ip: String, path: String): Mono<Boolean> {
        // ─── 1️⃣ 解析“当前命中的限流规则” ────────────────────────────────────────────────
        val rule = props.matchRoute(path)
        val qps   = rule?.qps   ?: props.defaultQps
        val burst = rule?.burst ?: props.defaultBurst ?: qps     // ⭐ 默认桶容量

        if (log.isDebugEnabled)
            log.debug("RateLimit rule resolved  path=$path  qps=$qps  burst=$burst")

        // ─── 2️⃣ 以「ip|path」作为桶 key（同一 IP 针对每条 path 单独限速） ─────────────
        val key = "$ip|$path"

        // ─── 3️⃣ 构造 Bucket 配置（以 Supplier 方式 lazy‑create）─────────────────────
        val supplier: Supplier<BucketConfiguration> = Supplier {
            BucketConfiguration.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(burst)                 // 桶容量
                        .refillGreedy(qps, Duration.ofSeconds(1))
                        .build()
                )
                .build()
        }

        // ─── 4️⃣ 取 / 建桶，并尝试消费 1 个令牌 ───────────────────────────────────────
        val bucket = proxyManager.builder()
            .withListener(MicrometerBucketListener(meterRegistry, key))
            .build(key, supplier)

        val allowed = bucket.tryConsume(1)
        if (!allowed && log.isWarnEnabled)
            log.warn("Rate-limit BLOCKED ip=$ip path=$path")

        return Mono.just(allowed)
    }
}
