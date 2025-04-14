pipeline {
    agent any

    environment {
        registryCredentials = "nexus"
        registry = "192.168.70.47:8083"
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

        stage('SonarQube Analysis') {
            steps {
                script {
                    // Use Maven's sonar integration directly without scanner tool
                    withSonarQubeEnv(credentialsId: 'scanner', installationName: 'scanner') {
                        sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=instructor-devops \
                        -Dsonar.projectName="Gestion Station Ski" \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_AUTH_TOKEN}
                        '''
                    }

                    // Wait for SonarQube analysis to complete
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: false
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

        stage('Deploy to Nexus') {
            steps {
                script {
                    docker.withRegistry("http://${registry}", registryCredentials) {
                        sh('docker push 192.168.70.47:8083/springbootapp:1.0')
                    }
                }
            }
        }

        stage('Run Application') {
            steps {
                script {
                    docker.withRegistry("http://${registry}", registryCredentials) {
                        sh "docker pull ${registry}/springbootapp:1.0"
                        sh 'docker-compose down'   // Stop and remove old containers
                        sh 'docker-compose up -d'   // Run fresh
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