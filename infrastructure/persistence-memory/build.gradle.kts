plugins {
    kotlin("jvm")
    `java-library`
    jacoco
}

dependencies {
    api(project(":application:ports"))

    testImplementation(kotlin("test"))
    testImplementation(testFixtures(project(":application:ports")))
    testImplementation(project(":application:core"))
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
