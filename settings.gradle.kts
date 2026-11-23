plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Thermora"

include(
    ":foundation:common",
    ":foundation:units",
    ":foundation:domain",
    ":calculation:api",
    ":calculation:engine",
    ":numerical:solvers",
    ":models:sealed-housing-legacy",
    ":application:core",
    ":calculation:validation",
    ":models:internal-mixing-legacy",
    ":models:external-cooling-legacy",
    ":models:finned-housing-legacy",
    ":application:ports",
    ":infrastructure:persistence-memory",
    ":infrastructure:serialization",
    ":interfaces:cli"
)
