package com.tr.io.service

import com.tr.io.models.DownloadOptions
import com.tr.io.models.UserInput
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
) {
    lateinit var userInput: UserInput

    fun handleUserInput(sessionToken: String) {
        logger.debug("Login Successful")
        val documentType = readUserConsoleInput(
            "Please enter the document type you're looking for, 'D' for Dividende or 'S' for Sparplan or 'Z' for Zinsen or 'O' for Order:",
            logger
        ) { DownloadOptions.isValidOption(it) }

        var selectedTime = readUserConsoleInput(
            "Please enter year and month you are interested in format YYYY-MM or leave it empty for using the current month:",
            logger
        ) { isMonthValid(it) || it.isEmpty() }

        if (selectedTime.isEmpty()) selectedTime = getCurrentMonth(TimeFormatPattern.PARTIAL)

        userInput = UserInput(
            DownloadOptions.fromValue(documentType),
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

    companion object {
        private val logger = LoggerFactory.getLogger(UserInputService::class.java)
    }
}
