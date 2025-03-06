package com.tr.io.service

import com.tr.io.models.DownloadProgress
import com.tr.io.models.UserSession
import com.tr.utils.transformDate
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.*

@Service
class FileService {
    private lateinit var folderPath: Path

    @PostConstruct
    private fun init() {
        folderPath = getJarDirectory().resolve(FOLDER_NAME)
        createFolderIfNotExist()
    }

    fun downloadFile(url: String, fileName: String, downloadProgress: DownloadProgress) {
        try {
            val fileUrl = URI(url).toURL()
            val uniqueFileName = getUniqueFileName(folderPath, fileName)
            val filePath = folderPath.resolve(uniqueFileName)

            Files.copy(fileUrl.openStream(), filePath, StandardCopyOption.REPLACE_EXISTING)
            println()
            logger.info("File '$uniqueFileName' downloaded.")
            logger.info("Downloaded (${downloadProgress.current}/${downloadProgress.total}) file(s) successfully.")
        } catch (e: Exception) {
            logger.error("Error downloading file: ${e.message}")
        }
    }

    private fun getUniqueFileName(folder: Path, baseName: String): String {
        var counter = 1
        var fileName = "$baseName.$FILE_EXTENSION"
        var filePath = folder.resolve(fileName)

        while (Files.exists(filePath)) {
            fileName = "${baseName}_$counter.$FILE_EXTENSION"
            filePath = folder.resolve(fileName)
            counter++
        }
        return fileName
    }

    fun buildFileName(userSession: UserSession, date: String, name: String): String {
        val filenameTemplate = userSession.downloadOption.filename
        return filenameTemplate
            .replace("\$DATE", transformDate(date))
            .replace("\$NAME", name)
    }

    fun openFolder() {
        val folderPath = this.folderPath.toString()
        try {
            val file = File(folderPath)
            if (file.exists() && file.isDirectory) {
                Desktop.getDesktop().open(file)
                logger.debug("Folder opened: $folderPath")
            } else {
                logger.error("Folder does not exist or is not a directory: $folderPath")
            }
        } catch (e: Exception) {
            logger.error("Error opening folder: ${e.message}")
        }
    }

    private fun getJarDirectory(): Path {
        return Paths.get("").toAbsolutePath()
    }

    private fun createFolderIfNotExist() {
        if (!Files.exists(folderPath)) {
            try {
                Files.createDirectory(folderPath)
                logger.debug("Folder '$FOLDER_NAME' created successfully.")
            } catch (e: Exception) {
                logger.error("Error creating folder: ${e.message}")
            }
        } else {
            logger.trace("Folder '$FOLDER_NAME' already exists.")
        }
    }

    companion object {
        private const val FOLDER_NAME = "Trade Republic Downloads"
        private const val FILE_EXTENSION = "pdf"
        private val logger = LoggerFactory.getLogger(FileService::class.java)
    }
}