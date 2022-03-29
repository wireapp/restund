pipeline {
    agent {
        node { label 'linuxslave2' }
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                // sh 'docker build . -t quay.io/wire/restund:feature-dtls_cert-latest'
                sh 'buildah bud --file "./Dockerfile" --squash --no-cache --tag restund:feature-dtls_cert-latest ./'
            }
        }
        stage('Upload') {
            steps {
                withCredentials([ file( credentialsId: 'quayio-image-push', variable: 'authJsonPath' ) ]) {
                    // sh 'docker push quay.io/wire/restund:feature-dtls_cert-latest'
                    sh 'buildah push --authfile ${authJsonPath} restund:feature-dtls_cert-latest quay.io/wire/restund:feature-dtls_cert-latest'
                }
            }
        }
    }
    
    post {
        success {
            node( 'built-in' ) {
                withCredentials([ string( credentialsId: 'wire-jenkinsbot', variable: 'jenkinsbot_secret' ) ]) {
                    wireSend secret: jenkinsbot_secret, message: "✅ restund branch: ${BRANCH_NAME} (${BUILD_ID}) succeeded\n${BUILD_URL}console"
                }
            }
        }

        failure {
            node( 'built-in' ) {
                withCredentials([ string( credentialsId: 'wire-jenkinsbot', variable: 'jenkinsbot_secret' ) ]) {
                    wireSend secret: jenkinsbot_secret, message: "❌ restund branch: ${BRANCH_NAME} (${BUILD_ID}) failed\n${BUILD_URL}console"
                }
            }
        }
    }
}
