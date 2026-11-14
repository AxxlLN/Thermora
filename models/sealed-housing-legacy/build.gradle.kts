plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    api(project(":foundation:common"))
    api(project(":foundation:units"))
    api(project(":foundation:domain"))
    api(project(":calculation:api"))
    implementation(project(":calculation:validation"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
