plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Thermora"

include(
    ":foundation:common",
    ":foundation:units",
    ":foundation:domain",
    ":calculation:api",
    ":calculation:engine"
)
