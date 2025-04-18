// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version '8.9.0' apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("com.google.devtools.ksp") version "2.1.10-1.0.31" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.android.library") version '8.9.0' apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10" apply false
}

// Configure Java toolchain for all projects
allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "21"
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }
}

buildscript {
    // Add JaCoCo classpath
    dependencies {
        classpath("org.jacoco:org.jacoco.core:0.8.8")
    }
}

// Apply JaCoCo version to all subprojects
allprojects {
    apply(plugin = "jacoco")

    jacoco {
        toolVersion = "0.8.8"
    }
}
