package org.example.mock.repository

import org.example.mock.entity.Url
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UrlRepository(private val jdbc: NamedParameterJdbcTemplate) {
    private val rowMapper = RowMapper { rs, _ ->
        Url(
            id = rs.getLong("id"),
            url = rs.getString("original_url"),
            shortCode = rs.getString("short_code"),
            accessesCount = rs.getLong("accesses_count"),
            created = rs.getTimestamp("created_at").toInstant(),
            updatedAt = rs.getTimestamp("updated_at").toInstant()
        )
    }

    fun insert(url: String, shortCode: String): Url =
        jdbc.queryForObject(
            """
                INSERT INTO url (original_url, short_code)
                VALUES (:url, :shortCode)
                RETURNING *
            """,
            mapOf("url" to url, "shortCode" to shortCode),
            rowMapper,
        )

    fun findAllByUserId(userId: Long): List<Url> =
        jdbc.query(
            """
                SELECT u.* FROM url u
                JOIN user_urls uu ON uu.url_id = u.id
                WHERE uu.user_id = :userId
                ORDER BY u.created_at DESC
            """,
            mapOf("userId" to userId),
            rowMapper,
        )

    fun findByUserIdAndOriginalUrl(userId: Long, url: String): Url? =
        jdbc.query(
            """
                SELECT u.* FROM url u
                JOIN user_urls uu ON uu.url_id = u.id
                WHERE uu.user_id = :userId AND u.original_url = :url
                LIMIT 1
            """,
            mapOf("userId" to userId, "url" to url),
            rowMapper,
        ).firstOrNull()

    fun isOwnedByUser(userId: Long, code: String): Boolean =
        jdbc.queryForObject(
            """
                SELECT EXISTS(
                    SELECT 1 FROM user_urls uu
                    JOIN url u ON u.id = uu.url_id
                    WHERE uu.user_id = :userId AND u.short_code = :code
                )
            """,
            mapOf("userId" to userId, "code" to code),
            Boolean::class.java,
        )!!
    fun getByCode(code: String): String =
        jdbc.queryForObject(
            """
                SELECT original_url FROM url WHERE short_code = :shortCode
            """,
            mapOf("shortCode" to code),
            String::class.java)!!

    fun incrementAccessCount(shortCode: String) =
        jdbc.update("""
            UPDATE url SET accesses_count = accesses_count + 1 WHERE short_code = :shortCode
        """, mapOf("shortCode" to shortCode))

    fun updateUrl(code: String, newUrl: String): Url =
        jdbc.queryForObject(""" 
            UPDATE url 
            SET original_url = :newUrl, updated_at = now()
            WHERE short_code = :code
            RETURNING *
        """,
            mapOf("code" to code, "newUrl" to newUrl),
            rowMapper)

    fun getAccesses(code: String): Url =
        jdbc.queryForObject("""
            SELECT * FROM url WHERE short_code = :code
        """, mapOf("code" to code), rowMapper)

    fun delete(code: String): Boolean {
        return jdbc.update("""
            DELETE FROM url WHERE short_code = :code
        """, mapOf("code" to code)) > 0
    }

    fun isExistsByCode(shortCode: String): Boolean =
        jdbc.queryForObject(
            """
                SELECT EXISTS(SELECT 1 FROM url where short_code = :shortCode)
            """,
            mapOf("shortCode" to shortCode),
            Boolean::class.java
        )!!

}