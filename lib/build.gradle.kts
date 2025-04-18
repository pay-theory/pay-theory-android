import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("jacoco")
}

val localProperties = Properties().apply {
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    namespace = "com.paytheory.lib"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Add this to help with native library loading in tests
        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
    }

    // Configure tests
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
            all { 
                it.systemProperty("robolectric.dependency.repo.url", "https://repo1.maven.org/maven2")
                it.systemProperty("robolectric.dependency.repo.id", "mavenCentral")
                it.systemProperty("javax.net.ssl.trustStoreType", "JKS")
                // Disable native library validation for tests that need it
                it.systemProperty("jna.nosys", "true") 
            }
        }
    }

    buildTypes {
        debug {
            resValue(
                "string",
                "google_project_number",
                localProperties.getProperty("GOOGLE_PROJECT_NUMBER")
            )
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            resValue(
                "string",
                "google_project_number",
                localProperties.getProperty("GOOGLE_PROJECT_NUMBER")
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    packaging {
        resources {
            pickFirsts.add("META-INF/gradle/incremental.annotation.processors")
        }
    }

    // Enable explicit compilation of annotation processors
    kapt {
        correctErrorTypes = true
        includeCompileClasspath = false
        useBuildCache = true
    }
}

dependencies {

//Compose bom
    implementation(platform("androidx.compose:compose-bom:2025.04.00"))
    implementation("androidx.appcompat:appcompat:1.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.00"))

    // Google Pay Button for Jetpack Compose
    implementation("com.google.pay.button:compose-pay-button:1.1.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:2.1.20")
    implementation("androidx.multidex:multidex:2.0.1")

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.ui:ui-util")
    implementation("androidx.compose.material3:material3:1.4.0-alpha12")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("com.google.dagger:hilt-android:2.56.2")
    kapt("com.google.dagger:hilt-compiler:2.56.2")

    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.10")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    implementation("com.google.code.gson:gson:2.13.0")

    //Google Play services
    implementation("com.google.android.gms:play-services-location:21.3.0")
    //AWS Android SDK
    implementation("com.amazonaws:aws-android-sdk-kms:2.79.0")

    //Google Play Integrity Api
    implementation("com.google.android.play:integrity:1.4.0")

    //Google Pay API
    implementation("com.google.android.gms:play-services-wallet:19.4.0")

    //Okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    //Lazy sodium
    implementation("com.goterl:lazysodium-android:5.1.0@aar") //5.1.0
    implementation("net.java.dev.jna:jna:5.17.0@aar")
    
    // Timber logging library
    implementation("com.jakewharton.timber:timber:5.0.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("io.coil-kt:coil-compose:2.7.0")

    // https://mvnrepository.com/artifact/androidx.hilt/hilt-navigation-compose
    runtimeOnly("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("androidx.compose.material3:material3:1.3.2") //or the latest version
    implementation("androidx.compose.material:material:1.7.8")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.14.0")
    testImplementation("org.mockito:mockito-core:5.17.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    testImplementation("org.mockito:mockito-core:5.17.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.robolectric:robolectric:4.14.1") // Re-enabled Robolectric
    testImplementation("org.powermock:powermock-module-junit4:2.0.9")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.compose.ui:ui-test")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Test dependencies
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test.ext:junit-ktx:1.2.1")
    testImplementation("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation("io.mockk:mockk-android:1.13.8")
//    testImplementation("io.mockk:mockk-agent:1.14.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
}

// JaCoCo configuration
jacoco {
    toolVersion = "0.8.8"
}

// Configure the standard test task for coverage
tasks.withType<Test> {
    extensions.configure<JacocoTaskExtension> {
        isEnabled = true
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
    
    // Force tests to run every time
    outputs.upToDateWhen { false }
}

// Consolidated coverage report task for Android tests
tasks.register<JacocoReport>("jacocoTestReport") {
    description = "Generates JaCoCo coverage report for Android tests"
    group = "Verification"
    
    dependsOn("testDebugUnitTest")
    
    // Don't require execution data to exist - helps with first-time runs
    val execFile = layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec").get().asFile
    
    // Always generate the report, even if no exec file
    executionData.setFrom(files(execFile).filter { it.exists() })
    
    val mainSrc = "${project.projectDir}/src/main/java"
    
    // Use more reliable approach to find class files
    // This searches all potential class output directories
    val javaClasses = fileTree(layout.buildDirectory) {
        include(
            "intermediates/javac/debug/classes/**/*.class",
            "intermediates/classes/debug/**/*.class",
            "tmp/kotlin-classes/debug/**/*.class"
        )
        exclude(
            // Standard exclusions
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**",
            "**/Lambda*",
            "**/*Lambda.class",
            "**/*Lambda*.class",
            "**/generated/**",
            "**/dagger/**",
            "**/hilt/**",
            
            // Excluded packages as requested
            "**/com/paytheory/lib/nacl/**",
            "**/com/paytheory/lib/compose/**",
            "**/com/paytheory/lib/compose/inputs/**"
        )
    }
    
    classDirectories.setFrom(javaClasses)
    sourceDirectories.setFrom(mainSrc)
    
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("jacoco/html"))
    }
    
    doFirst {
        // Create empty file to ensure report generation
        if (!execFile.exists()) {
            project.mkdir(execFile.parentFile)
            execFile.createNewFile()
        }
        logger.lifecycle("JaCoCo execution data file: ${execFile.absolutePath}, exists: ${execFile.exists()}")
        
        // Print out class directories for debugging
        logger.lifecycle("Class files found: ${classDirectories.files.size}")
        classDirectories.files.forEach { 
            logger.lifecycle("Searching for classes in: ${it}")
            if (it.exists()) {
                logger.lifecycle("Directory exists: ${it.absolutePath} with ${it.walk().filter { f -> f.isFile && f.name.endsWith(".class") }.count()} class files")
            }
        }
    }
}

// Simple task that opens the coverage report in browser
tasks.register("openJacocoReport") {
    description = "Opens the JaCoCo coverage report in the default browser"
    group = "Verification"
    dependsOn("jacocoTestReport")
    
    doLast {
        providers.exec {
            workingDir("${getLayout().buildDirectory}/jacoco/html")
            commandLine("open", "index.html")
        }
    }
}
