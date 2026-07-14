package org.example.mock.repository

import io.r2dbc.spi.Readable
import org.example.mock.entity.Url
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

@Repository
class UrlRepositoryReactive(private val dbClient: DatabaseClient) {
    private fun mapUrl(row: Readable): Url = Url(
        id            = row.get("id", Long::class.javaObjectType)!!,
        url           = row.get("original_url", String::class.java)!!,
        shortCode     = row.get("short_code", String::class.java)!!,
        accessesCount = row.get("accesses_count", Long::class.javaObjectType)!!,
        created       = row.get("created_at", OffsetDateTime::class.java)!!.toInstant(),
        updatedAt     = row.get("updated_at", OffsetDateTime::class.java)!!.toInstant(),
    )

    fun insert(url: String, code: String): Mono<Url> =
        dbClient.sql(
            """
                insert into url (original_url, short_code)
                values (:url, :code)
                returning *
            """)
            .bind("url", url)
            .bind("code", code)
            .map { row -> mapUrl(row)}
            .one()
    fun getByCode(code: String): Mono<String> =
        dbClient.sql("select original_url from url where short_code = :code")
            .bind("code", code)
            .map { row -> row.get("original_url", String::class.javaObjectType)!!}
            .one()

    fun incrementAccessCount(shortCode: String): Mono<Void> =
        dbClient
            .sql("UPDATE url SET accesses_count = accesses_count + 1 WHERE short_code = :shortCode")
            .bind("shortCode", shortCode).then()


    fun findAllByUserId(userId: Long): Flux<Url> =
        dbClient.sql(
            """
                SELECT u.* FROM url u
                JOIN user_urls uu ON uu.url_id = u.id
                WHERE uu.user_id = :userId
                ORDER BY u.created_at DESC
            """)
            .bind("userId", userId)
            .map { r -> mapUrl(r) }
            .all()

    fun findByUserIdAndOriginalUrl(userId: Long, url: String): Mono<Url> =
        dbClient.sql(
            """
                SELECT u.* FROM url u
                JOIN user_urls uu ON uu.url_id = u.id
                WHERE uu.user_id = :userId AND u.original_url = :url
                LIMIT 1
            """)
            .bind("userId", userId)
            .bind("url", url)
            .map{ r -> mapUrl(r)}
            .one()

    fun isOwnedByUser(userId: Long, code: String): Mono<Boolean> =
        dbClient.sql(
            """
                SELECT EXISTS(
                    SELECT 1 FROM user_urls uu
                    JOIN url u ON u.id = uu.url_id
                    WHERE uu.user_id = :userId AND u.short_code = :code
                )
            """)
            .bind("userId", userId)
            .bind("code", code)
            .map{ r -> r.get(0, Boolean::class.javaObjectType)!!}
            .one()

    fun updateUrl(code: String, newUrl: String): Mono<Url> =
        dbClient.sql(""" 
            UPDATE url 
            SET original_url = :newUrl, updated_at = now()
            WHERE short_code = :code
            RETURNING *
        """)
            .bind("code", code)
            .bind("newUrl", newUrl)
            .map { r -> mapUrl(r) }
            .one()

    fun getAccesses(code: String): Mono<Url> =
        dbClient.sql("SELECT * FROM url WHERE short_code = :code")
            .bind("code", code)
            .map { r -> mapUrl(r)}
            .one()

    fun delete(code: String): Mono<Boolean> =
        dbClient.sql("DELETE FROM url WHERE short_code = :code")
            .bind("code", code)
            .fetch()
            .rowsUpdated()
            .map { it > 0 }

    fun isExistsByCode(shortCode: String): Mono<Boolean> =
        dbClient.sql("SELECT EXISTS(SELECT 1 FROM url where short_code = :shortCode)")
            .bind("shortCode", shortCode)
            .map { r -> r.get(0, Boolean::class.javaObjectType)!! }
            .one()
}