package org.example.mock.repository

import org.example.mock.entity.User
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserRepository(private val jdbc: NamedParameterJdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        User(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            username = rs.getString("username"),
            passwordHash = rs.getString("password_hash"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            updatedAt = rs.getTimestamp("updated_at").toInstant(),
        )
    }

    fun createUser(name: String, username: String, passwordHash: String): User =
        jdbc.queryForObject("""
            INSERT INTO users (name, username, password_hash)
            VALUES (:name, :username, :passwordHash)
            RETURNING *
        """, mapOf("name" to name, "username" to username, passwordHash to passwordHash), rowMapper)

    fun getUserById(id: Long): User =
        jdbc.queryForObject("""
            SELECT * FROM users WHERE id = :id
        """, mapOf("id" to id), rowMapper)

    fun getUserByUsername(username: String) =
        jdbc.queryForObject("""
            SELECT * FROM users WHERE username = :username
        """, mapOf("username" to username), rowMapper)

    fun isUserExists(username: String): Boolean =
        jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)
        """, mapOf("username" to username), Boolean::class.java)!!

    fun updateUserNameById(newName: String, id: Long): User =
        jdbc.queryForObject("""
            UPDATE users
            SET name = :newName, updated_at = now()
            WHERE id = :id
            RETURNING *
        """, mapOf("newName" to newName, "id" to id), rowMapper)

    fun updateUserNameByUsername(newName: String, username: String): User =
        jdbc.queryForObject("""
            UPDATE users
            SET name = :newName, updated_at = now()
            WHERE username = :username
            RETURNING *
        """, mapOf("newName" to newName, "username" to username), rowMapper)

    fun updatePasswordHashById(newHash: String, id: Long): Int =
        jdbc.update("""
            UPDATE users SET password_hash = :newHash, updated_at = now() WHERE id = :id 
        """, mapOf("newHash" to newHash, "id" to id))

    fun updatePasswordHashByUsername(newHash: String, username: String): Int =
        jdbc.update("""
            UPDATE users SET password_hash = :newHash, updated_at = now() WHERE username = :username 
        """, mapOf("newHash" to newHash, "username" to username))
}