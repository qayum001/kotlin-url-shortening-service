package org.example.mock.service

import org.example.mock.entity.Url
import org.example.mock.repository.UrlRepository
import org.springframework.stereotype.Service

object RandomCode {
    private const val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    fun generate(length: Int): String =
        (1..length).map { CHARS.random() }.joinToString("")
}

@Service
class UrlService(private val repository: UrlRepository) {
    fun shortenUrl(originalUrl: String): Url {
        val uniqueCode = generateUniqueCode()
        val url = repository.insert(originalUrl, uniqueCode)
        return url
    }

    fun getOriginalUrl(code: String): String {
        val res = repository.getByCode(code)
        repository.incrementAccessCount(code)
        return res
    }

    fun updateShortUrl(code: String, newUrl: String): Url {
        return repository.updateUrl(code, newUrl)
    }

    fun getUrlAccessesCount(code: String): Url {
        return repository.getAccesses(code)
    }

    fun deleteUrl(code: String) {
        repository.delete(code)
    }

    private fun generateUniqueCode(): String {
        while (true) {
            val code = RandomCode.generate(6)
            val isExists = repository.isExistsByCode(code)
            if (!isExists) {
                return code
            }
        }
    }
}