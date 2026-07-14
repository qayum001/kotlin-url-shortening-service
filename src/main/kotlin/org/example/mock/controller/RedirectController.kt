package org.example.mock.controller

import org.example.mock.service.UrlService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class RedirectController(private val urlService: UrlService) {

    @GetMapping("/{code}")
    fun redirect(@PathVariable code: String): Mono<ResponseEntity<Void>> {
        return urlService.resolveForRedirect(code).map { target ->
        ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, target)
            .build() }
    }
}
