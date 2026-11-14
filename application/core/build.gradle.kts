plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    api(project(":calculation:api"))
    api(project(":foundation:common"))

    implementation(project(":calculation:engine"))
    implementation(project(":models:sealed-housing-legacy"))

    testImplementation(kotlin("test"))
    testImplementation(project(":foundation:domain"))
    testImplementation(project(":foundation:units"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}