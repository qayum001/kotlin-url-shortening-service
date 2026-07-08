package org.example.mock.service

import org.example.mock.dto.LoginCredentials
import org.example.mock.dto.TokenDto
import org.example.mock.dto.UserRegistrationDto
import org.example.mock.exceptions.InvalidCredentialsException
import org.example.mock.exceptions.InvalidPasswordException
import org.example.mock.repository.UserRepository
import org.example.mock.util.TokenGenerator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val hasher: PasswordEncoder,
    private val jwtGenerator: TokenGenerator
) {
    fun registerUser(dto: UserRegistrationDto): TokenDto {
        val passwordHash = hasher.encode(dto.password)
            ?: throw InvalidPasswordException("Invalid password")

        val user = userRepository.createUser(dto.username, dto.username, passwordHash)

        val token = jwtGenerator.generateToken(user.username, user.id)
        return TokenDto(token)
    }

    fun login(credentials: LoginCredentials): TokenDto {
        val isUserExists = userRepository.isUserExists(credentials.username)
        if (!isUserExists) {
            throw InvalidCredentialsException()
        }

        val user = userRepository.getUserByUsername(credentials.username)

        if (!hasher.matches(credentials.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        val token = jwtGenerator.generateToken(user.username, user.id)
        return TokenDto(token)
    }
}