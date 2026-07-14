package org.example.mock.config

import com.zaxxer.hikari.HikariDataSource
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import javax.sql.DataSource

@Configuration
class JdbcConfig(
    @Value("\${spring.datasource.url}") private val url: String,
    @Value("\${spring.datasource.username}") private val user: String,
    @Value("\${spring.datasource.password}") private val pass: String,
) {
    @Bean
    fun dataSource(): DataSource = HikariDataSource().apply {
        jdbcUrl = url
        username = user
        password = pass
    }

    @Bean
    fun namedParameterJdbcTemplate(dataSource: DataSource) = NamedParameterJdbcTemplate(dataSource)

    @Bean
    @Primary
    fun reactiveTransactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager =
        R2dbcTransactionManager(connectionFactory)
}
