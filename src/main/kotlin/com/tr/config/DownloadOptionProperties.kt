package com.tr.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties
class DownloadOptionProperties {
    lateinit var actions: Map<String, DownloadOption>
}

class DownloadOption {
    lateinit var id: String
    lateinit var filename: String
    lateinit var identifier: List<String>
    lateinit var title: String
}
