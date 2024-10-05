#!groovy

import groovy.json.JsonSlurper

def call(String git_commit) {
  // Non-PR builds will not set PR_STATUSES_URL - in which case we do not
  // want to post any statuses to Git
  if (true) { //(env.PR_STATUSES_URL) {
    // This command is triggered when _any_ PR job succeeds. We only want to run the integration
    // test if _all_ jobs pass. So use Github API to check the statuses of all workflows. This
    // returns all notifications as a JSON with fields "context" and "state". We check all unique
    // contexts have "state=success".
    if (isUnix()) {
      sh """
        echo \$PR_NUMBER
        stat_url=\$(curl -L "https://api.github.com/repos/pace-neutrons/Horace/pulls/\$PR_NUMBER" | \
            grep statuses_url | sed "/{sha}/d" | awk -F'"' '{print \$4}')
        echo \$stat_url
        st=\$(curl -L \$stat_url | \
            awk -F'"' '{if(\$2=="state") {CV=\$4} else if(\$2=="context") {ST[\$4]=ST[\$4]" "CV}} \
              END {for (key in ST) { if(ST[key] !~ /success/) {print key} }}') 
        if [ "\$st" == "" ]; then export all_success=1; else export all_success=0; fi
        echo \$all_success
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
        \$sthash = @{}
        foreach (\$stat in \$stout) {
          if(-not \$sthash.ContainsKey(\$stat.context)) { \$sthash.Add(\$stat.context, 0) }
          if (\$stat.state -eq "success") { \$sthash[\$stat.context] = 1 }
        }
        \$Env:all_success = -not (\$sthash.values -ne 1)
        Write-Output \$Env:all_success
      """
    }
    println('-------')
    println(env.all_success)
    println('-------')
    if (true) { //(env.all_success) {
      script {
        withCredentials([string(credentialsId: 'PACE_integration_webhook',
                              variable: 'api_token')]) {
          if (isUnix()) {
            sh """
              echo "Triggering integration from Linux"
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
              Write-Output "Triggering integration from Windows"
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
