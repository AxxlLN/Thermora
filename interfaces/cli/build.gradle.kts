plugins {
    kotlin("jvm") version "2.4.21"
    application
}

group = "com.thermora"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.thermora.cli.MainKt")
    applicationName = "thermora"
}

tasks.test {
    useJUnitPlatform()
}
