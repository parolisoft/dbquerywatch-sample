@file:Suppress("SpellCheckingInspection")

import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    application

    id("io.freefair.lombok") version "8.1.0"
    id("io.spring.dependency-management") version "1.1.2"
    id("org.springframework.boot") version "2.7.14"

    id("flyway-jooq")
}

group = "org.dbquerywatch"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val jooqVersion: String by project
ext["jooq.version"] = jooqVersion
val testcontainersVersion: String by project

repositories {
    mavenCentral()
}

dependencyManagement {
    dependencies {
        imports {
            mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
        }
        dependencySet("org.mapstruct:1.5.5.Final") {
            entry("mapstruct")
            entry("mapstruct-processor")
        }
    }
}

dependencies {
    implementation("org.mapstruct", "mapstruct")
    annotationProcessor("org.mapstruct", "mapstruct-processor")

    implementation("com.google.code.findbugs", "jsr305", "3.0.2")
    implementation("org.jooq", "jooq")
    implementation("org.springframework.boot", "spring-boot")
    implementation("org.springframework.boot", "spring-boot-starter-jooq")
    implementation("org.springframework.boot", "spring-boot-starter-web")

    runtimeOnly("org.flywaydb", "flyway-core")
    runtimeOnly("org.postgresql", "postgresql")

    testImplementation("com.tngtech.archunit", "archunit-junit5", "1.0.1") {
        exclude("org.slf4j", "slf4j-api")  // to avoid conflict with version 1.7.x from SB 2.x
    }
    testImplementation("com.parolisoft", "archunit-hexagonal", "0.1.0")
    testImplementation("org.dbquerywatch", "dbquerywatch", "1.0.0")
    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.testcontainers", "junit-jupiter")
    testImplementation("org.testcontainers", "postgresql")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "--release", "8",
        "-Xlint:deprecation",
        "-Xlint:unchecked",
        "-Werror",
    ))
}

tasks.compileJava {
    options.compilerArgs.addAll(listOf(
        "-Amapstruct.suppressGeneratorTimestamp=true",
        "-Amapstruct.suppressGeneratorVersionInfoComment=true",
    ))
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED)
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}
