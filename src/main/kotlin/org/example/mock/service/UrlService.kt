package org.example.mock.service

import org.example.mock.entity.Url
import org.example.mock.exceptions.InvalidCodeException
import org.example.mock.exceptions.UrlNotFoundException
import org.example.mock.repository.UrlRepository
import org.example.mock.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

object RandomCode {
    private const val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    fun generate(length: Int): String =
        (1..length).map { CHARS.random() }.joinToString("")
}

@Service
class UrlService(
    private val repository: UrlRepository,
    private val userRepository: UserRepository,
) {
    companion object {
        private const val CODE_LENGTH = 6
        private val CODE_REGEX = Regex("^[A-Za-z0-9]{${CODE_LENGTH}}$")
    }

    @Transactional
    fun shortenUrl(originalUrl: String, userId: Long): Url {
        val uniqueCode = generateUniqueCode()
        val url = repository.insert(originalUrl, uniqueCode)
        userRepository.connectUrlToUser(userId, url.id)
        return url
    }

    fun getUrl(code: String): String {
        validateCode(code)
        val res = repository.getByCode(code)
        repository.incrementAccessCount(code)
        return res
    }

    fun updateShortUrl(code: String, newUrl: String): Url {
        validateCode(code)
        return repository.updateUrl(code, newUrl)
    }

    fun getUrlAccessesCount(code: String): Url {
        validateCode(code)
        if (!repository.isExistsByCode(code)) {
            throw UrlNotFoundException(code)
        }

        return repository.getAccesses(code)
    }

    fun deleteUrl(code: String) {
        validateCode(code)
        if (!repository.isExistsByCode(code)) {
            throw UrlNotFoundException(code)
        }

        repository.delete(code)
    }

    private fun generateUniqueCode(): String {
        while (true) {
            val code = RandomCode.generate(CODE_LENGTH)
            val isExists = repository.isExistsByCode(code)
            if (!isExists) {
                return code
            }
        }
    }

    private fun validateCode(code: String) {
        if (!CODE_REGEX.matches(code)) {
            throw InvalidCodeException(
                "Code must be exactly $CODE_LENGTH characters and contain only letters and digits"
            )
        }
    }
}