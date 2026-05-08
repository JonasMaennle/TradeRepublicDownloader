package com.tr.login.service.playwright

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
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
        startPlaywright()

        val context = browser.newContext()
        val page = context.newPage()

        page.navigate(loginUrl)

        enterPhone(page, phoneNumber)

        val loginResponse = page.waitForResponse(
            { response ->
                response.url().contains("/api/v2/auth/web/login") &&
                        !response.url().contains("/processes/") &&
                        response.request().method() == "POST"
            }
        ) {
            enterPin(page, pin)
        }

        val body = loginResponse.text()
        logger.debug("Login response: $body")

        val processResponse = objectMapper.readValue<ProcessResponse>(body)
        val processId = processResponse.processId

        waitForConfirmation(page, processId)

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

    private fun enterPhone(page: Page, phoneNumber: String) {
        // handle cookie banner
        val cookieButton = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Auswahl speichern")
        )
        if (cookieButton.isVisible) {
            cookieButton.click()
        }

        // enter number
        val phoneInput = page.locator("#loginPhoneNumber__input")
        phoneInput.waitFor()
        phoneInput.fill(phoneNumber)

        page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Weiter")
        ).click()
    }

    private fun enterPin(page: Page, pin: String) {
        val pinField = page.locator("#loginPin__input")
        pinField.waitFor()
        pinField.click()
        pinField.pressSequentially(
            pin,
            Locator.PressSequentiallyOptions()
                .setDelay(30.0)
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