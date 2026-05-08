package com.tr.login.service

import com.tr.io.service.UserInputService
import com.tr.login.service.playwright.PlaywrightService
import com.tr.utils.readUserConsoleInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class LoginService(
    @Autowired private val userInputService: UserInputService,
    @Autowired private val playwrightService: PlaywrightService,
) {
    @Value("\${PHONE_NUMBER:}")
    private lateinit var phoneNumber: String

    @Value("\${PIN:}")
    private lateinit var pin: String

    fun initializeLogin() {
        val phoneNumber = phoneNumber.ifEmpty {
            readUserConsoleInput(
                "Please enter your phone number (e.g. +49123456789):",
                logger
            ) { it.length > 4 }
        }
        val pin = pin.ifEmpty {
            readUserConsoleInput(
                "Please enter your four digit PIN (e.g. 1234):",
                logger
            ) { it.length == 4 }
        }

        val response = playwrightService.performLogin(phoneNumber, pin)
        userInputService.handleUserInput(response.trSession)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoginService::class.java)
    }
}
