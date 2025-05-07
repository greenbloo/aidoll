package com.digitalxiaoyao.gateway.infrastructure.config

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonYamlConfig {
    @Bean fun yamlMapper(): YAMLMapper = YAMLMapper()
}
