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
    implementation(project(":application:core"))
    implementation(project(":infrastructure:serialization"))

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