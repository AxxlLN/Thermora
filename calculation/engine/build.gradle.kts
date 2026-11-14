plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    api(project(":calculation:api"))
    api(project(":foundation:common"))
    testImplementation(project(":models:sealed-housing-legacy"))
    testImplementation(project(":foundation:domain"))
    testImplementation(project(":foundation:units"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
