package com.tr.login.service.playwright

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.playwright.*
import com.microsoft.playwright.options.WaitForSelectorState
import com.tr.login.models.LoginSession
import com.tr.login.models.ProcessResponse
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PlaywrightService(
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Value("\${tr.api.login:}")
    private lateinit var loginUrl: String

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser

    fun performLogin(phoneNumber: String, pin: String): LoginSession {
        logger.info("Initialize login...")
        startPlaywright()

        val context = browser.newContext()
        val page = context.newPage()

        page.navigate(loginUrl)

        page.locator("input[name='username']")
            .fill(phoneNumber)

        page.locator("input[type='password']")
            .fill(pin)

        val submitButton = page.locator("button[type='submit']")

        submitButton.waitFor(
            Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
        )

        val loginResponse = page.waitForRequest(
            { request ->
                request.url().contains("/api/v2/auth/web/login") &&
                        request.method() == "POST"
            }
        ) {
            submitButton.click()
        }

        val body = loginResponse.response().text()
        logger.debug("Login response: $body")

        val processResponse = objectMapper.readValue<ProcessResponse>(body)
        waitForConfirmation(page, processResponse.processId)

        return LoginSession(
            trSession = extractTrSession(context)
        )
    }

    private fun extractTrSession(context: BrowserContext): String {
        val cookies = context.cookies()
        cookies.forEach {
            logger.debug("${it.name}=${it.value}")
        }
        val trSession = cookies.first { it.name == "tr_session" }.value ?: throw IllegalStateException("No tr_session cookie found")
        shutdown()
        return trSession
    }

    private fun waitForConfirmation(
        page: Page,
        processId: String
    ) {
        while (true) {
            val response = page.waitForResponse(
                { response ->
                    response.url()
                        .contains("/api/v2/auth/web/login/processes/$processId") &&
                            response.request().method() == "GET"
                }
            ) {
                // no-op
            }

            val body = response.text()
            logger.debug("Polling response: $body")

            if (body.contains("CONFIRMED")) {
                logger.info("Login confirmed")
                return
            }
        }
    }

    private fun startPlaywright() {
        playwright = Playwright.create()

        browser = playwright.chromium().launch(
            BrowserType.LaunchOptions()
                .setHeadless(false)
        )
    }

    @PreDestroy
    fun shutdown() {
        browser.close()
        playwright.close()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PlaywrightService::class.java)
    }
}