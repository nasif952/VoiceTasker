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
}

rootProject.name = "VoiceTasker"

// Main application module
include(":app")

// Core modules - shared functionality
include(":core:common")
include(":core:database")
include(":core:network")
include(":core:security")

// Feature modules - domain-specific functionality
include(":feature:auth")
include(":feature:task")
include(":feature:voice")
include(":feature:reminder")
include(":feature:sync")
