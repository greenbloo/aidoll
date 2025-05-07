package com.digitalxiaoyao.gateway.infrastructure.config

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * 使用 Bucket4j 8.14.0 最新 API（casBasedBuilder）构建分布式限流 ProxyManager。
 * 依赖 spring-data-redis 提供的配置，而非旧版 builderFor。
 */
@Configuration
class Bucket4jRedisConfig(
    private val redisProps: RedisProperties
) {

    /** RedisClient 与 Spring 生命周期绑定；关闭应用时自动 shutdown。 */
    @Bean(destroyMethod = "shutdown")
    fun redisClient(): RedisClient {
        val uri = RedisURI.Builder.redis(redisProps.host, redisProps.port)
            .apply {
                redisProps.password?.takeIf { it.isNotBlank() }?.let { withPassword(it) }
                withDatabase(redisProps.database)
            }.build()
        return RedisClient.create(uri)
    }

    /** 保持一个 `StatefulRedisConnection<String, ByteArray>` 供 Bucket4j 使用。 */
    @Bean(destroyMethod = "close")
    fun redisConnection(client: RedisClient): StatefulRedisConnection<String, ByteArray> {
        val codec: RedisCodec<String, ByteArray> =
            RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        return client.connect(codec)
    }

    /**
     * ProxyManager<String> —— 分布式 Bucket 工厂
     * - 使用官方推荐的 casBasedBuilder（非已弃用 builderFor）
     * - 设定基于「写后过期」策略，TTL 为 60 秒；可按需调整
     */
    @Bean
    fun proxyManager(connection: StatefulRedisConnection<String, ByteArray>): ProxyManager<String> =
        Bucket4jLettuce
            .casBasedBuilder(connection)                       // 最新入口方法﻿:contentReference[oaicite:0]{index=0}
            .expirationAfterWrite(                             // 来自 AbstractProxyManagerBuilder﻿:contentReference[oaicite:1]{index=1}
                ExpirationAfterWriteStrategy
                    .basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(60))
            )
            .build()
}
