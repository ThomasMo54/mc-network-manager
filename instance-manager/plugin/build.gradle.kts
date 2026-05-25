plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":api"))

    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}