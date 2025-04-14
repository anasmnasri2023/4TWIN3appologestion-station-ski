pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "192.168.33.10:8083"
        IMAGE_NAME = "stationski"
        TAG = "${BUILD_NUMBER}"
        DOCKER_COMPOSE = "docker-compose -f docker-compose.yml"
        SKIP_TESTS = "false"  // Set to "true" to skip tests
    }

    stages {
        stage('Checkout & Prep') {
            steps {
                checkout scm
                sh 'mkdir -p prometheus'
                writeFile file: 'prometheus/prometheus.yml', text: readFile('prometheus/prometheus.yml')
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    if (env.SKIP_TESTS == "true") {
                        echo "Skipping tests as requested"
                        sh 'mvn clean package -DskipTests'
                    } else {
                        echo "Running tests"
                        sh 'mvn clean package'
                        junit '**/target/surefire-reports/*.xml'
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            when {
                expression { env.SKIP_TESTS == "false" }
            }
            steps {
                withSonarQubeEnv('sonar') {
                    sh 'mvn sonar:sonar'
                }
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    sh "${DOCKER_COMPOSE} build"
                    withCredentials([usernamePassword(
                        credentialsId: 'nexus',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )]) {
                        sh """
                            docker login ${DOCKER_REGISTRY} -u ${NEXUS_USER} -p ${NEXUS_PASS}
                            docker tag ${IMAGE_NAME}:latest ${DOCKER_REGISTRY}/${IMAGE_NAME}:${TAG}
                            docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${TAG}
                        """
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    // Start DB first
                    sh "${DOCKER_COMPOSE} up -d stationski-db"

                    // Wait for DB
                    sh '''
                        for i in {1..10}; do
                            if docker exec stationski-db mysqladmin ping -uroot -psarah2025 --silent; then
                                echo "MySQL ready"
                                break
                            fi
                            echo "Waiting for MySQL... ($i/10)"
                            sleep 10
                        done
                    '''

                    // Start remaining services
                    sh "${DOCKER_COMPOSE} up -d --no-deps stationski-app prometheus grafana sonarqube sonarqube-db"
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
            slackSend channel: '#devops',
                     color: 'good',
                     message: "SUCCESS: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}"
        }
        failure {
            slackSend channel: '#devops',
                     color: 'danger',
                     message: "FAILED: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}"
        }
    }
}