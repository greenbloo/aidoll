package com.digitalxiaoyao.userservice.presentation

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

@RestController
@RequestMapping("/api/user")
class UserAuthController {

    private val flakyCount = AtomicInteger(0)

    /** 健康探测：curl http://localhost:8080/api/user/ping */
    @GetMapping("/ping")
    fun ping(): Mono<String> = Mono.just("pong")

    /** 模拟后端错误：第一次及每次调用都返回 500 */
    @GetMapping("/error")
    fun error(): Mono<String> =
        Mono.error(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error"))

    /** 模拟慢响应：睡眠 10 秒后返回 */
    @GetMapping("/slow")
    fun slow(): Mono<String> =
        Mono.delay(Duration.ofSeconds(10))
            .map { "slow" }

    /**
     * 模拟间歇性失败：
     * - 第一次调用返回 500
     * - 第二次及之后返回 200 + "flaky"
     */
    @GetMapping("/flaky")
    fun flaky(): Mono<String> =
        Mono.defer {
            if (flakyCount.incrementAndGet() == 1) {
                Mono.error(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "fail once"))
            } else {
                Mono.just("flaky")
            }
        }

    /**
     * 登录示例：
     * curl -X POST http://localhost:8080/api/user/login \
     *      -H "Content-Type:application/json" \
     *      -d '{"username":"demo","password":"123"}'
     *
     * 目前直接 Mock，后续替换为 application.service 调用
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@Valid @RequestBody dto: LoginDto): Mono<AuthResultDto> =
        Mono.just(
            AuthResultDto(
                token    = "mock-jwt-token-for-${dto.username}",
                userId   = 1L,
                username = dto.username
            )
        )
}

