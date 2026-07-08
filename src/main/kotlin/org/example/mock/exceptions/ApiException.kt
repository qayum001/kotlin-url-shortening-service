package org.example.mock.exceptions

import org.springframework.http.HttpStatus

abstract class ApiException(
    message: String,
    val status: HttpStatus,
    val errorCode: String) : RuntimeException(message)

class UrlNotFoundException(code: String) : ApiException(
    "Url not found for code $code",
    HttpStatus.NOT_FOUND,
    "URL_NOT_FOUND"
)

class InvalidUrlException(url: String) : ApiException(
    "Invalid URL: $url",
    HttpStatus.BAD_REQUEST,
    "INVALID_URL"
)

class AlreadyExistsException(url: String) : ApiException(
    "Already exists: $url",
    HttpStatus.CONFLICT,
    "ALREADY_EXISTS"
)