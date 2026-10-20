pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    includeBuild("build-logic")
}

rootProject.name = "ModernTerrainGenerator"

include(
    "common:common-util",
    "common:common-config",
    "common:common-customobject",
    "common:common-generator",
    "common:common-core",
    "platforms:paper",
)
