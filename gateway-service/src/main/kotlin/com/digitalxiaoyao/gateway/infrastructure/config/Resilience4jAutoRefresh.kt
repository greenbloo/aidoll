package com.digitalxiaoyao.gateway.infrastructure.config

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import org.slf4j.LoggerFactory
import org.springframework.cloud.context.environment.EnvironmentChangeEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.time.Duration

@Configuration
class Resilience4jAutoRefresh(
    private val env: Environment,
    private val timeLimiterRegistry: TimeLimiterRegistry,
    private val circuitBreakerRegistry: CircuitBreakerRegistry
) {

    companion object {
        private val log = LoggerFactory.getLogger(Resilience4jAutoRefresh::class.java)

        /* —— 监听的配置 Key —— */
        private const val TL_TIMEOUT_KEY =
            "resilience4j.timelimiter.configs.default.timeoutDuration"
        private const val CB_OPEN_WAIT_KEY =
            "resilience4j.circuitbreaker.configs.default.waitDurationInOpenState"

        /* —— 跟 yml 中保持一致的实例名 —— */
        private const val INSTANCE_NAME = "defaultCB"
    }

    /** 监听 Nacos 触发的 Environment 刷新事件 */
    @Bean
    fun resilience4jRefreshListener(): ApplicationListener<EnvironmentChangeEvent> =
        ApplicationListener { event ->
            if (event.keys.contains(TL_TIMEOUT_KEY)) refreshTimeLimiter()
            if (event.keys.contains(CB_OPEN_WAIT_KEY)) refreshCircuitBreaker()
        }

    /** 热更新 TimeLimiter */
    private fun refreshTimeLimiter() {
        val newDuration = env.getProperty(TL_TIMEOUT_KEY)
            ?.takeIf { it.isNotBlank() }
            ?.let { Duration.parse("PT${it.uppercase()}") }
            ?: return

        /* 1️⃣ 先删除旧实例（按名称） */
        timeLimiterRegistry.remove(INSTANCE_NAME)

        /* 2️⃣ 创建新配置 & 注册 */
        val dynamicCfg = TimeLimiterConfig
            .from(timeLimiterRegistry.defaultConfig)
            .timeoutDuration(newDuration)
            .build()

        timeLimiterRegistry.addConfiguration("dynamic-default-tl", dynamicCfg)
        timeLimiterRegistry.timeLimiter(INSTANCE_NAME, "dynamic-default-tl")

        log.info("TimeLimiter[{}] 已重新创建，timeout = {}", INSTANCE_NAME, newDuration)
    }

    /** 热更新 CircuitBreaker（演示只更新 waitDurationInOpenState） */
    private fun refreshCircuitBreaker() {
        val newWait = env.getProperty(CB_OPEN_WAIT_KEY)
            ?.takeIf { it.isNotBlank() }
            ?.let { Duration.parse("PT${it.uppercase()}") }
            ?: return

        /* 1️⃣ 删除旧实例 */
        circuitBreakerRegistry.remove(INSTANCE_NAME)

        /* 2️⃣ 创建新配置 & 注册 */
        val dynamicCfg = CircuitBreakerConfig
            .from(circuitBreakerRegistry.defaultConfig)
            .waitDurationInOpenState(newWait)
            .build()

        circuitBreakerRegistry.addConfiguration("dynamic-default-cb", dynamicCfg)
        circuitBreakerRegistry.circuitBreaker(INSTANCE_NAME, "dynamic-default-cb")

        log.info(
            "CircuitBreaker[{}] 已重新创建，waitDurationInOpenState = {}",
            INSTANCE_NAME, newWait
        )
    }
}
