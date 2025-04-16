#!/bin/bash
# Script to run gradle with Java 17

# Store original JAVA_HOME
ORIGINAL_JAVA_HOME=$JAVA_HOME

# Set JAVA_HOME to Java 17
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home

# Echo the Java version
echo "Using Java version:"
$JAVA_HOME/bin/java -version

# Run Gradle with the command line arguments
./gradlew "$@"

# Restore original JAVA_HOME
export JAVA_HOME=$ORIGINAL_JAVA_HOME 