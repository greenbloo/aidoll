// user-service/build.gradle.kts
plugins {
    id("org.springframework.boot")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2023.0.3.2")) // BOM :contentReference[oaicite:2]{index=2}

    /* --- WebFlux & Reactive --- */
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    /* --- Spring Security 基础 + Config（千万别排除 spring-security-web） --- */
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-config")

    /* --- Servlet API 仅为满足类加载，标记为 compileOnly --- */
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")



    /* 其余保持不变 */
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
