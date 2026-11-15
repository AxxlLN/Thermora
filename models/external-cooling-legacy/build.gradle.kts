plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":calculation:api"))
    api(project(":foundation:domain"))
    api(project(":foundation:units"))

    implementation(project(":calculation:validation"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
