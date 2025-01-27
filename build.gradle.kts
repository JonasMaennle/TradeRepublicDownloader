plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.3"
	id("io.spring.dependency-management") version "1.1.6"
	id("edu.sc.seis.launch4j") version "3.0.6"
	application
}

group = "com.tr"
version = "1.0.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-logging")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("io.github.cdimascio:dotenv-kotlin:6.5.0")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

application {
	mainClass = "com.tr.TradeRepublicDownloaderApplicationKt"
}

launch4j {
	icon = "${projectDir}/src/main/resources/images/launcher-icon.ico"
	headerType = "console"
	version = "${project.version}"
	textVersion = "${project.version}"
	jarFiles = project.tasks.named("bootJar").map { it.outputs.files }
	outputDir = "$projectDir/build"
}

tasks.named<JavaExec>("bootRun") {
	standardInput = System.`in`
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("createExe") {
	doNotTrackState("Awaiting folder")
	dependsOn("build")
}
