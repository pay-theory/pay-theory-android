#!/bin/bash
# Script to run tests first and then generate JaCoCo report

echo "Running tests and generating JaCoCo report..."

# Force test execution by cleaning test outputs first
./gradlew lib:cleanTestDebugUnitTest

# Run the test task with the withType Test configuration
./gradlew lib:testDebugUnitTest

# Now generate the report
./gradlew lib:jacocoTestReport

echo "Done. Check the report at lib/build/jacoco/html/index.html" 