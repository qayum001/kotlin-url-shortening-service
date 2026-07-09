package org.example.mock.component

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class KeycloakRoleConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmAccess = jwt.getClaimAsMap("realm_access") ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val roles = realmAccess["roles"] as? Collection<String> ?: return emptyList()
        return roles.map { SimpleGrantedAuthority("ROLE_$it") }
    }
}