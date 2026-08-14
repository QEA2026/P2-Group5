pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    PY_VENV = ".venv"
    REPO_URL = 'https://github.com/QEA2026/P2-Group5.git'
    EMPLOYEE_BASE_URL = 'http://127.0.0.1:5000'
    MANAGER_BASE_URL = 'http://127.0.0.1:8080'
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

    stage('Python Tests') {
      steps {
        script {
          if (isUnix()) {
            sh '''
              mkdir -p reports/python
              . ${PY_VENV}/bin/activate
              PYTHONPATH=employee_app${PYTHONPATH:+:${PYTHONPATH}} pytest employee_app/tests -q --junitxml=reports/python/pytest.xml
            '''
          } else {
            bat '''
              if not exist reports\\python mkdir reports\\python
              call %PY_VENV%\\Scripts\\activate
              set "PYTHONPATH=employee_app;%PYTHONPATH%"
              pytest employee_app\\tests -q --junitxml=reports\\python\\pytest.xml
            '''
          }
        }
      }
    }

    stage('Docker Build') {
      steps {
        script {
          if (isUnix()) {
            sh '''
              docker build -f employee_app/Dockerfile -t revature-expense-employee:${BUILD_NUMBER} employee_app
              docker build -f manager_app/Dockerfile -t revature-expense-manager:${BUILD_NUMBER} .
            '''
          } else {
            bat '''
              docker build -f employee_app\\Dockerfile -t revature-expense-employee:%BUILD_NUMBER% employee_app
              docker build -f manager_app\\Dockerfile -t revature-expense-manager:%BUILD_NUMBER% .
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
              sh '''
                set -e
                mkdir -p ../reports/java
                        EMPLOYEE_PORT=$((5000 + BUILD_NUMBER))
                        MANAGER_PORT=$((8080 + BUILD_NUMBER))
                        EMPLOYEE_URL="http://127.0.0.1:${EMPLOYEE_PORT}"
                        MANAGER_URL="http://127.0.0.1:${MANAGER_PORT}"

                docker rm -f employee-app manager-app >/dev/null 2>&1 || true
                        docker run -d --name employee-app -p ${EMPLOYEE_PORT}:5000 -v "$(pwd)/../db:/db" revature-expense-employee:${BUILD_NUMBER}

                for i in $(seq 1 30); do
                          if curl -fsS ${EMPLOYEE_URL}/login >/dev/null; then
                    break
                  fi
                  sleep 1
                done
                curl -fsS ${EMPLOYEE_URL}/login >/dev/null || {
                  echo "Employee app failed to start on ${EMPLOYEE_URL}" >&2
                  exit 1
                }

                docker run -d --name manager-app -p ${MANAGER_PORT}:8080 -v "$(pwd)/../db:/app/db" revature-expense-manager:${BUILD_NUMBER}

                for i in $(seq 1 30); do
                  if curl -fsS ${MANAGER_URL}/login >/dev/null; then
                    break
                  fi
                  sleep 1
                done
                curl -fsS ${MANAGER_URL}/login >/dev/null || {
                  echo "Manager app failed to start on ${MANAGER_URL}" >&2
                  exit 1
                }

                        mvn -B -ntp -Demployee.baseUrl=${EMPLOYEE_URL} -Dmanager.baseUrl=${MANAGER_URL} clean test
              '''
            } else {
              bat '''
                if not exist ..\\reports\\java mkdir ..\\reports\\java
                        set /a EMPLOYEE_PORT=5000+%BUILD_NUMBER%
                        set /a MANAGER_PORT=8080+%BUILD_NUMBER%
                        set "EMPLOYEE_URL=http://127.0.0.1:%EMPLOYEE_PORT%"
                        set "MANAGER_URL=http://127.0.0.1:%MANAGER_PORT%"

                docker rm -f employee-app manager-app 2>$null
                        docker run -d --name employee-app -p %EMPLOYEE_PORT%:5000 -v "%CD%\\..\\db:/db" revature-expense-employee:%BUILD_NUMBER%

                        powershell -Command "$count=0; while($count -lt 30){ try { (Invoke-WebRequest -Uri %EMPLOYEE_URL%/login -UseBasicParsing).StatusCode | Out-Null; break } catch { Start-Sleep -Seconds 1; $count++ } }; if($count -eq 30){ throw 'Employee app failed to start on %EMPLOYEE_URL%' }"

                        docker run -d --name manager-app -p %MANAGER_PORT%:8080 -v "%CD%\\..\\db:/app/db" revature-expense-manager:%BUILD_NUMBER%

                        powershell -Command "$count=0; while($count -lt 30){ try { (Invoke-WebRequest -Uri %MANAGER_URL%/login -UseBasicParsing).StatusCode | Out-Null; break } catch { Start-Sleep -Seconds 1; $count++ } }; if($count -eq 30){ throw 'Manager app failed to start on %MANAGER_URL%' }"

                        mvn -B -ntp -Demployee.baseUrl=%EMPLOYEE_URL% -Dmanager.baseUrl=%MANAGER_URL% clean test
              '''
            }
          }
        }
      }
    }

  }

  post {
    always {
      script {
        if (isUnix()) {
          sh 'docker rm -f employee-app manager-app >/dev/null 2>&1 || true'
        } else {
          bat 'docker rm -f employee-app manager-app 2>nul || exit /b 0'
        }
      }
      junit allowEmptyResults: true, testResults: 'reports/python/pytest.xml'
      junit allowEmptyResults: true, testResults: 'manager_app/target/surefire-reports/*.xml'
      archiveArtifacts allowEmptyArchive: true, artifacts: 'manager_app/target/**/*.jar, manager_app/target/site/jacoco/**'
    }
  }
}