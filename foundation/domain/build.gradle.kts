plugins {
    kotlin("jvm")
}

group = "com.thermora.foundation"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":foundation:units"))
    implementation(project(":foundation:common"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}