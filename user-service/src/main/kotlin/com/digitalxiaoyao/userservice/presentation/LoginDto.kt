// src/main/kotlin/com/digitalxiaoyao/userservice/presentation/LoginDto.kt
package com.digitalxiaoyao.userservice.presentation

import jakarta.validation.constraints.NotBlank

data class LoginDto(
    @field:NotBlank(message = "username 必填")
    val username: String,
    @field:NotBlank(message = "password 必填")
    val password: String
)

data class AuthResultDto(
    val token: String,
    val userId: Long,
    val username: String
)
