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
                }
            }
        }
        
        stage('Building Docker images (springboot and mysql)') {
            steps {
                script {
                    try {
                        sh 'docker-compose build'
                    } catch (Exception e) {
                        echo "Docker build failed: ${e.message}"
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
                        echo "Attempting to connect to Nexus registry at ${registry}"
                        sh "curl -m 5 -s -o /dev/null -w '%{http_code}' http://${registry} || echo 'Could not connect to Nexus'"
                        
                        echo "Attempting Docker login with Nexus"
                        sh "echo '${NEXUS_PASSWORD}' | docker login http://${registry} -u admin --password-stdin || echo 'Nexus login failed - continuing anyway'"
                        
                        echo "Tagging Docker image for Nexus"
                        sh "docker tag springbootapp:latest ${registry}/springbootapp:1.0 || echo 'Failed to tag image'"
                        
                        echo "Pushing to Nexus (may fail due to HTTPS issues)"
                        sh "docker push ${registry}/springbootapp:1.0 || echo 'Failed to push to Nexus - continuing anyway'"
                        
                        echo "Nexus deployment attempted - if it failed, we'll continue with local images"
                    } catch (Exception e) {
                        echo "Nexus deployment failed: ${e.message}"
                        echo "Continuing with pipeline using local images"
                    }
                }
            }
        }
        
        stage('Run Application') {
            steps {
                script {
                    echo "Trying to pull from Nexus (may fail)"
                    sh "docker pull ${registry}/springbootapp:1.0 || echo 'Failed to pull from Nexus - using local image'"
                    
                    echo "Stopping any existing containers"
                    sh 'docker-compose down || echo "No containers to stop"'
                    
                    echo "Starting application with docker-compose"
                    sh 'docker-compose up -d'
                    
                    echo "Application should now be running!"
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