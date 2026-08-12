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
              sh '''
                set -e
                mkdir -p ../reports/java

                if ! pgrep -f "employee_app/app.py" >/dev/null; then
                  . ../${PY_VENV}/bin/activate
                  nohup python ../employee_app/app.py > ../reports/python/employee_app.log 2>&1 &
                fi

                for i in $(seq 1 30); do
                  if curl -fsS ${EMPLOYEE_BASE_URL}/login >/dev/null; then
                    break
                  fi
                  sleep 1
                done
                curl -fsS ${EMPLOYEE_BASE_URL}/login >/dev/null || {
                  echo "Employee app failed to start on ${EMPLOYEE_BASE_URL}" >&2
                  exit 1
                }

                mvn -B -ntp -DskipTests package

                if ! pgrep -f "com.revature.Main" >/dev/null; then
                  nohup java -cp target/classes:target/dependency/* com.revature.Main > ../reports/java/manager_app.log 2>&1 &
                fi

                for i in $(seq 1 30); do
                  if curl -fsS ${MANAGER_BASE_URL}/login >/dev/null; then
                    break
                  fi
                  sleep 1
                done
                curl -fsS ${MANAGER_BASE_URL}/login >/dev/null || {
                  echo "Manager app failed to start on ${MANAGER_BASE_URL}" >&2
                  exit 1
                }

                mvn -B -ntp -Demployee.baseUrl=${EMPLOYEE_BASE_URL} -Dmanager.baseUrl=${MANAGER_BASE_URL} clean test
              '''
            } else {
              bat '''
                if not exist ..\\reports\\java mkdir ..\\reports\\java

                powershell -Command "$p = Get-CimInstance Win32_Process -Filter \"Name = 'python.exe'\"; if (-not ($p | Where-Object { $_.CommandLine -match 'employee_app\\app.py' })) { Start-Process python -ArgumentList 'employee_app\\app.py' -WorkingDirectory '..' -RedirectStandardOutput '..\\reports\\python\\employee_app.log' -RedirectStandardError '..\\reports\\python\\employee_app.log' }"

                powershell -Command "$count=0; while($count -lt 30){ try { (Invoke-WebRequest -Uri http://127.0.0.1:5000/login -UseBasicParsing).StatusCode | Out-Null; break } catch { Start-Sleep -Seconds 1; $count++ } }; if($count -eq 30){ throw 'Employee app failed to start on http://127.0.0.1:5000' }"

                mvn -B -ntp -DskipTests package

                powershell -Command "$p = Get-CimInstance Win32_Process -Filter \"Name = 'java.exe'\"; if (-not ($p | Where-Object { $_.CommandLine -match 'com.revature.Main' })) { Start-Process java -ArgumentList '-cp', 'target/classes;target/dependency/*', 'com.revature.Main' -WorkingDirectory '.' -RedirectStandardOutput '..\\reports\\java\\manager_app.log' -RedirectStandardError '..\\reports\\java\\manager_app.log' }"

                powershell -Command "$count=0; while($count -lt 30){ try { (Invoke-WebRequest -Uri http://127.0.0.1:8080/login -UseBasicParsing).StatusCode | Out-Null; break } catch { Start-Sleep -Seconds 1; $count++ } }; if($count -eq 30){ throw 'Manager app failed to start on http://127.0.0.1:8080' }"

                mvn -B -ntp -Demployee.baseUrl=http://127.0.0.1:5000 -Dmanager.baseUrl=http://127.0.0.1:8080 clean test
              '''
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
