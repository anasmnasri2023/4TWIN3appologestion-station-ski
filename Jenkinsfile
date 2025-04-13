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
                // Use H2 for tests to avoid MySQL dependency
                sh 'mvn test -Dspring.datasource.url=jdbc:h2:mem:testdb -Dspring.datasource.username=sa -Dspring.datasource.password='
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

                    // Start MySQL and wait for it to be ready
                    sh 'docker-compose up -d db'
                    sh '''
                        while ! docker exec stationski-db mysqladmin ping -uroot -proot --silent; do
                            echo "Waiting for MySQL..."
                            sleep 5
                        done
                    '''

                    // Deploy to Nexus
                    docker.withRegistry("http://${DOCKER_REGISTRY}", "nexus") {
                        sh "docker tag ${IMAGE_NAME}:${TAG} ${DOCKER_REGISTRY}/${IMAGE_NAME}:${TAG}"
                        sh "docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${TAG}"
                    }

                    // Start the full application
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