package com.tr.http.service

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.function.Consumer

@Service
class HttpService {
    private val client = RestClient.create()

    fun <T> post(
        url: String,
        responseType: Class<T>,
        body: Any? = null
    ): ResponseEntity<T> {
        val request = client
            .post()
            .uri(url)
            .headers(additionalHeaders())
        val requestWithBody = body?.let { request.body(it) } ?: request

        return requestWithBody
            .retrieve()
            .toEntity(responseType)
    }

    companion object {
        private fun additionalHeaders(): Consumer<HttpHeaders> =
            Consumer { headers ->
                headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                headers.set(
                    HttpHeaders.USER_AGENT,
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36"
                )
            }
    }
}