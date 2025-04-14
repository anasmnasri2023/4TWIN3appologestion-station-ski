pipeline {
    agent any

    tools {
        maven 'M2_HOME'    // Make sure this matches your Jenkins configuration
        jdk 'JAVA_HOME'    // Make sure this matches your Jenkins configuration
    }

    environment {
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        SONARQUBE_SERVER = 'http://192.168.70.47:9000'
        registryCredentials = 'nexus'
        registry = '192.168.70.47:8083'
        NEXUS_PASSWORD = '455a3956-6a34-4538-acae-df39a3936c1e'
    }

    stages {
        stage('Debug Environment') {
            steps {
                sh 'echo "JAVA_HOME: $JAVA_HOME"'
                sh 'echo "PATH: $PATH"'
                sh 'java -version || true'
                sh 'mvn -version || true'
                sh 'echo "Checking SonarQube connectivity..."'
                sh 'curl -v $SONARQUBE_SERVER/api/system/status || true'
                sh 'echo "Checking Nexus connectivity..."'
                sh 'curl -v http://$registry/service/rest/v1/status || true'
            }
        }

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
                    // Using the scanner tool directly (more reliable)
                    def scannerHome = tool 'scanner' // Make sure this matches your Jenkins configuration
                    withSonarQubeEnv('scanner') {
                        sh """
                            ${scannerHome}/bin/sonar-scanner \\
                            -Dsonar.projectKey=station-ski \\
                            -Dsonar.sources=. \\
                            -Dsonar.java.binaries=target/classes \\
                            -Dsonar.host.url=${SONARQUBE_SERVER}
                        """
                    }

                    // Optional: Wait for SonarQube quality gate
                    timeout(time: 2, unit: 'MINUTES') {
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

        stage('Building Docker images') {
            steps {
                script {
                    // Ensure the docker-compose.yml has proper registry path for images
                    sh 'docker-compose build'

                    // Tag the springboot image with registry path if not already done in docker-compose
                    sh 'docker tag springbootapp:latest ${registry}/springbootapp:1.0 || true'
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                script {
                    // Explicit login to Nexus Docker registry
                    sh "echo '${NEXUS_PASSWORD}' | docker login ${registry} -u admin --password-stdin"

                    // Push the image to Nexus
                    sh "docker push ${registry}/springbootapp:1.0"
                }
            }
        }

        stage('Run Application') {
            steps {
                script {
                    // Pull the latest image from Nexus
                    sh "docker pull ${registry}/springbootapp:1.0"

                    // Stop and remove old containers, then start fresh
                    sh 'docker-compose down || true'
                    sh 'docker-compose up -d'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed'
            sh 'docker logout ${registry} || true'
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