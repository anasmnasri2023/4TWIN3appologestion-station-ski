
pipeline {
    agent any

    environment {
        registryCredentials = "nexus"
        registry = "192.168.33.10:8083"
    }

    stages {
        stage('Install dependencies') {
            steps {
                script {
                    sh('npm install')
                }
            }
        }

        stage('Unit Test') {
            steps {
                script {
                    sh('npm test')
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

        stage('Build application') {
            steps {
                script {
                    sh('npm run build-dev')
                }
            }
        }

        stage('Building Docker images') {
            steps {
                script {
                    sh('docker-compose build')
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                script {
                    docker.withRegistry("http://${registry}", registryCredentials) {
                        sh("docker push ${registry}/nodemongoapp:6.0")
                    }
                }
            }
        }

        stage('Run application') {
            steps {
                script {
                    docker.withRegistry("http://${registry}", registryCredentials) {
                        sh("docker pull ${registry}/nodemongoapp:6.0")
                    }
                    sh('docker-compose up -d')
                }
            }
        }
    }
}
