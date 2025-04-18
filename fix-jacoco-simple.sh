#!/bin/bash

# Backup the original file
cp lib/build.gradle.kts lib/build.gradle.kts.bak

# Add the force test execution to the Test task
echo "Adding force test execution to Test tasks..."
sed -i '' '/tasks.withType<Test> {/a\\n    \// Force tests to run every time\n    outputs.upToDateWhen { false }' lib/build.gradle.kts

# Add doFirst block to the jacocoTestReport task
echo "Adding doFirst block to jacocoTestReport task..."
sed -i '' '/dependsOn("testDebugUnitTest")/a\\n    \// Make sure executionData files exist\n    doFirst {\n        executionData.setFrom(files(executionData.files.filter { it.exists() }))\n    }' lib/build.gradle.kts

echo "Build file updated successfully!"
echo "Now run: ./gradlew lib:jacocoTestReport" 