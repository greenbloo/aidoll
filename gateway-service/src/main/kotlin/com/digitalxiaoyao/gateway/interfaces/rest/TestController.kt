package com.digitalxiaoyao.gateway.interfaces.rest


import io.opentelemetry.api.GlobalOpenTelemetry
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/test-trace")
    fun testTrace(): String {
        val tracer = GlobalOpenTelemetry.getTracer("manual-test")
        val span = tracer.spanBuilder("manual-span").startSpan()
        span.end()
        return "trace created"
    }
}
