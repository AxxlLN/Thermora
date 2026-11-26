plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    api(project(":application:ports"))
    api(project(":models:perforated-housing-legacy"))
    api(project(":models:forced-cooling-legacy"))
    api(project(":models:sealed-housing-legacy"))
    api(project(":models:internal-mixing-legacy"))
    api(project(":models:external-cooling-legacy"))
    api(project(":models:finned-housing-legacy"))
    testImplementation(project(":application:core"))
    testImplementation(project(":infrastructure:persistence-memory"))
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