#!groovy

/* Info relating to the determination of current branch */
package pace.common

class PipeLineInfo {

  String job_name
  String build_type
  String matlab_release
  String agent
  String release_type
  String branch_name

  def PipeLineInfo(String job_name) {
    this.job_name = job_name
    this.build_type = this.get_build_type()
    this.matlab_release = this.get_matlab_release()
    this.agent = this.get_agent()
    this.release_type = this.get_release_type()
    this.branch_name = this.get_branch_name()
  }

  def get_matlab_release() {
    return 'R' + this.job_name[-5..-1]
  }

  def get_build_type() {
    if (this.job_name.startsWith('Release-')) {
      return 'Release'
    } else if (this.job_name.startsWith('Branch-')) {
      return 'Branch'
    } else if(this.job_name.startsWith('PR-')) {
      return 'Pull-request'
    } else {
      return 'Nightly'
    }
  }

  def get_agent() {
    if (this.job_name.contains('Scientific-Linux-7')) {
      withCredentials([string(credentialsId: 'sl7_agent', variable: 'agent')]) {
        return "${agent}"
      }
    } else if (this.job_name.contains('Windows-10')) {
      withCredentials([string(credentialsId: 'win10_agent', variable: 'agent')]) {
        return "${agent}"
      }
    } else {
      return ''
    }
  }

  def get_release_type() {
    switch(this.build_type) {
      case 'Release':
        return 'release'

      case 'Pull-request':
        return 'pull_request'

      case 'Nightly':
        return 'nightly'

      default:
        return ''
    }
  }

  def get_branch_name() {
    switch(this.build_type) {
      case 'Nightly':
        return 'master'

      default:
        return ''
    }
  }
}
