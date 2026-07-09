package org.example.mock.exceptions

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import kotlin.time.Clock

@RestControllerAdvice
class GlobalHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<ErrorResponse> {
        logger.warn("API Exception: ${e.errorCode}")
        return buildResponse(e.status, e.errorCode, e.message ?: "Error")
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.warn("HttpMessageNotReadableException: ${e.message}")
        return buildResponse(HttpStatus.NOT_IMPLEMENTED, "MALFOLDED_REQUEST", e.message ?: "Error")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn("MethodArgumentNotValidException: ${e.message}")
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", e.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.warn("Unknown error", e)
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR", e.message ?: "Error")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn("MethodArgumentNotValidException")
        val detail = e.bindingResult.fieldErrors.joinToString("; ")
            { "${it.field}: ${it.defaultMessage}" }
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", detail)
    }

    fun buildResponse(
        status: HttpStatus,
        errorCode: String,
        errorMessage: String): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(status).body(
            ErrorResponse(
                time = Clock.System.now(),
                status = status.value(),
                error = status.reasonPhrase,
                code = errorCode,
                message = errorMessage
            )
        )
    }
}