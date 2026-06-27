val ktorVersion: String by project
val logbackVersion: String by project
val kotlinxSerializationVersion: String by project

plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "1.9.21"
    id("io.ktor.plugin") version "3.5.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    // HTTP client for Whisper API
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
//    implementation("io.ktor:ktor-server-slf4j:2.3.6")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
//    testImplementation("kotlin.test:kotlin.test-junit:1.9.21")
}

application {
    mainClass.set("org.example.MainKt")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}