package org.example.mock.service

import org.example.mock.repository.UserRepository
import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AppUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.getUserByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        return SpringUser.builder()
            .username(user.username)
            .password(user.passwordHash)
            .authorities(emptyList())
            .build()
    }
}