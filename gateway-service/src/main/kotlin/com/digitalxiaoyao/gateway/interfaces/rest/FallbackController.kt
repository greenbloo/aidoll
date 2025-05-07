package com.digitalxiaoyao.gateway.interfaces.rest

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * 全局兜底控制器：
 * 当任一路由因下游服务不可用、超时等触发 CircuitBreaker 时，
 * Gateway 会 forward:/fallback 到这里，返回统一回退 JSON。
 */
@RestController
class FallbackController {

    private val log = LoggerFactory.getLogger(FallbackController::class.java)

    @RequestMapping("/fallback", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun fallback(): Mono<Map<String, Any>> {
        log.info(">>> 已进入 fallback 处理逻辑")
        val body = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status"    to 503,
            "message"   to "Service is temporarily unavailable. Please try again later."
        )
        return Mono.just(body)
    }
}
