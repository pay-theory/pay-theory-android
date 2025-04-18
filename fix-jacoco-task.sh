#!/bin/bash

# Backup the original file
cp lib/build.gradle.kts lib/build.gradle.kts.bak2

# Create a temporary file with the jacoco configuration block
cat > jacoco-config.txt << 'EOL'
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
EOL

# Create a temporary file with the doFirst block for jacocoTestReport
cat > jacoco-report-doFirst.txt << 'EOL'
    // Make sure executionData files exist
    doFirst {
        executionData.setFrom(files(executionData.files.filter { it.exists() }))
    }
EOL

# Insert the jacoco configuration before the jacocoTestReport task
awk '
/^\/\/ Consolidated coverage report task for Android tests/ {
    system("cat jacoco-config.txt")
    print
    next
}
{ print }
' lib/build.gradle.kts > lib/build.gradle.kts.new

# Insert the doFirst block after dependsOn
awk '
/^\s*dependsOn\("testDebugUnitTest"\)/ {
    print
    system("cat jacoco-report-doFirst.txt")
    next
}
{ print }
' lib/build.gradle.kts.new > lib/build.gradle.kts

# Clean up temporary files
rm jacoco-config.txt jacoco-report-doFirst.txt lib/build.gradle.kts.new

echo "Build file updated successfully!"
echo "Now run: ./gradlew lib:jacocoTestReport" 