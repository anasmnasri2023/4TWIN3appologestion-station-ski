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
                    echo "SonarQube analysis temporarily disabled due to server issues"
                    // Uncomment the below code when SonarQube server is fixed
                    /*
                    try {
                        def scannerHome = tool 'scanner'
                        withSonarQubeEnv('scanner') {
                            sh """
                                ${scannerHome}/bin/sonar-scanner \
                                -Dsonar.projectKey=station-ski \
                                -Dsonar.sources=. \
                                -Dsonar.java.binaries=target/classes \
                                -Dsonar.host.url=http://192.168.70.47:9000
                            """
                        }
                    } catch (Exception e) {
                        echo "SonarQube analysis failed: ${e.message}"
                        // Continue pipeline
                    }
                    */
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

        stage('Docker Diagnostics') {
            steps {
                script {
                    sh 'echo "Docker client version:"'
                    sh 'docker --version || true'
                    sh 'echo "Docker compose version:"'
                    sh 'docker-compose --version || true'
                    sh 'echo "Docker info:"'
                    sh 'docker info || true'
                    sh 'echo "Docker socket:"'
                    sh 'ls -la /var/run/docker.sock || true'
                    sh 'echo "Daemon configuration:"'
                    sh 'cat /etc/docker/daemon.json || true'
                    sh 'echo "Jenkins user groups:"'
                    sh 'id jenkins || true'
                }
            }
        }

        stage('Building Docker images (springboot and mysql)') {
            steps {
                script {
                    try {
                        // Simple approach - use sudo without password if configured
                        sh 'sudo chmod 666 /var/run/docker.sock || echo "Failed to set permissions, continuing anyway"'

                        // Build with docker-compose
                        sh 'docker-compose build'
                    } catch (Exception e) {
                        echo "Docker build failed: ${e.message}"
                        echo "You may need to manually fix Docker permissions on the Jenkins server"
                        // Display docker-compose file for debugging
                        sh 'cat docker-compose.yml || echo "No docker-compose.yml found"'
                        throw e
                    }
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                script {
                    try {
                        // Verify Nexus connectivity
                        sh "curl -m 5 -s -o /dev/null -w '%{http_code}' http://${registry} || echo 'Could not connect to Nexus'"

                        // Login to Nexus and push image
                        sh "echo '${NEXUS_PASSWORD}' | docker login http://${registry} -u admin --password-stdin"
                        // Modify tags to include http:// prefix explicitly
                        sh "docker tag springbootapp:latest http://${registry}/springbootapp:1.0 || echo 'Failed to tag image'"
                        sh "docker push http://${registry}/springbootapp:1.0 || echo 'Failed to push image'"
                    } catch (Exception e) {
                        echo "Nexus deployment failed: ${e.message}"
                        // Display docker images to verify
                        sh 'docker images | grep springbootapp || echo "No springbootapp image found"'
                        throw e
                    }
                }
            }
        }

        stage('Run Application') {
            steps {
                script {
                    sh "docker pull ${registry}/springbootapp:1.0 || echo 'Failed to pull image'"
                    sh 'docker-compose down || echo "No containers to stop"'
                    sh 'docker-compose up -d'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed'
            sh 'docker logout ${registry} || echo "Not logged into registry"'
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