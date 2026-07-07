package org.example.mock.entity

import java.time.Instant

data class Url (
    val id: Long,
    val url: String,
    val shortCode: String,
    val created: Instant,
    val updatedAt: Instant,
    val accessesCount: Long
)