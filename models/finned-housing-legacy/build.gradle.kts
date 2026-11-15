plugins {
    kotlin("jvm") version "2.4.21"
    `java-library`
}

group = "com.thermora"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":calculation:api"))
    implementation(project(":foundation:common"))
    api(project(":foundation:units"))
    api(project(":foundation:domain"))
    implementation(project(":calculation:validation"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
