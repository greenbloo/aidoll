// build.gradle.kts

import org.springframework.boot.gradle.tasks.run.BootRun

tasks.withType<BootRun>().configureEach {
    // 1. 附加 Java Agent
    jvmArgs = listOf(
        "-javaagent:${rootDir}/ops/otel/opentelemetry-javaagent.jar",
        // 2. 启用全局自动配置
        "-Dotel.java.global-autoconfigure.enabled=true",
        // 3. 指定导出器为 OTLP
        "-Dotel.traces.exporter=otlp",
        // 4. 明确使用 gRPC 协议（默认 port 4317）
        "-Dotel.exporter.otlp.protocol=grpc",
        // 5. Collector 地址（必须包含协议前缀）
        "-Dotel.exporter.otlp.endpoint=http://localhost:4317",
        // 6. 资源属性（可选）
        "-Dotel.resource.attributes=service.name=gateway-service,service.version=${'$'}{project.version},service.namespace=aidoll,deployment.environment=dev"
    )
}

// gateway-service/build.gradle.kts —— Gateway 模块配置
plugins {
    id("org.springframework.boot")                // Spring Boot 插件
    kotlin("jvm")                                 // Kotlin/JVM 插件
    kotlin("plugin.spring")                      // Kotlin Spring 插件
}

repositories {
    mavenCentral()                                // Maven 中央仓库
}

dependencies {
    // —— BOM 管理 ——
    implementation(platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2023.0.3.2"))

    // —— 缓存 & 本地限流 ——
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("com.bucket4j:bucket4j_jdk17-core:8.14.0")

    // —— 分布式限流 (Redis + Lettuce) ——
    implementation("com.bucket4j:bucket4j_jdk17-lettuce:8.14.0")

    // —— 服务发现 & 网关 ——
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    // —— 熔断降级（CircuitBreaker + Resilience4j） ——
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

    // —— 验证 & Actuator ——
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // —— 配置中心 & YAML 解析 ——
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.0")

    // —— 日志 & 测试 ——
    runtimeOnly("ch.qos.logback:logback-classic")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // —— 注解处理 ——
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // —— Redis 客户端（Lettuce Reactive） ——
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Reactor 集成
    implementation("io.github.resilience4j:resilience4j-reactor:2.3.0")
    implementation("io.github.resilience4j:resilience4j-timelimiter:2.3.0")
    implementation("io.github.resilience4j:resilience4j-micrometer")
    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-observation")

    // —— Opentelemetry 自动注入 ——
    implementation(platform("io.opentelemetry:opentelemetry-bom:1.46.0"))
    implementation(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.12.0"))
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter:2.12.0")
}
