# Determining Build Readiness

_these instructions come from [jitpack](https://jitpack.io/docs/ANDROID/#create-your-release)_

 * ensure you can run the following successfully
    * ./gradlew AndroidSDK:publishToMavenLocal
    * ./gradlew AndroidSDK:compileReleaseKotlin
 * ensure you have the same version of Kotlin
    * Top level build ext.kotlin_version
    * ./gradlew -v
    * Android Gradle Plugin Version (see project structure)
