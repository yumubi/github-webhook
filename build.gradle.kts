plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
}

group = "io.goji"
version = "1.0-SNAPSHOT"
val kotlinCoroutinesVersion = "1.7.3"
repositories {
    mavenCentral()
}

dependencies {
// Vert.x
    implementation(platform("io.vertx:vertx-stack-depchain:4.5.11"))
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-web-client")
    implementation("io.vertx:vertx-lang-kotlin")
    implementation("io.vertx:vertx-lang-kotlin-coroutines")


    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")


    // Telegram Bot
    implementation("org.telegram:telegrambots:6.9.7.0")

    // Jackson for JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.12")

    // Config
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.5")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.7.5")

    // template engine
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
