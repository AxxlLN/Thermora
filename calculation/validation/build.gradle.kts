plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    api(project(":calculation:api"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}