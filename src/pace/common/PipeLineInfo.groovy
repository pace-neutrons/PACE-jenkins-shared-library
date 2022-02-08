#!groovy

/* Info relating to the determination of current branch */
package pace.common

class PipeLineInfo {

  def job_name
  def build_type
  def matlab_release
  def release_type
  def branch_name
  def herbert_branch
  def os

  def PipeLineInfo(String job_name) {
    this.job_name = job_name
    get_build_type()
    get_matlab_release()
    get_release_type()
    get_branch_name()
    get_default_herbert_branch()
    get_os()
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

  @NonCPS
  private void get_os() {
    if (this.job_name.contains('Scientific-Linux-7')) {
      this.os = 'sl7'
    } else if (this.job_name.contains('Windows-10')) {
      this.os = 'win10'
    } else if (this.job_name.contains('VM-Win-10')) {
      this.os = 'pacewin'
    } else {
      this.os = ''
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

  @NonCPS
  private void get_default_herbert_branch() {
    switch(this.build_type) {
      case 'Release':
	this.herbert_branch = ''

      case 'Nightly':
	this.herbert_branch = 'None'

      default:
	this.herbert_branch = 'master'
    }
  }

}
