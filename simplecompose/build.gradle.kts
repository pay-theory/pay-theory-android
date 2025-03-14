
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
val localProperties = Properties().apply {
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}
android {
    namespace = "com.paytheory.simplecompose"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.paytheory.simplecompose"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "1.07"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            resValue("string", "api_key", "${localProperties.getProperty("API_KEY")}")
//            isMinifyEnabled = true
//
//            // Enables resource shrinking, which is performed by the
//            // Android Gradle plugin.
//            isShrinkResources = true
//            proguardFiles(
//                // Includes the default ProGuard rules files that are packaged with
//                // the Android Gradle plugin. To learn more, go to the section about
//                // R8 configuration files.
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//
//                // Includes a local, custom Proguard rules file
//                "proguard-rules.pro"
//            )
        }
        release {
            isDebuggable = false
            resValue("string", "api_key", "${localProperties.getProperty("API_KEY")}")
            // Enables code shrinking, obfuscation, and optimization for only
            // your project's release build type. Make sure to use a build
            // variant with `isDebuggable=false`.
            isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the
            // Android Gradle plugin.
            isShrinkResources = true

            proguardFiles(
                // Includes the default ProGuard rules files that are packaged with
                // the Android Gradle plugin. To learn more, go to the section about
                // R8 configuration files.
                getDefaultProguardFile("proguard-android-optimize.txt"),

                // Includes a local, custom Proguard rules file
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(project(":lib"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.03.00"))
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.ui:ui-graphics:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.compose.material3:material3:1.4.0-alpha10")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")
}