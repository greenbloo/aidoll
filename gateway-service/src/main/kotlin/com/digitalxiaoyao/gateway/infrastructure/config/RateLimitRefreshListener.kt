package com.digitalxiaoyao.gateway.infrastructure.config

import org.springframework.cloud.context.environment.EnvironmentChangeEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * 监听 Spring Cloud Context 的热刷新事件，
 * 只要 gateway.rate‑limit* 有变更，就迭代 revision，
 * 让后续请求命中新桶，从而立即使用新 QPS。
 */
@Component
class RateLimitRefreshListener(
    private val props: RateLimitProperties
) {

    @EventListener
    fun onRefresh(evt: EnvironmentChangeEvent) {
        if (evt.keys.any { it.startsWith("gateway.rate-limit") }) {
            props.revision = System.currentTimeMillis().toString()  // ★ 换版本（= 换桶 key）
        }
    }
}
