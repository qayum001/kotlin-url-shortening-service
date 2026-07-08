package org.example.mock.controller

import jakarta.validation.Valid
import org.example.mock.dto.LoginCredentials
import org.example.mock.dto.TokenDto
import org.example.mock.dto.UserRegistrationDto
import org.example.mock.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody dto: UserRegistrationDto): ResponseEntity<TokenDto> {
        return ResponseEntity.status(HttpStatus.OK).body(authService.registerUser(dto))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody dto: LoginCredentials): ResponseEntity<TokenDto> {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(dto))
    }
}