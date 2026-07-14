package org.example.mock.controller

import jakarta.validation.Valid
import org.example.mock.dto.CreateShortUrlRequest
import org.example.mock.dto.UpdateUrlRequest
import org.example.mock.entity.Url
import org.example.mock.service.UrlService
import org.example.mock.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/url")
class UrlController(
    private val urlService: UrlService,
    private val userService: UserService,
) {
    @PostMapping("/shorten")
    fun shortenUrl(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: CreateShortUrlRequest
    ): Mono<ResponseEntity<Url>> =
        currentUserId(jwt)
            .flatMap { userId -> urlService.shortenUrl(request.url, userId) }
            .map { url -> ResponseEntity.status(HttpStatus.CREATED).body(url) }

    @GetMapping
    fun listMyUrls(@AuthenticationPrincipal jwt: Jwt): Flux<Url> =
        currentUserId(jwt).flatMapMany { userId -> urlService.listUserUrls(userId) }

    @GetMapping("/{code}")
    fun getOriginalUrl(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable code: String
    ): Mono<ResponseEntity<String>> =
        currentUserId(jwt)
            .flatMap { userId -> urlService.getUserUrl(userId, code) }
            .map { ResponseEntity.ok(it) }

    @PutMapping("/update/{code}")
    fun updateUrl(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable code: String,
        @Valid @RequestBody request: UpdateUrlRequest
    ): Mono<ResponseEntity<Url>> =
        currentUserId(jwt)
            .flatMap { userId -> urlService.updateShortUrl(userId, code, request.url) }
            .map { ResponseEntity.ok(it) }

    @GetMapping("/{code}/qr", produces = ["image/svg+xml"])
    fun getQr(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable code: String
    ): Mono<ResponseEntity<String>> =
        currentUserId(jwt)
            .flatMap { userId -> urlService.qrForCode(userId, code) }
            .map { svg ->
                ResponseEntity.ok().contentType(MediaType.valueOf("image/svg+xml")).body(svg)
            }

    @GetMapping("/{code}/stats")
    fun getStats(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable code: String
    ): Mono<ResponseEntity<Url>> =
        currentUserId(jwt)
            .flatMap { userId -> urlService.getUrlAccessesCount(userId, code) }
            .map { ResponseEntity.ok(it) }

    @DeleteMapping("/{code}")
    fun deleteUrl(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable code: String
    ): Mono<ResponseEntity<Void>> =
        currentUserId(jwt)
            .flatMap { userId -> urlService.deleteUrl(userId, code) }
            .thenReturn(ResponseEntity.noContent().build())

    private fun currentUserId(jwt: Jwt): Mono<Long> =
        userService.resolveKeycloakUser(jwt).map { it.id }
}
