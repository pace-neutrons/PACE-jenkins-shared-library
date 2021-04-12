#!groovy

/* Common PACE utilities */

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

def get_agent(String agentIn) {
  if (agentIn == 'sl7') {
    withCredentials([string(credentialsId: 'sl7_agent', variable: 'agent')]) {
      return "${agent}"
    }
  } else if (agentIn == 'win10') {
    withCredentials([string(credentialsId: 'win10_agent', variable: 'agent')]) {
      return "${agent}"
    }
  } else {
    return ''
  }
}

/* Parse the string of Git issue labels for a label that matches Herbert_*.
 * If a match is found, build using the Herbert branch of that name.
 *
 * This function will return a message beginning 'Error: ' if more than one
 * matching Herbert branch label is found. Throwing an error directly inside
 * this function will not fail the pipeline build.
 */
def get_herbert_ref_from_labels(String labels, String herbert_branch) {
  def match = (labels =~ "Herbert_([a-zA-Z0-9_-]+)")
  try {
    match[1]
    // Return an error here as there must be, at most, one Herbert branch label
    // on the pull request. If the above line does not error, then there must
    // be at least two.
    return("Error: Found more than one Herbert branch label on the pull request.")
  } catch (IndexOutOfBoundsException e1) {
    try {
      // There is exactly one matching label on the pull request
      return match[0][1]
    } catch (IndexOutOfBoundsException e2) {
      // We get here if there are no matching labels on the pull request
      return herbert_branch
    }
  }
}
