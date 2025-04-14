pipeline {
    agent any

    environment {
        registryCredentials = "nexus"
        registry = "192.168.70.47:8083"
        NEXUS_PASSWORD = '455a3956-6a34-4538-acae-df39a3936c1e'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'SarahHenia-4twin3-Apollo-Piste',
                    url: 'https://github.com/anasmnasri2023/4TWIN3appologestion-station-ski.git'
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
                    def scannerHome = tool 'scanner'
                    withSonarQubeEnv('scanner') {
                        sh """
                            ${scannerHome}/bin/sonar-scanner \\
                            -Dsonar.projectKey=station-ski \\
                            -Dsonar.sources=. \\
                            -Dsonar.java.binaries=target/classes \\
                            -Dsonar.host.url=http://192.168.70.47:9000
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

        stage('Deploy to Nexus') {
            steps {
                script {
                    sh "echo '${NEXUS_PASSWORD}' | docker login ${registry} -u admin --password-stdin"
                    sh "docker push ${registry}/springbootapp:1.0"
                }
            }
        }

        stage('Run Application') {
            steps {
                script {
                    sh "docker pull ${registry}/springbootapp:1.0"
                    sh 'docker-compose down || true'
                    sh 'docker-compose up -d'
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