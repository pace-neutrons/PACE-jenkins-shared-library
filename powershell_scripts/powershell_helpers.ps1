function Write-And-Invoke([string]$command) {
<#
  .SYNOPSIS
    Write a command to the terminal before executing it.
  .DESCRIPTION
    Uses `Write-Ouptut` to print the given command then uses `Invoke-Expression`
    to execute it.
    The command is written to the terminal with a preceeding '+ ' to indicate
    that this function printed it, and it's not an output of the given command.
  .PARAMETER command
    Command to execute.
  .EXAMPLE
    Write-And-Invoke "Write-Output 'Hello, World!'"
      Outputs:
        + Write-Output 'Hello, World!'
        Hello, World!
#>
  Write-Output "+ $command"
  Invoke-Expression "$command"
}

function Invoke-In-Dir {
<#
  .SYNOPSIS
    Execute a command in the given directory then return to the original
    directory - printing the given command.
  .DESCRIPTION
    Changes directory before executing the command. It uses a try-finally block
    so that the original directory is returned to even if the given command
    exits the script. The command is executed using Write-And-Invoke, so it is
    written to the terminal with a preceeding '+ ' before being executed.
  .PARAMETER directory
    The directory to execute the command in.
  .PARAMETER command
    Command to execute.
  .EXAMPLE
    cd C:\Users\Public\
    Get-Location
    Invoke-In-Dir C:\Users\Public\Documents Get-Location
    Get-Location
      Outputs:
        C:\Users\Public\
        + Get-Location
        C:\Users\Public\Documents
        C:\Users\Public\
#>
  param([string]$directory, [string]$command)
  Push-Location -Path $directory
  try {
    Write-And-Invoke "$command"
  }
  finally {
    Pop-Location
  }
}

function Get-From-Registry ([string]$key) {
<#
  .SYNOPSIS
    Search the Windows registry for a specific key and return its value
  .DESCRIPTION
    Uses `Get-ItemProperty` to get the key. Uses `Write-Error` if the key can't
    be found
  .PARAMETER key
    The key to use e.g. HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB\9.8
  .EXAMPLE
    Get-From-Registry "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB\9.8"
#>
  Try {
    $reg = Get-ItemProperty "Registry::$key" -ErrorAction Stop
  } Catch [System.Management.Automation.ItemNotFoundException] {
    Write-Error ("Couldn't find $key in the Windows registry, ensure the correct software version is " +
                 "definitely installed and the correct Powershell architecture is being used. A 32-bit " +
                 "Powershell may not be able to search a 64-bit registry and vice versa`n$($_.Exception)")
  }
  return $reg
}

function Get-Conda-Env-Dir () {
<#
  .SYNOPSIS
    Gets the path of the conda env used for Windows pace-integration tests
  .DESCRIPTION
    Uses `Get-From-Registry` to get the base dir, then appends the environment
    name
  .NOTES
     Required environment variables:
       CONDA_ENV_NAME - Name of the Conda environment e.g. py36_pace_integration_2019b
  .EXAMPLE
    $CONDA_ENV_DIR = Get-Conda-Env-Dir
#>
    $conda_reg = Get-From-Registry "HKEY_LOCAL_MACHINE\SOFTWARE\Python\ContinuumAnalytics\Anaconda39-64\InstallPath"
    $conda_dir = "$(($conda_reg).'(default)')\envs\$env:CONDA_ENV_NAME"
    return $conda_dir
}
