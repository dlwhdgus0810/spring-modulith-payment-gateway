plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"

    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "me.hyunlee"
version = "0.0.1-SNAPSHOT"
description = "laundry"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Spring Modulith
    implementation(platform("org.springframework.modulith:spring-modulith-bom:1.4.4"))
    implementation("org.springframework.modulith:spring-modulith-core")
    implementation("org.springframework.modulith:spring-modulith-events-core")
    implementation("org.springframework.modulith:spring-modulith-events-jpa")
    implementation("org.springframework.modulith:spring-modulith-events-jackson")

    // Coroutions
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.9.0"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // AWS SDK v2 for SES/SNS
    implementation(platform("software.amazon.awssdk:bom:2.38.2"))
    implementation("software.amazon.awssdk:sts")
    implementation("software.amazon.awssdk:ses")
    implementation("software.amazon.awssdk:sns")
    implementation("software.amazon.awssdk:auth")
    implementation("software.amazon.awssdk:regions")

    // libphonenumber
    implementation("com.googlecode.libphonenumber:libphonenumber:9.0.18")

    // stripe
    implementation("com.stripe:stripe-java:30.2.0")

    // MongoDB (for event history)
//    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Micrometer / Prometheus
    implementation("io.micrometer:micrometer-core")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // runtime
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.springframework.boot:spring-boot-docker-compose")
//    runtimeOnly("com.h2database:h2")

    // Redis rate limiting (planned)
    // implementation("org.redisson:redisson-spring-boot-starter:3.37.0")
    // implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:8.10.1")

    // archunit
    // testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.security:spring-security-test")

    // test runtime
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    enabled = false
//    useJUnitPlatform()
}
