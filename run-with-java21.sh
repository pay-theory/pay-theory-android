#!/bin/bash
# Script to run gradle with Android Studio's JDK

# Store original JAVA_HOME
ORIGINAL_JAVA_HOME=$JAVA_HOME

# Set JAVA_HOME to Android Studio's JDK
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Echo the Java version
echo "Using Java version:"
"$JAVA_HOME/bin/java" -version

# Run Gradle with the command line arguments
./gradlew "$@"

# Restore original JAVA_HOME
export JAVA_HOME=$ORIGINAL_JAVA_HOME 