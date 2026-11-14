plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":foundation:common"))
    implementation(project(":foundation:units"))
    implementation(project(":foundation:domain"))
    implementation(project(":calculation:api"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}