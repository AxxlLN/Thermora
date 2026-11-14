rootProject.name = "thermora-public-api-smoke"

includeBuild("../..")

include("api-consumer", "domain-consumer", "model-consumer", "engine-consumer", "core-consumer", "default-model-consumer")
