pipeline {
    agent any
    tools {
        jdk 'JDK 17'
    }
    environment {
        GITHUB_TOKEN = credentials('github-personal-access-token')
    }
    stages {
        stage('start') {
            steps {
                setBuildStatus('pending')
            }
        }
        stage('assemble') {
            steps {
                sh './gradlew assemble testClasses'
            }
        }
        stage('lint') {
            steps {
                sh './gradlew ktlintCheck'
            }
        }
        stage('test') {
            steps {
                sh './gradlew build'
            }
        }
    }
    post {
        success {
            echo 'Success!'
            setBuildStatus('success')
        }
        unstable {
            echo 'I am unstable :/'
            setBuildStatus('failure')
        }
        failure {
            echo 'I failed :('
            setBuildStatus('failure')
        }
        always {
            junit '**/build/test-results/test/TEST-*.xml'
        }
    }
}

void setBuildStatus(state) {
    sh """
        curl "https://api.GitHub.com/repos/zwalsh/dailygames/statuses/$GIT_COMMIT" \
                -H "Content-Type: application/json" \
                -H "Authorization: token $GITHUB_TOKEN" \
                -X POST \
                -d '{\"state\": \"$state\",\"context\": \"continuous-integration/jenkins\",
                \"description\": \"Jenkins\", \"target_url\": \"https://jenkins.zachwal.sh/job/dailygames/$BUILD_NUMBER/console\"}'
    """
}
