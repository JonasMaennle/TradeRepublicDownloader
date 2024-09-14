package com.tr

import com.tr.login.service.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TradeRepublicDownloaderApplication(
	@Autowired private val loginService: LoginService
) : CommandLineRunner {
	override fun run(vararg args: String?) = loginService.initializeLogin()
}

fun main(args: Array<String>) {
	System.setProperty("java.awt.headless", "false")
	runApplication<TradeRepublicDownloaderApplication>(*args)
}
