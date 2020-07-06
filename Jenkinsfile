pipeline {
    agent any
    tools {
        maven 'maven'
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent test package'
                sh 'mvn org.jacoco:jacoco-maven-plugin:report'
            }
        }
        stage('Sonar Analysis') {
            steps {
                 withSonarQubeEnv('sonar') {
                    sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar'
                 }
            }
        }
        stage('Docker') {
            steps {
                configFileProvider([configFile(fileId: 'global-maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "mvn -s $MAVEN_SETTINGS -Dbuild.tag=${env.BUILD_TAG} docker:build"
                }
             }
        }
        stage('Deploy') {
            environment {
                DOCKERCFG = credentials('portr-dockercfg')
            }
            steps {
            	script {
            		try {
			            sh "helm upgrade gcp-bq-reporting-${env.BRANCH_NAME} --namespace=${env.BRANCH_NAME} --set tag=${env.BUILD_TAG} --set secret.portr.auth=$DOCKERCFG src/main/helm"
            		} catch (e) {
            		    try{
            		        sh "helm delete gcp-bq-reporting-${env.BRANCH_NAME} --namespace=${env.BRANCH_NAME}"
            		        sh "helm install gcp-bq-reporting-${env.BRANCH_NAME} --namespace=${env.BRANCH_NAME} --set tag=${env.BUILD_TAG} --set secret.portr.auth=$DOCKERCFG src/main/helm"
            		    } catch (e1){
            		        sh "helm delete gcp-bq-reporting-${env.BRANCH_NAME} --namespace=${env.BRANCH_NAME}"
            		    }
                    }
            	}
            }
       }
    }
}