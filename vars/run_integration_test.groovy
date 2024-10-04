#!groovy

import groovy.json.JsonSlurper

def call(String git_commit) {
  // Non-PR builds will not set PR_STATUSES_URL - in which case we do not
  // want to post any statuses to Git
  if (env.PR_STATUSES_URL) {
    // This command is triggered when _any_ PR job succeeds. We only want to run
    // the integration test if _all_ jobs pass. So use Github API to check the statuses of
    // all workflows and if they have all succeeded then trigger the integration test.
    def jsonSlurper = new JsonSlurper()
    def pr_info_url = new URL("https://api.github.com/repos/pace-neutrons/Horace/pulls/${env.PR_NUMBER}")
    def pr_info = jsonSlurper.parseText(pr_info_url.getText())
    def pr_stat_url = new URL(pr_info.statuses_url)
    stat_txt = pr_stat_url.getText()
    // All checks completed if the number of "success" equals "pendings"
    // We assume that if we're here, that all the "pendings" have been signaled.
    def pr_statuses = jsonSlurper.parseText(stat_txt)
    def all_success = 0
    pr_statuses.eachWithIndex {
      status, index -> 
        if (status.state == "pending") { all_success += 1 }
        else if (status.state == "success") { all_success -= 1 }
    }
    if (all_success == 0) {
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
