plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.thermora:persistence-memory:0.1.0-SNAPSHOT")
}

kotlin {
    jvmToolchain(21)
}
