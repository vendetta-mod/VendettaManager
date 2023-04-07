// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.0-alpha11" apply false
    id("com.android.library") version "8.1.0-alpha11" apply false
    kotlin("android") version "1.7.20" apply false
}

tasks.withType<Copy>().all {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliucord.com/snapshots")
        maven("https://jitpack.io")
    }
}