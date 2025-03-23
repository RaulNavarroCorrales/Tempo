pluginManagement {
    repositories {
        google()         // Repositorio de Google para plugins
        mavenCentral()   // Repositorio de Maven Central
        gradlePluginPortal()
        jcenter()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Permite repositorios en build.gradle.kts
    repositories {
        google()       // Repositorio de Google
        mavenCentral() // Repositorio de Maven Central
    }
}

rootProject.name = "Project"
include(":app")
