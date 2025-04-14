pipeline {
    agent any

    environment {
        registryCredentials = "nexus"
        registry = "192.168.33.10:8083"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    sh 'mvn test'
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    sh 'mvn package -DskipTests'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Building Docker images (springboot and mysql)') {
            steps {
                script {
                    sh 'docker-compose build'
                }
            }
        }

        // Uploading Docker images into Nexus Registry
        stage('Deploy to Nexus') {
            steps {
                script {
                    docker.withRegistry("http://${registry}", registryCredentials) {
                        sh 'docker push $registry/nodemongoapp:5.0'
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed'
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
