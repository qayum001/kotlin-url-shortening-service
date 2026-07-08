package org.example.mock.controller

import org.example.mock.dto.CreateShortUrlRequest
import org.example.mock.entity.Url
import org.example.mock.service.UrlService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/shorten")
class UrlController(private val urlService: UrlService) {
    @PostMapping
    fun shortenUrl(@RequestBody request: CreateShortUrlRequest): ResponseEntity<Url> {
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.shortenUrl(request.url))
    }

    @GetMapping("/{code}")
    fun getOriginalUrl(@PathVariable code: String): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body(urlService.getUrl(code))
    }

    @PutMapping("/{code}")
    fun updateUrl(@PathVariable code: String, @RequestBody url: String): ResponseEntity<Url> {
        return ResponseEntity.status(HttpStatus.OK).body(urlService.updateShortUrl(code, url))
    }

    @GetMapping("/{code}/stats")
    fun getStats(@PathVariable code: String): ResponseEntity<Url> {
        return ResponseEntity.status(HttpStatus.OK).body(urlService.getUrlAccessesCount(code))
    }

    @DeleteMapping("/{code}")
    fun deleteUrl(@PathVariable code: String): ResponseEntity<Void> {
        urlService.deleteUrl(code)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}