rootProject.name = "llmchain"
include(":core")
include(":serviceprovider:gpt4all")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}