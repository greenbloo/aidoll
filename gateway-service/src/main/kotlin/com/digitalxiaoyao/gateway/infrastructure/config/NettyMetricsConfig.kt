//package com.digitalxiaoyao.gateway.infrastructure.config
//
//import io.micrometer.core.instrument.binder.netty.NettyMetrics
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//
//@Configuration
//class NettyMetricsConfig {
//
//    /**
//     * 快速替代方案：强制向全局 MeterRegistry 注册 Netty 指标。
//     *
//     * 解释：
//     * ───────────────────────────────────────────────────────────────────
//     * 1. NettyMetrics.autoConfiguration { true }
//     *    - autoConfiguration(...) 会返回一个实现了 MeterBinder &
//     *      ObservationRegistryCustomizer 的对象。
//     *    - Spring Boot 在启动时发现它是 Bean，会自动注入到
//     *      Micrometer 的 MeterRegistry + ObservationRegistry。
//     *
//     * 2. lambda 返回 true ⇒ *始终* 启用；如果你想按条件
//     *    开关，可把 true 换成读取配置文件：
//     *    NettyMetrics.autoConfiguration {
//     *        environment.getProperty("management.metrics.binders.netty.enabled", Boolean::class.java, true)
//     *    }
//     */
//    @Bean
//    fun nettyMetricsBinder() =
//        NettyMetrics.autoConfiguration { true }   // “手动绑定 Registry”
//}
//
