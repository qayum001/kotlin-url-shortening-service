package org.example.mock.service

import org.example.mock.entity.Url
import org.example.mock.exceptions.InvalidCodeException
import org.example.mock.exceptions.UrlNotFoundException
import org.example.mock.repository.UrlRepositoryReactive
import org.example.mock.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

object RandomCode {
    private const val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    fun generate(length: Int): String =
        (1..length).map { CHARS.random() }.joinToString("")
}

@Service
class UrlService(
    private val userRepository: UserRepository,
    private val qrService: QrService,
    private val repository: UrlRepositoryReactive,
) {
    companion object {
        private const val CODE_LENGTH = 6
        private val CODE_REGEX = Regex("^[A-Za-z0-9]{${CODE_LENGTH}}$")
    }

    @Transactional
    fun shortenUrl(originalUrl: String, userId: Long): Mono<Url> =
        repository.findByUserIdAndOriginalUrl(userId, originalUrl)
            .switchIfEmpty(Mono.defer {
                generateUniqueCode().flatMap { code ->
                    repository.insert(originalUrl, code).flatMap { saved ->
                        userRepository.connectUrlToUser(userId, saved.id).thenReturn(saved)
                    }
                }
            })

    fun listUserUrls(userId: Long): Flux<Url> = repository.findAllByUserId(userId)

    fun resolveForRedirect(code: String): Mono<String> =
        repository.getByCode(code)
            .switchIfEmpty(Mono.error(UrlNotFoundException(code)))
            .flatMap { target -> repository.incrementAccessCount(code).thenReturn(target) }

    fun getUserUrl(userId: Long, code: String): Mono<String> {
        validateCode(code)
        return verifyOwnership(userId, code).then(repository.getByCode(code))
    }

    fun qrForCode(userId: Long, code: String): Mono<String> {
        validateCode(code)
        return verifyOwnership(userId, code).then(Mono.fromCallable { qrService.svgForCode(code) })
    }

    fun updateShortUrl(userId: Long, code: String, newUrl: String): Mono<Url> {
        validateCode(code)
        return verifyOwnership(userId, code).then(repository.updateUrl(code, newUrl))
    }

    fun getUrlAccessesCount(userId: Long, code: String): Mono<Url> {
        validateCode(code)
        return verifyOwnership(userId, code).then(repository.getAccesses(code))
    }

    fun deleteUrl(userId: Long, code: String): Mono<Void> {
        validateCode(code)
        return verifyOwnership(userId, code).then(repository.delete(code).then())
    }

    private fun verifyOwnership(userId: Long, code: String): Mono<Void> =
        repository.isOwnedByUser(userId, code)
            .flatMap { owned ->
                if (owned) Mono.empty() else Mono.error(UrlNotFoundException(code))
            }

    private fun generateUniqueCode(): Mono<String> =
        Mono.fromCallable { RandomCode.generate(CODE_LENGTH) }
            .filterWhen { code -> repository.isExistsByCode(code).map { exists -> !exists } }
            .repeatWhenEmpty { attempts -> attempts }

    private fun validateCode(code: String) {
        if (!CODE_REGEX.matches(code)) {
            throw InvalidCodeException(
                "Code must be exactly $CODE_LENGTH characters and contain only letters and digits"
            )
        }
    }
}