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
    }

    // Disable tests for now
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        // Re-enable tests
        // unitTests.all {
        //     it.enabled = false
        // }
    }

    buildTypes {
        debug {
            resValue(
                "string",
                "google_project_number",
                "${localProperties.getProperty("GOOGLE_PROJECT_NUMBER")}"
            )
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            resValue(
                "string",
                "google_project_number",
                "${localProperties.getProperty("GOOGLE_PROJECT_NUMBER")}"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.appcompat:appcompat:1.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.02.00"))

    // Google Pay Button for Jetpack Compose
    implementation("com.google.pay.button:compose-pay-button:1.1.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:2.1.10")
    implementation("androidx.multidex:multidex:2.0.1")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.ui:ui-util")
    implementation("androidx.compose.material3:material3:1.4.0-alpha09")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("com.google.dagger:hilt-android:2.55")
    kapt("com.google.dagger:hilt-compiler:2.55")

    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.9")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    implementation("com.google.code.gson:gson:2.12.1")

    //Google Play services
    implementation("com.google.android.gms:play-services-location:21.3.0")
    //AWS Android SDK
    implementation("com.amazonaws:aws-android-sdk-kms:2.79.0")

    //Google Play Integrity Api
    implementation("com.google.android.play:integrity:1.4.0")

    //Google Pay API
    implementation("com.google.android.gms:play-services-wallet:19.2.1")

    //Okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    //Lazy sodium
    implementation("com.goterl:lazysodium-android:5.1.0@aar") //5.1.0
    implementation("net.java.dev.jna:jna:5.16.0@aar")
    
    // Timber logging library
    implementation("com.jakewharton.timber:timber:5.0.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("io.coil-kt:coil-compose:2.7.0")

    // https://mvnrepository.com/artifact/androidx.hilt/hilt-navigation-compose
    runtimeOnly("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("androidx.compose.material3:material3:1.3.1") //or the latest version
    implementation("androidx.compose.material:material:1.7.8")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.17")
    testImplementation("org.mockito:mockito-core:4.8.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    testImplementation("org.mockito:mockito-core:4.8.1")
    testImplementation("org.mockito:mockito-inline:4.8.1")
    testImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.robolectric:robolectric:4.11.1") // Re-enabled Robolectric
    testImplementation("org.powermock:powermock-module-junit4:2.0.9")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    androidTestImplementation("androidx.compose.ui:ui-test")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
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
}

// Consolidated coverage report task for Android tests
tasks.register<JacocoReport>("jacocoTestReport") {
    description = "Generates JaCoCo coverage report for Android tests"
    group = "Verification"
    
    dependsOn("testDebugUnitTest")
    
    executionData.from(fileTree(project.buildDir) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("jacoco/testDebugUnitTest.exec")
    })
    
    classDirectories.setFrom(
        fileTree("${buildDir}/tmp/kotlin-classes/debug") {
            // Include only specific packages
            include("**/com/paytheory/lib/configuration/**")
            include("**/com/paytheory/lib/googlepay/**")
            include("**/com/paytheory/lib/utils/**")
            include("**/com/paytheory/lib/valid/**")
            include("**/com/paytheory/lib/model/**")
            include("**/com/paytheory/lib/api/**")
            include("**/com/paytheory/lib/data/**")
            // Only include specific compose packages
            include("**/com/paytheory/lib/compose/string/**")
            include("**/com/paytheory/lib/compose/transformation/**")
            include("**/com/paytheory/lib/compose/utility/**")
            // Add reactors and websocket packages
            include("**/com/paytheory/lib/reactors/**")
            include("**/com/paytheory/lib/websocket/**")
            // Add root lib package files
            include("**/com/paytheory/lib/ContextProvider.class")
            include("**/com/paytheory/lib/Payable.class")
            include("**/com/paytheory/lib/PaymentMethodProcessor.class")
            include("**/com/paytheory/lib/Payment.class")
            include("**/com/paytheory/lib/PaymentMethodToken.class")
            include("**/com/paytheory/lib/PayTheoryConfiguration.class")
            
            // Standard exclusions
            exclude("**/R.class")
            exclude("**/R$*.class")
            exclude("**/BuildConfig.*")
            exclude("**/Manifest*.*")
            exclude("**/*Test*.*")
            exclude("android/**")
            exclude("**/Lambda*")
            exclude("**/*Lambda.class")
            exclude("**/*Lambda*.class")
            exclude("**/*_MembersInjector.class")
            exclude("**/Dagger*Component*.*")
            exclude("**/Dagger*Subcomponent*.*")
            exclude("**/*Module_*Factory.class")
        }
    )
    
    sourceDirectories.setFrom("${project.projectDir}/src/main/java")
    
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("jacoco/html"))
    }
}

// Simple task that opens the coverage report in browser
tasks.register("openJacocoReport") {
    description = "Opens the JaCoCo coverage report in the default browser"
    group = "Verification"
    dependsOn("jacocoTestReport")
    
    doLast {
        exec {
            workingDir("${buildDir}/jacoco/html")
            commandLine("open", "index.html")
        }
    }
}