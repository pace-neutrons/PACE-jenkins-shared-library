#!groovy

def call(String state, String message) {
  // Non-PR builds will not set PR_STATUSES_URL - in which case we do not
  // want to post any statuses to Git
  if (env.PR_STATUSES_URL) {
    script {
      withCredentials([string(credentialsId: 'GitHub_API_Token',
                              variable: 'api_token')]) {
        if (isUnix()) {
          sh """
            curl -H "Authorization: token \${api_token}" \
              --request POST \
              --data '{"state": "${state}", \
                "description": "${message}", \
                "target_url": "$BUILD_URL", \
                "context": "$JOB_BASE_NAME"}' \
              $PR_STATUSES_URL > /dev/null
            """
        }
        else {
          powershell """
            [Net.ServicePointManager]::SecurityProtocol = "tls12, tls11, tls"
            \$payload = @{
              "state" = "${state}";
              "description" = "${message}";
              "target_url" = "$BUILD_URL";
              "context" = "$JOB_BASE_NAME"}

            Invoke-RestMethod -URI "$PR_STATUSES_URL" \
              -Headers @{Authorization = "token \$env:api_token"} \
              -Method 'POST' \
              -Body (\$payload|ConvertTo-JSON) \
              -ContentType "application/json"
          """
        }
      }
    }
  }
}
