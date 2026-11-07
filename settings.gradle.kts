pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VapeConfig"
include(":app")
include(":core:database", ":core:calculator", ":core:compatibility", ":core:updater")
include(":feature:catalog", ":feature:configurator", ":feature:liquids", ":feature:settings", ":feature:batteries")
