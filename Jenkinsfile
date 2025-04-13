pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Clone your GitHub repository
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    // Install dependencies and build the package (skip tests for now)
                    sh 'mvn clean package -DskipTests'
                }
            }
        }



        stage('Package') {
            steps {
                script {
                    // Create final JAR file
                    sh 'mvn package -DskipTests'
                    // Archive the built JAR
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        stage('SonarQube Analysis') {
        steps{
        script {
        def scannerHome = tool 'scanner'
        withSonarQubeEnv {
        sh "${scannerHome}/bin/sonar-scanner"
        }
        }
        }
        } 
    }

    post {
        always {
            echo 'Pipeline completed'
            // Clean up workspace if needed
            cleanWs()
        }
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}