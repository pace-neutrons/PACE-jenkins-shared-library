#!groovy

def call(String git_commit) {
  // Non-PR builds will not set PR_STATUSES_URL - in which case we do not
  // want to post any statuses to Git
  if (env.PR_STATUSES_URL) {
    // This command is triggered when _any_ PR job succeeds. We only want to run the integration
    // test if _all_ jobs pass. So use Github API to check the statuses of all workflows. This
    // returns all notifications as a JSON with fields "context" and "state". We check all unique
    // contexts have "state=success".
    script {
      withCredentials([string(credentialsId: 'PACE_integration_webhook', variable: 'api_token')]) {
        if (isUnix()) {
          sh """
            stat_url=\$(curl -L "https://api.github.com/repos/pace-neutrons/Horace/pulls/\$PR_NUMBER" | \
                grep statuses_url | sed "/{sha}/d" | awk -F'"' '{print \$4}')
            st=\$(curl -L \$stat_url | \
                awk -F'"' '{if(\$2=="state") {CV=\$4} else if(\$2=="context") {ST[\$4]=ST[\$4]" "CV}} \
                  END {for (key in ST) { if(ST[key] !~ /success/) {print key} }}') 
            if [ "\$st" == "" ]
            then
              echo "Triggering integration from Linux"
              curl -H "Authorization: Bearer \${api_token}" \
                   -H "X-GitHub-Api-Version: 2022-11-28" \
                   --request POST \
                   --data '{"ref":"horace_integration", \
                            "inputs":{"jenkins_id":"${git_commit}", "jenkins_url":"$PR_STATUSES_URL"}}' \
                   https://api.github.com/repos/pace-neutrons/pace-integration/actions/workflows/build.yml/dispatches
            fi
          """
        } else {
          powershell """
            \$pr_info = Invoke-RestMethod -Uri "https://api.github.com/repos/pace-neutrons/Horace/pulls/\$Env:PR_NUMBER"
            \$pr_stat = Invoke-RestMethod -Uri \$pr_info.statuses_url
            \$sthash = @{}
            foreach (\$stat in \$pr_stat) {
              if(-not \$sthash.ContainsKey(\$stat.context)) { \$sthash.Add(\$stat.context, 0) }
              if (\$stat.state -eq "success") { \$sthash[\$stat.context] = 1 }
            }
            \$all_success = \$true
            foreach (\$ky in \$sthash.keys) { if (\$sthash[\$ky] -eq 0) { \$all_success = \$false } }
            Write-Output \$sthash
            Write-Output \$all_success
            if (\$all_success) {
              Write-Output "Triggering integration from Windows"
              [Net.ServicePointManager]::SecurityProtocol = "tls12, tls11, tls"
              \$payload = @{
                "ref" = "horace_integration";
                "inputs" = @{
                  "jenkins_url" = "$PR_STATUSES_URL";
                  "jenkins_id" = "${git_commit}"
                }
              }
              Invoke-RestMethod -URI "https://api.github.com/repos/pace-neutrons/pace-integration/actions/workflows/build.yml/dispatches" \
                -Headers @{Authorization = "Bearer \$env:api_token"} \
                -Method 'POST' \
                -Body (\$payload|ConvertTo-JSON) \
                -ContentType "application/json"
            }
          """
        }
      }
    }
  }
}
