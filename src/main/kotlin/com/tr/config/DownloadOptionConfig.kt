package com.tr.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class DownloadOptionConfig(
    @Autowired private val downloadOptionProperties: DownloadOptionProperties,
) {
    fun getDownloadOptionMap(): Map<String, Map.Entry<String, DownloadOption>> =
        downloadOptionProperties.actions.entries.associateBy { it.value.id }

    fun getDownloadOptionById(id: String): DownloadOption =
        getDownloadOptionMap().entries.first { it.key == id.uppercase() }.value.value
}