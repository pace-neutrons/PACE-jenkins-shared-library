/* Common PACE utilities */

package PACE.Common

String get_param(String param_name, String default_val) {
  // Return environment variable if present and non-empty
  // Else return default
  String value = "";
  try {
    value = env."${param_name}";
  } catch (groovy.lang.MissingPropertyException _) { }
  if (!value) {
    value = default_val;
  }
  println "${param_name} = ${value}"
  return value
}

def post_github_status(String state, String message) {
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

def write_git_revision_to_file(String file_name) {
  script {
    def git_rev_cmd = "git rev-parse HEAD"
    echo "Writing Git revision to ${file_name}..."
    if (isUnix()) {
      sh """
        echo "\$(${git_rev_cmd})" > ${file_name}
      """
    } else {
      powershell """
        Write-Output "\$(${git_rev_cmd})" > ${file_name}
      """
    }
  }
}
