package com.tr.service

import com.tr.model.DownloadProgress
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Desktop
import java.io.File
import java.net.URL
import java.nio.file.*

class FileService {
    private val folderName = "Trade Republic Downloads"
    private val folderPath = getJarDirectory().resolve(folderName)
    private val logger = KotlinLogging.logger {}

    init {
        createFolderIfNotExist()
        openFolder()
    }

    fun downloadFile(url: String, fileName: String, downloadProgress: DownloadProgress) {
        try {
            val fileUrl = URL(url)
            val filePath = folderPath.resolve("$fileName.pdf")

            Files.copy(fileUrl.openStream(), filePath, StandardCopyOption.REPLACE_EXISTING)
            println()
            logger.info { "File '$fileName' downloaded to '$filePath'" }
            logger.info { "Downloaded (${downloadProgress.current}/${downloadProgress.total}) file(s) successfully." }
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

    private fun openFolder() {
        val folderPath = this.folderPath.toString()
        try {
            val file = File(folderPath)
            if (file.exists() && file.isDirectory) {
                Desktop.getDesktop().open(file)
                logger.info { "Folder opened: $folderPath" }
            } else {
                logger.error { "Folder does not exist or is not a directory: $folderPath" }
            }
        } catch (e: Exception) {
            logger.error { "Error opening folder: ${e.message}" }
        }
    }
}