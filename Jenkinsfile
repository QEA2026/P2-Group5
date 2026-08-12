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
              python -m pip install -r employee_app/requirements.txt
              python -m pip install -r employee_app/Behave_Tests/requirements.txt
              python -m pip install pytest
            '''
          } else {
            bat '''
              py -m venv %PY_VENV%
              call %PY_VENV%\\Scripts\\activate
              python -m pip install --upgrade pip
              python -m pip install -r employee_app\\requirements.txt
              python -m pip install -r employee_app\\Behave_Tests\\requirements.txt
              python -m pip install pytest
            '''
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
