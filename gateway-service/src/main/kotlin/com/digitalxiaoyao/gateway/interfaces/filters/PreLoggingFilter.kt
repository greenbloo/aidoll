

package com.digitalxiaoyao.gateway.interfaces.filters

import com.digitalxiaoyao.gateway.infrastructure.config.RateLimitProperties
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * 全局预过滤器：仅负责
 * 1. 生成 / 透传 TraceId
 * 2. 记录 TraceId + IP + 方法 + 路径
 * 3. IP 白名单校验
 */
@Component
class PreLoggingFilter(
    private val properties: RateLimitProperties
) : GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(javaClass)

    /** 保持最高优先级，在限流、路由前执行 */
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val traceId = request.headers.getFirst("X-Trace-Id") ?: UUID.randomUUID().toString()
        val clientIp = request.remoteAddress?.address?.hostAddress ?: "unknown"

        log.info("TraceId={} IP={} {} {}", traceId, clientIp, request.method, request.uri.path)

        // IP 白名单校验
        if (clientIp !in properties.whitelistIps) {
            exchange.response.statusCode = HttpStatus.FORBIDDEN
            return exchange.response.setComplete()
        }

        // 透传 TraceId
        val mutated = exchange.mutate()
            .request(request.mutate().header("X-Trace-Id", traceId).build())
            .build()

        return chain.filter(mutated)
    }
}
