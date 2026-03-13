pipeline {
    agent any
    triggers {
            githubPush()
    }
    environment{
        DOCKER_IMAGE = "faritych8/SpringToDO"
        SPRING_PROFILE = "spring"
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
    }
    stages {
        stage('github sign in'){
            steps{
                git credentialsId: 'GitHub', url: 'git@github.com:Ravilat/SpringToDo.git'
            }
        }
        stage('Maven install'){
            steps{
                sh 'chmod +x mvnw'
                sh './mvnw clean install -Dspring.profiles.active=$SPRING_PROFILE'
            }
        }
        stage('Docker build and push') {
//             when {
//                 branch 'master'
//             }
            steps {
                withCredentials([usernamePassword(credentialsId: 'DOCKER_PASSWORD', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USERNAME')]) {
                     sh 'docker build -t $DOCKER_IMAGE:1.1 .'
                     sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USERNAME" --password-stdin'
                     sh 'docker push $DOCKER_IMAGE:1.1'
                     sh 'docker rmi $DOCKER_IMAGE:1.1'
                }
            }
        }
    }
}