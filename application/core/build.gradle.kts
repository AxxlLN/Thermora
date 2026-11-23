plugins {
    kotlin("jvm")
    `java-library`
    jacoco
}

dependencies {
    api(project(":calculation:api"))
    api(project(":foundation:common"))
    api(project(":application:ports"))

    implementation(project(":calculation:engine"))

    api(project(":models:sealed-housing-legacy"))
    api(project(":models:internal-mixing-legacy"))
    api(project(":models:external-cooling-legacy"))
    api(project(":models:finned-housing-legacy"))

    testImplementation(kotlin("test"))
    testImplementation(testFixtures(project(":application:ports")))
    testImplementation(project(":foundation:domain"))
    testImplementation(project(":foundation:units"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
