package com.tr.http.service

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.function.Consumer

@Service
class HttpService {
    private val client: RestClient = RestClient.builder()
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(
            HttpHeaders.USER_AGENT,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/139.0.0.0 Safari/537.36"
        )
        .build()

    fun <T> post(
        url: String,
        responseType: Class<T>,
        headers: Consumer<HttpHeaders>? = null
    ): ResponseEntity<T> {
        val request = client
            .post()
            .uri(url)

        headers?.let { request.headers(it) }

        return request
            .retrieve()
            .toEntity(responseType)
    }
}