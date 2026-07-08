package org.example.mock.entity

import java.time.Instant

data class User (
    val id: Long,
    val name: String,
    val username: String,
    val passwordHash: String,
    val createdAt: Instant,
    val updatedAt: Instant
)