package com.digitalxiaoyao.gateway.infrastructure.config

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class RateLimiterConfig {

    /**
     * 基于客户端 IP 的限流 KeyResolver。
     * 跟你在 YAML 里写的 "#{@remoteAddrKeyResolver}" 对应。
     */
    @Bean
    fun remoteAddrKeyResolver(): KeyResolver =
        KeyResolver { exchange ->
            Mono.just(exchange.request.remoteAddress?.address?.hostAddress ?: "unknown")
        }
}
