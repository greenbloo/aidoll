// 根 build.gradle.kts  —— aidoll
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.4.5" apply false
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "1.9.25" apply false
	kotlin("plugin.spring") version "1.9.25" apply false
}

group = "com.digitalxiaoyao"
version = "0.0.1-SNAPSHOT"

extra["springCloudVersion"] = "2024.0.1"   // 统一 Spring Cloud 版本

repositories { mavenCentral() }

/* ===== 对所有子模块生效的公共约定 ===== */
subprojects {

	// 1) 至少应用 Kotlin 插件（它会自动 apply Java 插件）
	apply(plugin = "org.jetbrains.kotlin.jvm")

	// 如需 Spring Boot，把下行也放到子模块脚本里或这里
	// apply(plugin = "org.springframework.boot")

	apply(plugin = "io.spring.dependency-management")

	repositories { mavenCentral() }

	// 2) Java toolchain 现在放到这里——此时 java 扩展已存在
	extensions.configure<JavaPluginExtension> {
		toolchain.languageVersion.set(JavaLanguageVersion.of(17))
	}

	// 3) Kotlin 编译器统一参数
	tasks.withType<KotlinCompile>().configureEach {
		compilerOptions.freeCompilerArgs.add("-Xjsr305=strict")
	}

	// 4) 引入 Spring Cloud BOM，子模块无需再写版本号
	dependencies {
		"implementation"(platform("org.springframework.cloud:spring-cloud-dependencies:${rootProject.extra["springCloudVersion"]}"))
		"testImplementation"("org.springframework.boot:spring-boot-starter-test")
		"testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5")
		"testRuntimeOnly"  ("org.junit.platform:junit-platform-launcher")
	}
}
