pipeline {
    agent any
    triggers {
            githubPush()
    }
    environment {
        DOCKER_IMAGE = "faritych8/springtodo"
        IMAGE_TAG = "1.1"
        SPRING_PROFILE = "spring"
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
    }
    stages {
        stage('Maven install') {
            when {
                expression {
                        return env.CHANGE_ID != null && env.CHANGE_TARGET == 'dev'
                }
            }
            steps {
                echo "Maven test and install"
                sh 'chmod +x mvnw'
                sh './mvnw clean install -Dspring.profiles.active=$SPRING_PROFILE'
            }
        }
        stage('Docker build and push') {
            when {
                   expression { return env.GIT_BRANCH == 'master' }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'DOCKER_PASSWORD', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USERNAME')]) {
                     echo "Docker build and push"
                     sh 'docker build -t $DOCKER_IMAGE:$IMAGE_TAG .'
                     sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USERNAME" --password-stdin'
                     sh 'docker push $DOCKER_IMAGE:$IMAGE_TAG'
                     sh 'docker rmi $DOCKER_IMAGE:$IMAGE_TAG'
                }
            }
        }

    }
}