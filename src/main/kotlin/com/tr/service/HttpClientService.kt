package com.tr.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class HttpClientService(
    private val client: OkHttpClient = OkHttpClient(),
    private val objectMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
) {

    fun <T> postRequest(url: String, body: T? = null): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val payload = if (body != null) objectMapper.writeValueAsString(body).toRequestBody(mediaType) else "".toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(payload)
            .build()
        return client.newCall(request).execute()
    }

    fun <T> transformBufferToObject(response: Response, clazz: Class<T>): T {
        val responseBody = response.body
        val buffer = StringBuilder()
        responseBody?.let {
            val source = it.source()
            while (!source.exhausted()) {
                buffer.append(source.readUtf8())
            }
        }
        return objectMapper.readValue(buffer.toString(), clazz)
    }
}