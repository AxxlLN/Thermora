plugins {
    kotlin("jvm")
    `java-library`
    `java-test-fixtures`
    jacoco
}

dependencies {
    api(project(":calculation:api"))
    api(project(":foundation:common"))
    testImplementation(kotlin("test"))
    testFixturesApi(kotlin("test-junit5"))
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
