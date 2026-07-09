package org.example.mock.exceptions

import java.time.Instant


data class ErrorResponse(
    val time: Instant = Instant.now(),
    val status: Int,
    val error: String = "Error",
    val code: String = "UNKNOWN",
    val message: String = "Unknown error",
)