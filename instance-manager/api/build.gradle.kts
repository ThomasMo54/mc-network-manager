plugins {
    kotlin("jvm")
    `maven-publish`
}

group = rootProject.group
version = rootProject.version

dependencies {
    compileOnly(libs.velocity.api)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "instance-manager-api"
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY") ?: "ThomasMo54/mc-network-manager"}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
