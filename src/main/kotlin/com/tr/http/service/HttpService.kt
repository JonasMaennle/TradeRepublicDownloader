package com.tr.http.service

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

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
            .contentType(MediaType.APPLICATION_JSON)

        val requestWithBody = body?.let { request.body(it) } ?: request

        return requestWithBody
            .retrieve()
            .toEntity(responseType)
    }
}