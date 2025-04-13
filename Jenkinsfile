pipeline {
    agent any

    tools {
        // Déclare l'outil SonarQube scanner installé sur Jenkins
        // (le nom "scanner" doit correspondre à ce que tu as configuré dans Jenkins > Global Tool Configuration)
        sonarScanner 'scanner'
    }

    environment {
        // Ajoute ici l'URL du serveur SonarQube si besoin, ou géré via Jenkins global config
        // SONAR_HOST_URL = 'http://192.168.33.10:9000'
    }

    stages {
        stage('Install dependencies') {
            steps {
                script {
                    sh 'npm install'
                }
            }
        }

        stage('Unit Test') {
            steps {
                script {
                    sh 'npm test'
                }
            }
        }

        stage('Build application') {
            steps {
                script {
                    sh 'npm run build-dev'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def scannerHome = tool name: 'scanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
                    withSonarQubeEnv('SonarQube') { // "SonarQube" = nom du serveur défini dans Jenkins
                        sh "${scannerHome}/bin/sonar-scanner"
                    }
                }
            }
        }
    }
}
