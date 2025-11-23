plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.alexandr44"
version = "1.0.0"
description = "HeadHunterMonitorBot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2025.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    implementation("org.telegram:telegrambots-spring-boot-starter:6.9.7.1")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Liquibase
    implementation("org.liquibase:liquibase-core")
    implementation("org.xerial:sqlite-jdbc")

    implementation("org.hibernate.orm:hibernate-community-dialects")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

application {
    mainClass.set("com.alexandr44.telegramprinterbot.TelegramPrinterBotApplication")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("com.alexandr44.headhuntermonitorbot.HeadHunterMonitorBotApplication")
    archiveFileName.set("HeadHunterMonitorBot.jar")
}
