package org.example.mock.repository

import io.r2dbc.spi.Readable
import org.example.mock.entity.User
import org.example.mock.entity.UserUrl
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class UserRepository(private val dbClient: DatabaseClient) {

    private fun mapUser(row: Readable): User =
        User(
            id = row.get("id", Long::class.javaObjectType)!!,
            name = row.get("name", String::class.java),
            username = row.get("username", String::class.java)!!,
            createdAt = row.get("created_at", OffsetDateTime::class.java)!!.toInstant(),
            updatedAt = row.get("updated_at", OffsetDateTime::class.java)!!.toInstant(),
        )

    fun findOrCreate(keycloakId: UUID, username: String, name: String?): Mono<User> {
        val spec = dbClient.sql(
            """
                INSERT INTO users (keycloak_id, username, name)
                VALUES (:kcId, :username, :name)
                ON CONFLICT (keycloak_id) DO UPDATE
                    SET username = EXCLUDED.username, updated_at = now()
                RETURNING id, keycloak_id, username, name, created_at, updated_at
            """)
            .bind("kcId", keycloakId)
            .bind("username", username)
        val bound = if (name != null) spec.bind("name", name)
                    else spec.bindNull("name", String::class.java)
        return bound.map { row -> mapUser(row) }.one()
    }

    fun connectUrlToUser(userId: Long, urlId: Long): Mono<UserUrl> =
        dbClient.sql(
            """
                INSERT INTO user_urls (user_id, url_id)
                VALUES (:userId, :urlId)
                RETURNING user_id, url_id
            """)
            .bind("userId", userId)
            .bind("urlId", urlId)
            .map { row ->
                UserUrl(
                    userId = row.get("user_id", Long::class.javaObjectType)!!,
                    urlId = row.get("url_id", Long::class.javaObjectType)!!,
                )
            }
            .one()
}