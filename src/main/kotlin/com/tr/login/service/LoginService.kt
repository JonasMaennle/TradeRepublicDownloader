package com.tr.login.service

import com.tr.http.service.HttpService
import com.tr.login.models.LoginRequest
import com.tr.login.models.LoginResponse
import com.tr.io.service.UserInputService
import com.tr.utils.readUserConsoleInput
import com.tr.utils.transformHeaderCookiesToMap
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.Exception

@Service
class LoginService(
    @Autowired private val httpService: HttpService,
    @Autowired private val userInputService: UserInputService,
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

        val loginResponse = httpService.post(
            loginUrl,
            LoginResponse::class.java,
            LoginRequest(phoneNumber, pin),
        )
        if (!loginResponse.statusCode.is2xxSuccessful || loginResponse.body == null) throw Exception("Code: ${loginResponse.statusCode.value()} Message: ${loginResponse.body}")
        perform2FA(loginResponse.body!!)
    }

    private fun perform2FA(loginResponse: LoginResponse) {
        val twoFACode = readUserConsoleInput(
            "Please enter the four digit 2FA code you received (valid for ${loginResponse.countdownInSeconds} seconds):",
            logger
        ) { it.length == 4 }
        val twoFAResponse = httpService.post("$loginUrl/${loginResponse.processId}/$twoFACode", Any::class.java)
        val cookieMap = transformHeaderCookiesToMap(twoFAResponse.headers[COOKIE_IDENTIFIER])
        val sessionToken = cookieMap[SESSION_IDENTIFIER] ?: throw Exception("Invalid Code. No session cookie received")

        userInputService.handleUserInput(sessionToken)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoginService::class.java)
        private const val SESSION_IDENTIFIER = "tr_session"
        private const val COOKIE_IDENTIFIER = "set-cookie"
    }
}
