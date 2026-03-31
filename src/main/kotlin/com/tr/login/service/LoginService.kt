package com.tr.login.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.tr.http.service.HttpService
import com.tr.io.service.UserInputService
import com.tr.login.models.LoginDataWrapper
import com.tr.login.service.playwright.PlaywrightService
import com.tr.utils.readUserConsoleInput
import com.tr.utils.transformHeaderCookiesToMap
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.function.Consumer

@Service
class LoginService(
    @Autowired private val httpService: HttpService,
    @Autowired private val userInputService: UserInputService,
    @Autowired private val playwrightService: PlaywrightService,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Value("\${tr.api.login:}")
    private lateinit var loginUrl: String

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

        val loginResponse = playwrightService.performTRLogin(phoneNumber, pin)
        logger.debug("Login Response: " + objectMapper.writeValueAsString(loginResponse))
        perform2FA(loginResponse)
    }

    private fun perform2FA(login: LoginDataWrapper) {
        val twoFACode = readUserConsoleInput(
            "Please enter the four digit 2FA code you received (valid for ${login.loginResponse.countdownInSeconds} seconds):",
            logger
        ) { it.length == 4 }

        val twoFAResponse = httpService.post(
            "$loginUrl/${login.loginResponse.processId}/$twoFACode",
            Any::class.java,
            addWafHeader(login.awsHeaderToken)
        )

        val cookieMap = transformHeaderCookiesToMap(twoFAResponse.headers[COOKIE_IDENTIFIER])
        val sessionToken = cookieMap[SESSION_IDENTIFIER] ?: throw Exception("Invalid Code. No session cookie received")

        userInputService.handleUserInput(sessionToken)
    }

    private fun addWafHeader(header: String): Consumer<HttpHeaders> =
        Consumer { headers ->
            headers.set("x-aws-waf-token", header)
        }

    companion object {
        private val logger = LoggerFactory.getLogger(LoginService::class.java)
        private const val SESSION_IDENTIFIER = "tr_session"
        private const val COOKIE_IDENTIFIER = "set-cookie"
    }
}
