package org.example.mock.exceptions

import kotlin.time.Clock
import kotlin.time.Instant

data class ErrorResponse(
    val time: Instant = Clock.System.now(),
    val status: Int,
    val error: String = "Error",
    val code: String = "UNKNOWN",
    val message: String = "Unknown error",
)