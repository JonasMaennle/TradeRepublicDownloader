package com.tr.login.service.playwright

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import com.tr.login.models.LoginResponse
import com.tr.login.models.LoginDataWrapper
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PlaywrightService(
    @Autowired private val objectMapper: ObjectMapper,
) {
    private lateinit var playwright: Playwright
    private lateinit var browser: Browser

    fun performTRLogin(phoneNumber: String, pin: String): LoginDataWrapper {
        startPlaywright()
        val context = browser.newContext()
        val page = context.newPage()

        try {
            page.navigate("https://app.traderepublic.com/login")

            enterPhone(page, phoneNumber)

            val loginResponse = page.waitForResponse(
                { response ->
                    response.url().contains("/api/v1/auth/web/login") &&
                            response.request().method() == "POST"
                }
            ) {
                enterPin(page, pin)
            }

            logger.debug("Login data: " + loginResponse.text())
            val loginData = objectMapper.readValue<LoginResponse>(loginResponse.text())
            return LoginDataWrapper(loginData, loginResponse.request().headers()["x-aws-waf-token"] ?: "")
        } finally {
            shutdown()
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