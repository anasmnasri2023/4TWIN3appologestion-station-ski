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
                script {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    sh 'mvn test -Dmaven.test.failure.ignore=true'
                }
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
                    def scannerHome = tool 'sonar'
                    withSonarQubeEnv('sonar') {
                        sh """
                            ${scannerHome}/bin/sonar-scanner \\
                            -Dsonar.projectKey=instructor-devops \\
                            -Dsonar.sources=. \\
                            -Dsonar.java.binaries=target/classes \\
                            -Dsonar.host.url=${SONAR_HOST_URL} \\
                            -Dsonar.login=${SONAR_AUTH_TOKEN} \\
                            -Dsonar.java.libraries=target/dependency/*.jar
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

        stage('Building Docker images') {
            steps {
                script {
                    sh 'docker-compose build'
                    sh "docker tag springbootapp:latest ${registry}/springbootapp:1.0"
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: registryCredentials, passwordVariable: 'NEXUS_PASSWORD', usernameVariable: 'NEXUS_USERNAME')]) {
                        sh "echo ${NEXUS_PASSWORD} | docker login ${registry} -u ${NEXUS_USERNAME} --password-stdin"
                        sh "docker push ${registry}/springbootapp:1.0"
                    }
                }
            }
        }

        stage('Run Application') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: registryCredentials, passwordVariable: 'NEXUS_PASSWORD', usernameVariable: 'NEXUS_USERNAME')]) {
                        sh "echo ${NEXUS_PASSWORD} | docker login ${registry} -u ${NEXUS_USERNAME} --password-stdin"
                        sh "docker pull ${registry}/springbootapp:1.0"
                        sh 'docker-compose down || true'
                        sh 'docker-compose up -d'
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                echo 'Pipeline completed'
                cleanWs()
            }
        }
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
            emailext (
                subject: "🚨 Pipeline échoué : ${currentBuild.fullDisplayName}",
                body: "Bonjour Anes,\n\nLe pipeline a échoué.\nDétails ici : ${env.BUILD_URL}\n\nMerci de vérifier.\n\n— Jenkins",
                to: "anes.mnasri@esprit.tn"
            )
        }
    }
}
