package org.example.mock.dto

import org.example.mock.entity.User
import java.time.Instant

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserRegistrationDto(
    @field:NotBlank(message = "Name must not be blank")
    @field:Size(min = 1, max = 100, message = "Name must be 1-100 characters")
    val name: String,

    @field:NotBlank(message = "Username must not be blank")
    @field:Pattern(
        regexp = "^[A-Za-z0-9_]{6,18}$",
        message = "Username must be 6-18 characters, letters/digits/underscore only"
    )
    val username: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    val password: String
)

data class UserDto(private val user: User) {
    val id: Long = user.id
    val name: String = user.name
    val username: String = user.username
    val createdAt: Instant = user.createdAt
    val updatedAt: Instant = user.updatedAt
}

data class LoginCredentials(
    @field:NotBlank(message = "Username must not be blank")
    @field:Pattern(
        regexp = "^[A-Za-z0-9_]{6,18}$",
        message = "Username must be 6-18 characters, letters/digits/underscore only"
    )
    val username: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    val password: String
)

data class TokenDto(val token: String)