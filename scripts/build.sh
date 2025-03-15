#!/bin/bash
PT_VERSION=4.0.0-alpha1
# from project root
./gradlew publishToMavenLocal
cp scripts/maven_bundle.sh ~/.m2/repository/com/paytheory/android/sdk/AndroidSDK/"$PT_VERSION"/.

cd ~/.m2/repository/com/paytheory/android/sdk/AndroidSDK/"$PT_VERSION"
bash maven_bundle.sh
rm maven_bundle*
rm AndroidSDK*module*
cd ~/.m2/repository
zip release.zip com/paytheory/android/sdk/AndroidSDK/"$PT_VERSION"/*
