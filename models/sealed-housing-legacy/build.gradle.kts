plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    api(project(":foundation:common"))
    api(project(":foundation:units"))
    api(project(":foundation:domain"))
    api(project(":calculation:api"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
