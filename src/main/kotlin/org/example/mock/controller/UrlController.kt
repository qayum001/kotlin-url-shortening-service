package org.example.mock.controller

import jakarta.validation.Valid
import org.example.mock.dto.CreateShortUrlRequest
import org.example.mock.dto.UpdateUrlRequest
import org.example.mock.entity.Url
import org.example.mock.service.UrlService
import org.example.mock.service.UserService
import org.springframework.http.HttpStatus
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
    ): ResponseEntity<Url> {
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.shortenUrl(request.url, currentUserId(jwt)))
    }

    @GetMapping
    fun listMyUrls(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<Url>> {
        return ResponseEntity.status(HttpStatus.OK).body(urlService.listUserUrls(currentUserId(jwt)))
    }

    @GetMapping("/{code}")
    fun getOriginalUrl(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable code: String
    ): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body(urlService.getUserUrl(currentUserId(jwt), code))
    }

    @PutMapping("/update/{code}")
    fun updateUrl(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable code: String,
        @Valid @RequestBody request: UpdateUrlRequest
    ): ResponseEntity<Url> {
        return ResponseEntity.status(HttpStatus.OK).body(urlService.updateShortUrl(currentUserId(jwt), code, request.url))
    }

    @GetMapping("/{code}/stats")
    fun getStats(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable code: String
    ): ResponseEntity<Url> {
        return ResponseEntity.status(HttpStatus.OK).body(urlService.getUrlAccessesCount(currentUserId(jwt), code))
    }

    @DeleteMapping("/{code}")
    fun deleteUrl(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable code: String
    ): ResponseEntity<Void> {
        urlService.deleteUrl(currentUserId(jwt), code)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    private fun currentUserId(jwt: Jwt): Long = userService.resolveKeycloakUser(jwt).id
}