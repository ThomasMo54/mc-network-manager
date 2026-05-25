import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.gradleup.shadow") version libs.versions.shadow.get()
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":api"))

    // Velocity
    compileOnly(libs.velocity.api)
    kapt(libs.velocity.api)

    // Jackson
    implementation(libs.jackson)
    implementation(libs.jackson.yaml)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("InstanceManager")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
}