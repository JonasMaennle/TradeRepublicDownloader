package com.tr

import com.tr.model.response.LoginResponse
import com.tr.model.request.LoginRequest
import com.tr.model.request.TimelineRequest
import com.tr.service.*
import com.tr.utils.*
import io.github.cdimascio.dotenv.Dotenv
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.Response
import java.lang.Exception
import java.time.YearMonth
import java.time.format.DateTimeFormatter

fun main() {
    Login()
}

class Login {
    private val logger = KotlinLogging.logger {}
    private val clientService = HttpClientService()
    init {
        initialLogin()
    }

    private fun initialLogin() {
        logger.info { "Starting Trade Republic 'Sparplan' downloader..." }
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
        val documentInput = getUserInput(
            "Please enter the document type you're looking for, 'D' for Dividende or 'S' for Sparplan or 'Z' for Zinsen:", logger
        ) { it == "D" || it == "S" || it == "Z" }
        var selectedMonth = getUserInput(
            "Please enter year and month you are interested in (e.g. 2024-03). Or leave it empty for using the current month:", logger
        ) { isMonthValid(it) || it.isEmpty() }
        if (selectedMonth.isEmpty()) {
            selectedMonth = getCurrentMonth(Pattern.PARTIAL)
        }
        val selectedMonthDate = YearMonth.parse(selectedMonth, DateTimeFormatter.ofPattern(Pattern.PARTIAL.patternString))

        val twoFaResponse = clientService.postRequest<String>("https://api.traderepublic.com/api/v1/auth/web/login/${loginResponse.processId}/$twoFaCode")
        val customHeaders = twoFaResponse.headers.toCustomHeaders()
        val map = transformCookiesToMap(customHeaders.setCookies)
        val sessionToken = map["tr_session"] ?: throw Exception("Invalid Code. No session cookie received")

        TradeRepublicDownloadService(sessionToken, getEventFilter(documentInput, selectedMonthDate))
            .createNewSubRequest(TimelineRequest(sessionToken))
    }

    private fun getEventFilter(documentInput: String, selectedMonth: YearMonth): EventFilter {
        return when (documentInput) {
            "D" -> { EventFilter(DividendFilter, selectedMonth) }
            "S" -> { EventFilter(SavingPlanFilter, selectedMonth) }
            "Z" -> { EventFilter(InterestFilter, selectedMonth) }
            else -> { throw IllegalStateException("Invalid document type") }
        }
    }
}