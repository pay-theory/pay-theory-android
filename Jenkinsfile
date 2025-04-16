pipeline {
    agent {
        ecs {
            inheritFrom 'build-android-spot'
        }
    }
    
    parameters {
        booleanParam(name: 'NIGHTLY_BUILD', defaultValue: false, description: 'Is this a nightly build?')
        booleanParam(name: 'RELEASE_BUILD', defaultValue: false, description: 'Is this a release build?')
        string(name: 'MIN_COVERAGE', defaultValue: '80', description: 'Minimum test coverage percentage')
    }
    
    environment {
        ANDROID_HOME = tool 'android-sdk'
        JAVA_HOME = tool 'jdk17'
        PATH = "$JAVA_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH"
        
        // Define environment variables
        IS_PR = env.CHANGE_ID != null ? true : false
        IS_NIGHTLY = params.NIGHTLY_BUILD
        IS_RELEASE = params.RELEASE_BUILD
    }
    
    stages {
        stage('Setup') {
            steps {
                checkout scm
                sh '''
                    # Setup environment
                    echo "Setting up build environment"
                    ./gradlew clean
                '''
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh './gradlew testDebugUnitTest'
            }
            post {
                always {
                    junit '**/build/test-results/testDebugUnitTest/*.xml'
                }
            }
        }
        
        stage('Code Coverage') {
            steps {
                sh '''
                    # Generate JaCoCo reports
                    ./gradlew jacocoTestReport
                    
                    # Verify coverage meets threshold
                    ./gradlew jacocoTestCoverageVerification -PminCoverage=${MIN_COVERAGE}
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: '**/build/reports/jacoco/**', allowEmptyArchive: true
                }
            }
        }
        
        stage('Lint Checks') {
            steps {
                sh './gradlew lint'
            }
            post {
                always {
                    archiveArtifacts artifacts: '**/build/reports/lint-results*.html', allowEmptyArchive: true
                }
            }
        }
        
        stage('Instrumentation Tests') {
            when {
                expression { return params.NIGHTLY_BUILD || params.RELEASE_BUILD }
            }
            steps {
                sh '''
                    # Create and start an AVD for testing
                    echo "no" | avdmanager create avd -n test_avd -k "system-images;android-29;google_apis;x86_64" -f
                    emulator -avd test_avd -no-window -no-audio -no-boot-anim &
                    
                    # Wait for emulator to start
                    adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 2; done'
                    
                    # Run instrumentation tests
                    ./gradlew connectedDebugAndroidTest
                '''
            }
            post {
                always {
                    junit '**/build/outputs/androidTest-results/connected/*.xml'
                    archiveArtifacts artifacts: '**/build/reports/androidTests/**', allowEmptyArchive: true
                }
                cleanup {
                    sh 'adb devices | grep emulator | cut -f1 | while read device; do adb -s $device emu kill; done || true'
                }
            }
        }
        
        stage('Build Debug APK') {
            steps {
                sh './gradlew assembleDebug'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/outputs/**/*.apk', allowEmptyArchive: true
                }
            }
        }
        
        stage('Release Build') {
            when {
                expression { return params.RELEASE_BUILD }
            }
            steps {
                sh './gradlew assembleRelease'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/outputs/**/*.aar', allowEmptyArchive: true
                }
            }
        }
    }
    
    post {
        always {
            // Archive test reports
            archiveArtifacts artifacts: '**/build/reports/**', allowEmptyArchive: true
            
            // Clean workspace
            cleanWs()
        }
        success {
            echo 'Build and tests completed successfully!'
        }
        failure {
            echo 'Build or tests failed'
            
            // Send email notification on failure
            mail to: 'dev-team@paytheory.com',
                 subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                 body: "Build or tests failed: ${env.BUILD_URL}"
        }
    }
} 