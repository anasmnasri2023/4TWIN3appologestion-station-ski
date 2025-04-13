pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "192.168.33.10:8083"
        IMAGE_NAME = "stationski"
        TAG = "1.0"
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Run Tests') {
            steps {
                // Use actual MySQL configuration for testing
                sh 'mvn test -Dspring.datasource.url=jdbc:mysql://localhost:3306/stationSki -Dspring.datasource.username=root -Dspring.datasource.password=root'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def scannerHome = tool 'scanner'
                    withSonarQubeEnv {
                        sh "${scannerHome}/bin/sonar-scanner"
                    }
                }
            }
        }

        stage('Build Application') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build and Deploy') {
            steps {
                script {
                    // Build Docker images
                    sh 'docker-compose build'

                    // Start MySQL service and wait for it to be ready
                    sh 'docker-compose up -d stationski-db'
                    sh '''
                        while ! docker exec stationski-db mysqladmin ping -uroot -proot --silent; do
                            echo "Waiting for MySQL to be ready..."
                            sleep 5
                        done
                    '''

                    // Push Docker image to Nexus registry
                    docker.withRegistry("http://${DOCKER_REGISTRY}", "nexus") {
                        sh "docker tag ${IMAGE_NAME}:${TAG} ${DOCKER_REGISTRY}/${IMAGE_NAME}:${TAG}"
                        sh "docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${TAG}"
                    }

                    // Start the full application (all services)
                    sh 'docker-compose up -d'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution completed'
        }
        success {
            echo 'Pipeline executed successfully'
        }
        failure {
            echo 'Pipeline execution failed'
        }
    }
}
