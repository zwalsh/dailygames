pipeline {
    agent any
    environment {
        GITHUB_TOKEN = credentials('github-personal-access-token')
    }
    stages {
        stage('start') {
            steps {
                setBuildStatus('pending')
            }
        }
//         stage('test') {
//             steps {
//                 sh './gradlew clean build'
//             }
//         }
//         stage('test-release') {
//             steps {
//                 // Clear test releases
//                 sh "rm -rf ~testdailygames/releases/*"
//                 // Create the release
//                 sh "mkdir ~testdailygames/releases/$GIT_COMMIT"
//                 sh "tar -xvf build/distributions/dailygames.tar -C ~testdailygames/releases/$GIT_COMMIT"
//                 // Set it as current
//                 sh "ln -s ~testdailygames/releases/$GIT_COMMIT ~testdailygames/releases/current"
//                 // Restart the service (only has sudo permissions for this command)
//                 sh "sudo systemctl restart testdailygames"
//             }
//         }
        stage('migrate database - testdailygames') {
            when {
                expression { env.GIT_BRANCH == 'origin/ci-cd-database-migrations' } // TODO conditional on current branch
            }
            steps {
                // Copy migrations & script into ~testdailygames
                sh "rm -rf ~testdailygames/migrations/*"
                sh "cp -r db ~testdailygames/migrations"

                // Run migrations as testdailygames
                sh "sudo su - testdailygames -c 'cd ~testdailygames/migrations && ./migrate.sh'"
            }
        }


//         stage('release') {
//             when {
//                 expression { env.GIT_BRANCH == 'origin/main' }
//             }
//             steps {
//                 // Create the release
//                 sh "mkdir ~dailygames/releases/$GIT_COMMIT"
//                 sh "tar -xvf build/distributions/dailygames.tar -C ~dailygames/releases/$GIT_COMMIT"
//                 // Set it as current
//                 sh "rm ~dailygames/releases/current"
//                 sh "ln -s ~dailygames/releases/$GIT_COMMIT ~dailygames/releases/current"
//                 // Restart the service (only has sudo permissions for this command)
//                 sh "sudo systemctl restart dailygames"
//             }
//         }
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
