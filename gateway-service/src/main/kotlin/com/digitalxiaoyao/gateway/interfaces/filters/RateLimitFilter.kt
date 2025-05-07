package com.digitalxiaoyao.gateway.interfaces.filters

import com.digitalxiaoyao.gateway.application.service.RateLimiterAppService
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * 限流过滤器：只做“协议适配”，真正的令牌桶逻辑在应用层的 [RateLimiterAppService] 中。
 *
 * 执行顺序：在日志过滤器之后，路由之前 —— 取 PreLoggingFilter +1。
 */
@Component
class RateLimitFilter(
    // 构造注入应用服务，方便单测 mock
    private val rateLimiter: RateLimiterAppService
) : GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        val ip = exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"

        // 询问应用层：这一次请求是否被允许？
        return rateLimiter.isAllowed(ip, path)
            .flatMap { allowed ->
                if (allowed) {
                    chain.filter(exchange)
                } else {
                    log.warn("Rate-limit BLOCKED ip=$ip path=$path")
                    exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
                    exchange.response.setComplete()
                }
            }
    }
}




