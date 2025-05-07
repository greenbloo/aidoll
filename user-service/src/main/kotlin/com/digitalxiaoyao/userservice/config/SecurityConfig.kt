package com.digitalxiaoyao.userservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.invoke       // ← DSL 扩展
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http {                                  // ← DSL 内部已经 build()
            authorizeExchange {
                authorize("/api/user/login", permitAll)
                authorize("/api/user/ping",  permitAll)
                authorize("/api/user/error",  permitAll)
                authorize("/api/user/slow",  permitAll)
                authorize("/api/user/flaky",  permitAll)
                authorize(anyExchange,      authenticated)
            }
            csrf { disable() }
        }                                       // 直接把 DSL 的返回值交给 Spring
}
