plugins {
    kotlin("jvm") version libs.versions.kotlin
}

group = "com.motompro"
version = providers.gradleProperty("releaseVersion").getOrElse("1.0-SNAPSHOT")

allprojects {
    repositories {
        mavenCentral()
        // PaperMC
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
