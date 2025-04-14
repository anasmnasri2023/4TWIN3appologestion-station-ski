pipeline {
    agent any

    environment {
        registryCredentials = "nexus"
        registry = "192.168.56.100:8083"
        SONAR_HOST_URL = "http://192.168.56.100:9000"
        SONAR_AUTH_TOKEN = credentials('sonar-token')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -Dmaven.test.failure.ignore=true'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh '''
                        sonar-scanner \
                        -Dsonar.projectKey=instructor-devops \
                        -Dsonar.sources=. \
                        -Dsonar.java.binaries=target/classes \
                        -Dsonar.host.url=$SONAR_HOST_URL \
                        -Dsonar.login=$SONAR_AUTH_TOKEN
                    '''
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Building Docker images') {
            steps {
                sh '''
                    docker-compose build
                    docker tag springbootapp:latest ${registry}/springbootapp:1.0
                '''
            }
        }

        stage('Deploy to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: registryCredentials, usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    sh '''
                        echo $NEXUS_PASSWORD | docker login $registry -u $NEXUS_USERNAME --password-stdin
                        docker push $registry/springbootapp:1.0
                    '''
                }
            }
        }

        stage('Run Application') {
            steps {
                withCredentials([usernamePassword(credentialsId: registryCredentials, usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    sh '''
                        echo $NEXUS_PASSWORD | docker login $registry -u $NEXUS_USERNAME --password-stdin
                        docker pull $registry/springbootapp:1.0
                        docker-compose down || true
                        docker-compose up -d
                    '''
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
            emailext(
                subject: "Pipeline failed: ${currentBuild.fullDisplayName}",
                body: "La compilation a échoué. Veuillez vérifier: ${env.BUILD_URL}",
                to: "admin@example.com"
            )
        }
    }
}
