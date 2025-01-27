package com.tr.io.service

import com.tr.config.DownloadOptionConfig
import com.tr.io.models.UserSession
import com.tr.utils.getCurrentMonth
import com.tr.utils.isMonthValid
import com.tr.utils.models.TimeFormatPattern
import com.tr.utils.readUserConsoleInput
import com.tr.utils.toYearMonth
import com.tr.websocket.models.request.TimelineTransactionsRequest
import com.tr.websocket.service.WebsocketService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.system.exitProcess

@Service
class UserInputService(
    @Autowired private val websocketService: WebsocketService,
    @Autowired private val downloadOptionConfig: DownloadOptionConfig,
) {
    lateinit var userSession: UserSession

    fun handleUserInput(sessionToken: String) {
        logger.debug("Login Successful")
        val documentType = readUserConsoleInput(
            "Please enter the document type you're looking for, 'D' for Dividende or 'S' for Sparplan or 'Z' for Zinsen or 'O' for Order:",
            logger
        ) { validateUserInput(it) }

        var selectedTime = readUserConsoleInput(
            "Please enter year and month you are interested in format YYYY-MM or leave it empty for using the current month:",
            logger
        ) { isMonthValid(it) || it.isEmpty() }

        if (selectedTime.isEmpty()) selectedTime = getCurrentMonth(TimeFormatPattern.PARTIAL)

        userSession = UserSession(
            downloadOptionConfig.getDownloadOptionById(documentType),
            selectedTime.toYearMonth(),
            sessionToken
        )
        websocketService.createNewRequest(TimelineTransactionsRequest(sessionToken))
    }

    fun checkForNextRequest(sessionToken: String) {
        println()
        logger.info("Job finished successful :)")

        val userInput = readUserConsoleInput(
            "Enter 'X' to close the application or 'Q' to start another query:",
            logger
        ) { it.equals("x", ignoreCase = true) || it.equals("q", ignoreCase = true) }

        if (userInput.equals("x", ignoreCase = true)) {
            websocketService.disconnect()
            exitProcess(0)
        }

        handleUserInput(sessionToken)
    }

    private fun validateUserInput(input: String) =
        downloadOptionConfig.getDownloadOptionMap().keys.contains(input.uppercase())

    companion object {
        private val logger = LoggerFactory.getLogger(UserInputService::class.java)
    }
}
