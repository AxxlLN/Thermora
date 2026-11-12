plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":foundation:common"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}