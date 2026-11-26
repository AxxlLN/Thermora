plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":foundation:units"))
    implementation(project(":foundation:domain"))
    implementation("com.fasterxml.jackson.core:jackson-core:2.22.3")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}