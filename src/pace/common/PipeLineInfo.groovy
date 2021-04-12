#!groovy

/* Info relating to the determination of current branch */
package pace.common

class PipeLineInfo {

  def job_name
  def build_type
  def matlab_release
  def release_type
  def branch_name

  def PipeLineInfo(String job_name) {
    this.job_name = job_name
    get_build_type()
    get_matlab_release()
    get_release_type()
    get_branch_name()
  }

  @NonCPS
  private void get_matlab_release() {
    this.matlab_release = 'R' + this.job_name[-5..-1]
  }

  @NonCPS
  private void get_build_type() {
    if (this.job_name.startsWith('Release-')) {
      this.build_type = 'Release'
    } else if (this.job_name.startsWith('Branch-')) {
      this.build_type = 'Branch'
    } else if(this.job_name.startsWith('PR-')) {
      this.build_type = 'Pull-request'
    } else {
      this.build_type = 'Nightly'
    }

  }

  private void get_agent() {
    if (this.job_name.contains('Scientific-Linux-7')) {
      withCredentials([string(credentialsId: 'sl7_agent', variable: 'agent')]) {
        return "${agent}"
      }
    } else if (this.job_name.contains('Windows-10')) {
      withCredentials([string(credentialsId: 'win10_agent', variable: 'agent')]) {
        return "${agent}"
      }
    } else {
      this.agent = ''
    }
  }

  @NonCPS
  private void get_release_type() {
    switch(this.build_type) {
      case 'Release':
        this.release_type = 'release'

      case 'Pull-request':
        this.release_type = 'pull_request'

      case 'Nightly':
        this.release_type = 'nightly'

      default:
        this.release_type = ''
    }
  }

  @NonCPS
  private void get_branch_name() {
    switch(this.build_type) {
      case 'Nightly':
        this.branch_name = 'master'

      default:
        this.branch_name = ''
    }
  }
}
