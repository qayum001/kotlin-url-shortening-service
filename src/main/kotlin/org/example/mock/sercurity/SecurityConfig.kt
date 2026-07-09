package org.example.mock.sercurity

import org.example.mock.component.KeycloakRoleConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.http.HttpMethod
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity
class SecurityConfig(private val roleConverter: KeycloakRoleConverter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/scalar/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/{code:[A-Za-z0-9]{6}}").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt { jwt -> jwt.jwtAuthenticationConverter(converter()) } }
        return http.build()
    }

    private fun converter() = JwtAuthenticationConverter().apply {
        setJwtGrantedAuthoritiesConverter(roleConverter)
    }
}