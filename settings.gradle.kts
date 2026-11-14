pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex(".*android.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kmp-production-playbook"

include(":shared")
