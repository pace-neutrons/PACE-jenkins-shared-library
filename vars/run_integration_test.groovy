#!groovy

import groovy.json.JsonSlurper

def call(String git_commit) {
  // Non-PR builds will not set PR_STATUSES_URL - in which case we do not
  // want to post any statuses to Git
  if (true) { //(env.PR_STATUSES_URL) {
    // This command is triggered when _any_ PR job succeeds. We only want to run the integration
    // test if _all_ jobs pass. So use Github API to check the statuses of all workflows. This
    // returns all notifications, so we assume that if we're here, that all the "pendings" were 
    // signaled, so if we see the number of "success" equals "pendings" we know all checks passed
    if (isUnix()) {
      sh """
        echo \$PR_NUMBER
        stat_url=\$(curl -L "https://api.github.com/repos/pace-neutrons/Horace/pulls/\$PR_NUMBER" | \
            grep statuses_url | sed "/{sha}/d" | awk -F'"' '{print \$4}')
        echo \$stat_url
        export scount=\$(curl -L \$stat_url | grep state | \
            awk -F'"' 'BEGIN {ct=0} {if(\$4=="pending") {ct++} else {if(\$4=="success") ct--}} END {print ct}')
      """
    } else {
      powershell """
        gci env:*
        Write-Output "PR_NUMBER"
        Write-Output \$Env:PR_NUMBER
        Write-Output "---------"
        \$pr_info = Invoke-RestMethod -Uri "https://api.github.com/repos/pace-neutrons/Horace/pulls/\$Env:PR_NUMBER"
        Write-Output \$pr_info
        \$pr_stat = Invoke-RestMethod -Uri \$pr_info.statuses_url
        \$Env:scount = 0
        foreach (\$stat in \$pr_stat) {
          if (\$stat.state -eq "pending") { \$Env:scount += 1 }
          elseif (\$stat.state -eq "success") { \$Env:scount -= 1 }
        }
      """
    }
    if (env.scount == 0) {
      script {
        withCredentials([string(credentialsId: 'PACE_integration_webhook',
                              variable: 'api_token')]) {
          if (isUnix()) {
            sh """
              curl -H "Authorization: Bearer \${api_token}" \
                -H "X-GitHub-Api-Version: 2022-11-28" \
                --request POST \
                --data '{"ref":"horace_integration", \
                         "inputs":{"jenkins_id":"$BUILD_URL", "jenkins_url":"$PR_STATUSES_URL", "horace_commit":${git_commit}}}' \
                https://api.github.com/repos/pace-neutrons/pace-integration/actions/workflows/build.yml/dispatches
                > /dev/null
              """
          }
          else {
            powershell """
              [Net.ServicePointManager]::SecurityProtocol = "tls12, tls11, tls"
              \$payload = @{
                "ref" = "horace_integration";
                "inputs" = @{
                  "jenkins_id":"$BUILD_URL";
                  "jenkins_url":"$PR_STATUSES_URL"
                  "horace_commit":"${git_commit}"
                }
              }
              Invoke-RestMethod -URI "https://api.github.com/repos/pace-neutrons/pace-integration/actions/workflows/build.yml/dispatches" \
                -Headers @{Authorization = "Bearer \$env:api_token"} \
                -Method 'POST' \
                -Body (\$payload|ConvertTo-JSON) \
                -ContentType "application/json"
            """
          }
        }
      }
    }
  }
}
