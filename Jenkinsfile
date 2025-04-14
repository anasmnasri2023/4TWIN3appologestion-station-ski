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

        stage('Building Docker images (springboot and mysql)') {
            steps {
                script {
                    try {
                        sh 'docker-compose build'
                    } catch (Exception e) {
                        echo "Docker build failed: ${e.message}"
                        // Check docker-compose file
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
                        sh "echo '${NEXUS_PASSWORD}' | docker login ${registry} -u admin --password-stdin"
                        sh "docker tag springbootapp:latest ${registry}/springbootapp:1.0 || echo 'Failed to tag image'"
                        sh "docker push ${registry}/springbootapp:1.0"
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