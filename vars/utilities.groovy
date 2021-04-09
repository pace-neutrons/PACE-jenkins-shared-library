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
