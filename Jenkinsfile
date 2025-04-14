pipeline {
    agent any

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

       stage('SonarQube Analysis') {
           steps {
               script {
                   def scannerHome = tool 'sonar' // Changer 'scanner' en 'sonar' pour correspondre au nom de l'installation
                   withSonarQubeEnv('sonar') { // Changer aussi ici pour correspondre
                       sh """
                       ${scannerHome}/bin/sonar-scanner \\
                       -Dsonar.projectKey=instructor-devops \\
                       -Dsonar.sources=. \\
                       -Dsonar.java.binaries=target/classes \\
                       -Dsonar.host.url=http://192.168.33.10:9000 \\ // URL explicite plutôt que variable
                       -Dsonar.login=TOKEN_ICI // Remplacer par votre token réel ou utiliser un credential Jenkins
                       """
                   }
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