
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}
val localProperties = Properties().apply {
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}
android {
    namespace = "com.paytheory.qualitydemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.paytheory.qualitydemo"
        minSdk = 28
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
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.16.0")

    // AndroidX Activity and Fragment
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.6")

    //AppCompat
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Compose UI
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.material:material:1.7.8")
    implementation("androidx.compose.material3:material3:1.4.0-alpha12")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-graphics:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2025.04.00"))

    //Navigation
    implementation("androidx.navigation:navigation-compose:2.8.9")

    //Activity
    implementation("androidx.activity:activity-compose:1.10.1")

    //ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")



    //testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")

    // Project module
    implementation(project(":lib"))
}