package org.example.mock.repository

import org.example.mock.entity.User
import org.example.mock.entity.UserUrl
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository(private val jdbc: NamedParameterJdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        User(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            username = rs.getString("username"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            updatedAt = rs.getTimestamp("updated_at").toInstant(),
        )
    }

    private val userUrlMapper = RowMapper { rs, _ ->
        UserUrl(
            userId = rs.getLong("user_id"),
            urlId = rs.getLong("url_id"),
        )
    }

    fun findOrCreate(keycloakId: UUID, username: String, name: String?): User =
        jdbc.queryForObject(
            """
            INSERT INTO users (keycloak_id, username, name)
            VALUES (:kcId, :username, :name)
            ON CONFLICT (keycloak_id) DO UPDATE
                SET username = EXCLUDED.username, updated_at = now()
            RETURNING id, keycloak_id, username, name, created_at, updated_at
            """,
            mapOf("kcId" to keycloakId, "username" to username, "name" to name),
            rowMapper
        )

    fun connectUrlToUser(userId: Long, urlId: Long): UserUrl =
        jdbc.queryForObject(
            """
                INSERT INTO user_urls (user_id, url_id)
                VALUES (:userId, :urlId)
            """, mapOf("userId" to userId, "urlId" to urlId),
            userUrlMapper
        )
}