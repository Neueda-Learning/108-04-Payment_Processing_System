pipeline {

    agent any

    environment {
        GIT_URL = 'https://github.com/Neueda-Learning/108-04-Payment_Processing_System.git'
        BRANCH = 'main'
    }

    stages {

        stage('Checkout Source') {
            steps {
                git branch: "${BRANCH}",
                    url: "${GIT_URL}"
            }
        }

        stage('Stop Existing Containers') {
            steps {
                sh 'docker-compose down || true'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker-compose build'
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker-compose up -d'
            }
        }

        stage('Verify') {
            steps {
                sh 'docker ps'
            }
        }
    }

    post {
        failure {
            sh 'docker-compose logs --tail=100 || true'
        }

        always {
            echo 'Pipeline completed'
        }
    }
}