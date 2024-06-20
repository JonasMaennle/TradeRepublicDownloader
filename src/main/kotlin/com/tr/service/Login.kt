package com.tr.service

import com.tr.model.Pattern
import com.tr.model.response.LoginResponse
import com.tr.model.request.LoginRequest
import com.tr.model.request.TimelineTransactionsRequest
import com.tr.utils.*
import io.github.cdimascio.dotenv.Dotenv
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.Response
import java.lang.Exception
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class Login {
    private val logger = KotlinLogging.logger {}
    private val clientService = HttpClientService()
    init {
        initialLogin()
    }

    private fun initialLogin() {
        logger.info { "Welcome to 'Trade Republic PDF Downloader'" }
        val dotenv = Dotenv.configure().directory(".").load()
        val phoneNumber = dotenv["PHONE"] ?: getUserInput("Please enter your phone number (e.g. +49123456789):", logger) { it.length > 4 }
        val pin = dotenv["PIN"] ?: getUserInput("Please enter your four digit pin (e.g. 1234):", logger) { it.length == 4 }

        val response: Response = clientService.postRequest(
            "https://api.traderepublic.com/api/v1/auth/web/login",
            LoginRequest(phoneNumber, pin)
        )
        if (response.code != 200) {
            throw Exception("Code: ${response.code} Message: ${response.message}")
        }

        val loginResponse: LoginResponse = clientService.transformBufferToObject(response, LoginResponse::class.java)
        twoFactorLogin(loginResponse)
    }

    private fun twoFactorLogin(loginResponse: LoginResponse) {
        val twoFaCode = getUserInput(
            "Please enter the four digit 2FA code you received on your phone (valid for ${loginResponse.countdownInSeconds} seconds):", logger
        ) { it.length == 4 }
        val twoFaResponse = clientService.postRequest<String>("https://api.traderepublic.com/api/v1/auth/web/login/${loginResponse.processId}/$twoFaCode")
        val customHeaders = twoFaResponse.headers.toCustomHeaders()
        val map = transformCookiesToMap(customHeaders.setCookies)
        val sessionToken = map["tr_session"] ?: throw Exception("Invalid Code. No session cookie received")
        processUserInput(sessionToken)
    }

    fun processUserInput(sessionToken: String) {
        logger.info { "Login Successful." }
        val documentInput = getUserInput(
            "Please enter the document type you're looking for, 'D' for Dividende or 'S' for Sparplan or 'Z' for Zinsen or 'O' for Order:", logger
        ) { it == "D" || it == "S" || it == "Z" || it == "O" }
        var selectedMonth = getUserInput(
            "Please enter year and month you are interested in (e.g. 2024-03). Or leave it empty for using the current month:", logger
        ) { isMonthValid(it) || it.isEmpty() }
        if (selectedMonth.isEmpty()) {
            selectedMonth = getCurrentMonth(Pattern.PARTIAL)
        }
        val selectedMonthDate = YearMonth.parse(selectedMonth, DateTimeFormatter.ofPattern(Pattern.PARTIAL.patternString))
        TradeRepublicDownloadService(sessionToken, getEventFilter(documentInput, selectedMonthDate), this)
            .createNewSubRequest(TimelineTransactionsRequest(sessionToken))
    }

    private fun getEventFilter(documentInput: String, selectedMonth: YearMonth): EventFilter {
        return when (documentInput) {
            "D" -> { EventFilter(DividendFilter, selectedMonth) }
            "S" -> { EventFilter(SavingPlanFilter, selectedMonth) }
            "Z" -> { EventFilter(InterestFilter, selectedMonth) }
            "O" -> { EventFilter(OrderFilter, selectedMonth) }
            else -> { throw IllegalStateException("Invalid document type") }
        }
    }
}