#!/bin/bash

# Backup the original file
cp lib/build.gradle.kts lib/build.gradle.kts.bak

# Find the JaCoCo configuration section and replace it
cat > temp_config.txt << 'EOL'
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
    
    // Force tests to run every time by setting outputs to not be up-to-date
    outputs.upToDateWhen { false }
}

// Consolidated coverage report task for Android tests
tasks.register<JacocoReport>("jacocoTestReport") {
    description = "Generates JaCoCo coverage report for Android tests"
    group = "Verification"
    
    // Make sure tests are run first and don't skip even if up-to-date
    val testTask = tasks.named("testDebugUnitTest")
    dependsOn(testTask)
    
    // Make sure executionData files exist or will be created
    doFirst {
        executionData.setFrom(files(executionData.files.filter { it.exists() }))
    }
EOL

# Replace the old JaCoCo configuration with the new one
sed -i '' '/^\/\/ JaCoCo configuration/,/^\/\/ Consolidated coverage report task for Android tests/{
    /^\/\/ Consolidated coverage report task for Android tests/!d
}' lib/build.gradle.kts

# Insert the new configuration
sed -i '' '/^\/\/ Consolidated coverage report task for Android tests/i\
'"$(cat temp_config.txt)"'
' lib/build.gradle.kts

# Clean up
rm temp_config.txt

echo "Build file updated successfully!"
echo "Now run: ./gradlew lib:jacocoTestReport" 