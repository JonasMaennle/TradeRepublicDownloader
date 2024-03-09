package com.tr.service

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URL
import java.nio.file.*

class FileService {
    private val jarPath = getJarDirectory()
    private val folderName = "Trade Republic Downloads"
    private val folderPath = jarPath.resolve(folderName)
    private val logger = KotlinLogging.logger {}
    private var downloadCounter: Int = 0

    init {
        createFolderIfNotExist()
    }

    fun downloadFile(url: String, fileName: String) {
        try {
            val fileUrl = URL(url)
            val filePath = folderPath.resolve("$fileName.pdf")

            Files.copy(fileUrl.openStream(), filePath, StandardCopyOption.REPLACE_EXISTING)
            downloadCounter++
            println()
            logger.info { "File '$fileName' downloaded to '$filePath'" }
            logger.info { "Downloaded $downloadCounter file(s) successfully." }
        } catch (e: Exception) {
            logger.error { "Error downloading file: ${e.message}" }
        }
    }

    private fun createFolderIfNotExist() {
        if (!Files.exists(folderPath)) {
            try {
                Files.createDirectory(folderPath)
                logger.info { "Folder '$folderName' created successfully." }
            } catch (e: Exception) {
                logger.error { "Error creating folder: ${e.message}" }
            }
        } else {
            logger.trace { "Folder '$folderName' already exists." }
        }
    }

    private fun getJarDirectory(): Path {
        val uri = object {}.javaClass.protectionDomain.codeSource.location.toURI()
        val jarPath = if (uri.scheme == "file") {
            Paths.get(uri)
        } else {
            FileSystems.newFileSystem(uri, emptyMap<String, Any>()).getPath("/")
        }
        return jarPath.parent
    }
}