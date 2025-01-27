package com.tr

import com.tr.login.service.LoginService
import io.github.cdimascio.dotenv.Dotenv
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
class TradeRepublicDownloaderApplication(
	@Autowired private val loginService: LoginService
) : CommandLineRunner {
	override fun run(vararg args: String?) = loginService.initializeLogin()
}

fun main(args: Array<String>) {
	Dotenv
		.configure()
		.ignoreIfMissing()
		.systemProperties()
		.load()
	System.setProperty("java.awt.headless", "false")
	runApplication<TradeRepublicDownloaderApplication>(*args)
}
