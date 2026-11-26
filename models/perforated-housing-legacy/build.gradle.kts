plugins { kotlin("jvm"); `java-library` }
dependencies {
    api(project(":calculation:api"))
    api(project(":foundation:domain"))
    api(project(":foundation:units"))
    implementation(project(":foundation:common"))
    testImplementation(kotlin("test"))
}
kotlin { jvmToolchain(21) }
tasks.test { useJUnitPlatform() }
