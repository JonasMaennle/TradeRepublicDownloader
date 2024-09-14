package com.tr.http.service

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class HttpService {
    private val client = RestClient.create()

    fun <T> post(url: String, body: Any, responseType: Class<T>): ResponseEntity<T> =
        client
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toEntity(responseType)

    fun <T> post(url: String, responseType: Class<T>): ResponseEntity<T> =
        client
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .toEntity(responseType)
}