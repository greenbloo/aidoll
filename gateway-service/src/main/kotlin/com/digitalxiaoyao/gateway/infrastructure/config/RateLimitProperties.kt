package com.digitalxiaoyao.gateway.infrastructure.config

import jakarta.validation.constraints.Positive
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@RefreshScope
@Component
@Validated
@ConfigurationProperties("gateway.rate-limit")
class RateLimitProperties {

    /** 热刷版本，用于换桶 */
    var revision: String = System.currentTimeMillis().toString()

    /** 全局默认 QPS（≥1） */
    @field:Positive
    var defaultQps: Long = 1

    /** 全局默认桶容量（≥ defaultQps） */
    @field:Positive
    var defaultBurst: Long = defaultQps  // ★ 新增

    /** IP 白名单 */
    var whitelistIps: List<String> = listOf("127.0.0.1")

    /** 路由级限流 */
    @NestedConfigurationProperty
    var routes: Map<String, RouteLimit> = emptyMap()

    class RouteLimit {
        @field:Positive
        var qps: Long = 1
        @field:Positive
        var burst: Long? = null
    }

    fun isWhitelisted(ip: String) = ip in whitelistIps
    fun matchRoute(path: String) =
        routes.entries
            .firstOrNull { (pattern, _) ->
                SIMPLE_MATCHER.match(pattern, path)
            }?.value

    companion object {
        private val SIMPLE_MATCHER = org.springframework.util.AntPathMatcher()
    }
}
