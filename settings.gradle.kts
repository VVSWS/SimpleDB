pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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
    /*versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }*/
}

rootProject.name = "CarFault"
include(":app")
include(":data")
include(":core")
include(":domain")
include(":presentation")
