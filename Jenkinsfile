pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    PY_VENV = ".venv"
    REPO_URL = 'https://github.com/QEA2026/P2-Group5.git'
  }

  triggers {
    // Optional: poll SCM every 5 min if you are not using webhooks
    // pollSCM('H/5 * * * *')
  }

  stages {
    stage('Checkout') {
      steps {
        script {
          def branchName = env.BRANCH_NAME ?: 'main'
          checkout([
            $class: 'GitSCM',
            branches: [[name: "*/${branchName}"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [],
            submoduleCfg: [],
            userRemoteConfigs: [[url: env.REPO_URL]]
          ])
        }
      }
    }

    stage('Python Setup') {
      steps {
        script {
          if (isUnix()) {
            sh '''
              python3 -m venv ${PY_VENV}
              . ${PY_VENV}/bin/activate
              python -m pip install --upgrade pip
              pip install -r [requirements.txt](http://_vscodecontentref_/0)
              pip install -r [requirements.txt](http://_vscodecontentref_/1)
              pip install pytest
            '''
          } else {
            bat '''
              py -m venv %PY_VENV%
              call %PY_VENV%\\Scripts\\activate
              python -m pip install --upgrade pip
              pip install -r [requirements.txt](http://_vscodecontentref_/2)
              pip install -r [requirements.txt](http://_vscodecontentref_/3)
              pip install pytest
            '''
          }
        }
      }
    }

    stage('Python Tests') {
      steps {
        script {
          if (isUnix()) {
            sh '''
              mkdir -p reports/python
              . ${PY_VENV}/bin/activate
              pytest employee_app/tests -q --junitxml=reports/python/pytest.xml
            '''
          } else {
            bat '''
              if not exist reports\\python mkdir reports\\python
              call %PY_VENV%\\Scripts\\activate
              pytest employee_app\\tests -q --junitxml=reports\\python\\pytest.xml
            '''
          }
        }
      }
    }

    stage('Java Tests') {
      steps {
        dir('manager_app') {
          script {
            if (isUnix()) {
              sh 'mvn -B -ntp clean test'
            } else {
              bat 'mvn -B -ntp clean test'
            }
          }
        }
      }
    }

    stage('Java Package') {
      steps {
        dir('manager_app') {
          script {
            if (isUnix()) {
              sh 'mvn -B -ntp -DskipTests package'
            } else {
              bat 'mvn -B -ntp -DskipTests package'
            }
          }
        }
      }
    }

    stage('Docker Build (main only)') {
      when {
        branch 'main'
      }
      steps {
        script {
          if (isUnix()) {
            sh 'docker build -f manager_app/Dockerfile -t revature-expense-manager:${BUILD_NUMBER} .'
          } else {
            bat 'docker build -f manager_app\\Dockerfile -t revature-expense-manager:%BUILD_NUMBER% .'
          }
        }
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'reports/python/pytest.xml'
      junit allowEmptyResults: true, testResults: 'manager_app/target/surefire-reports/*.xml'
      archiveArtifacts allowEmptyArchive: true, artifacts: 'manager_app/target/**/*.jar, manager_app/target/site/jacoco/**'
    }
  }
}