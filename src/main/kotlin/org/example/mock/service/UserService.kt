package org.example.mock.service

import org.example.mock.entity.User
import org.example.mock.exceptions.InvalidTokenException
import org.example.mock.repository.UserRepository
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(private val userRepository: UserRepository) {
    fun resolveKeycloakUser(jwt: Jwt): User = userRepository.findOrCreate(
        keycloakId = UUID.fromString(jwt.subject),
        username = jwt.getClaimAsString("preferred_username")
            ?: throw InvalidTokenException("Token is missing 'preferred_username' claim"),
        name = jwt.getClaimAsString("name")
    )
}