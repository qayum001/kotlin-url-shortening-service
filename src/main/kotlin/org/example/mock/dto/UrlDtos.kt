package org.example.mock.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.URL

data class CreateShortUrlRequest(
    @field:NotBlank(message = "URL must not be blank")
    @field:URL(message = "URL is malformed")
    @field:Pattern(
        regexp = "^(?i)https?://.+",
        message = "URL must start with http:// or https:// and contain a host"
    )
    val url: String
)

data class UpdateUrlRequest(
    @field:NotBlank(message = "URL must not be blank")
    @field:URL(message = "URL is malformed")
    @field:Pattern(
        regexp = "^(?i)https?://.+",
        message = "URL must start with http:// or https:// and contain a host"
    )
    val url: String
)
