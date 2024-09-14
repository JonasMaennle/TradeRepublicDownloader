package com.tr.io.service

import com.tr.io.models.DownloadProgress
import com.tr.io.models.DownloadOptions
import com.tr.io.models.UserInput
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
    init {
        // createFolderIfNotExist()
    }

    fun downloadFile(url: String, fileName: String, downloadProgress: DownloadProgress) {
        try {
            val fileUrl = URI(url).toURL()
            val filePath = folderPath.resolve("$fileName.pdf")

            Files.copy(fileUrl.openStream(), filePath, StandardCopyOption.REPLACE_EXISTING)
            println()
            logger.info("File '$fileName' downloaded to '$filePath'")
            logger.info("Downloaded (${downloadProgress.current}/${downloadProgress.total}) file(s) successfully.")
        } catch (e: Exception) {
            logger.error("Error downloading file: ${e.message}")
        }
    }

    fun buildFileName(userInput: UserInput, date: String, name: String): String {
        return when (userInput.documentType) {
            DownloadOptions.DIVIDEND -> "Dividende_${transformDate(date)}_${name}"
            DownloadOptions.SAVINGS_PLAN -> "Abrechnung Sparplan_${transformDate(date)}_${name}"
            DownloadOptions.INTEREST -> "Zinsen_${transformDate(date)}"
            DownloadOptions.ORDER -> "Kauf_${transformDate(date)}_${name}"
        }
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
        // Get the path of a class file, which is inside the JAR
        val path = Paths.get("").toAbsolutePath()
        logger.info("ABSOLUTE: $path")
        //val resource = object {}.javaClass.classLoader.getResource("com/tr/io/service/FileService.class")
        //val uri = resource?.toURI()

        //if (uri == null) {
          //  throw IllegalStateException("Resource URI is null")
        //}

        //val jarPath = Paths.get(uri)
        //logger.info("JAR PATH: $jarPath")
        return path
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
        private val logger = LoggerFactory.getLogger(FileService::class.java)
    }
}