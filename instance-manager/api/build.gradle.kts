plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    compileOnly(libs.velocity.api)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}