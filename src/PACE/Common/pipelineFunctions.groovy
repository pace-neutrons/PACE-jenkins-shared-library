/* Functions relating to the determination of current branch */

package PACE.Common

def get_matlab_release(String job_name) {
  return 'R' + job_name[-5..-1]
}

def get_build_type(String job_name) {
  if (job_name.startsWith('Release-')) {
    return 'Release'
  } else if (job_name.startsWith('Branch-')) {
    return 'Branch'
  } else if(job_name.startsWith('PR-')) {
    return 'Pull-request'
  } else {
    return 'Nightly'
  }
}

def get_agent(String job_name) {
  if (job_name.contains('Scientific-Linux-7')) {
    withCredentials([string(credentialsId: 'sl7_agent', variable: 'agent')]) {
      return "${agent}"
    }
  } else if (job_name.contains('Windows-10')) {
    withCredentials([string(credentialsId: 'win10_agent', variable: 'agent')]) {
      return "${agent}"
    }
  } else {
    return ''
  }
}

def get_release_type(String job_name) {
  String build_type = get_build_type(job_name);

  switch(build_type) {
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

def get_branch_name(String job_name) {
  String build_type = get_build_type(job_name);

  switch(build_type) {
    case 'Nightly':
      return 'master'

    default:
      return ''
  }
}
