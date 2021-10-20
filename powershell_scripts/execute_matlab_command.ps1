. $PSScriptRoot/powershell_helpers.ps1 <# Imports:
  Write-And-Invoke, Get-From-Registry, Get-Conda-Env-Dir
#>

<#
  .SYNOPSIS
    Optionally sets the PYTHON_EX_PATH environment variable for calling Python from Matlab
    (if the CONDA_ENV_DIR env var has been set), then executes a Matlab command
  .DESCRIPTION
    Optionally, for a specific Conda environment (specified in the CONDA_ENV_DIR environment variable),
    determines the Python executable location and uses it to set the PYTHON_EX_PATH environment variable,
    this allows Python to be activated from Matlab if required. It then uses the MATLAB_VERSION environment
    variable to find the Matlab executable path and runs the command specified in the first argument to
    this script.
  .PARAMETER command
    The MATLAB command to execute
  .EXAMPLE
    execute_matlab_command.ps1 "setup_and_run_tests"
  .NOTES
    Required environment variables:
      MATLAB_VERSION - Matlab version to use e.g. 2019b
    Optional environment variables:
      CONDA_ENV_DIR  - Name of the conda environment containing the Python executable e.g. py36_pace_integration_2019b
#>

$ErrorActionPreference = 'Stop'

$MATLAB_VERSION_MAP = @{
  '2018a' = '9.4';
  '2018b' = '9.5';
  '2019a' = '9.6';
  '2019b' = '9.7';
  '2020a' = '9.8';
  '2020b' = '9.9';
}

$matlab_command = $args[0]

# Get path to Conda environment Python, and set as environment variable to
# be accessed by the Matlab command - if CONDA_ENV_DIR has been set
Try {
  $CONDA_ENV_DIR = Get-Conda-Env-Dir
  Write-Output "$CONDA_ENV_DIR"
  $PYTHON_EX_PATH = "$CONDA_ENV_DIR\python"
  Write-And-Invoke "Set-Item -Path Env:PYTHON_EX_PATH -Value $PYTHON_EX_PATH"
} Catch [System.ArgumentNullException] {
  Write-Warning ("Couldn't get conda environment dir, maybe CONDA_ENV_DIR wasn't set?")
}

# Get Matlab root directory from registry, and path to MATLAB exe
$MATLAB_REG = Get-From-Registry "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB\$($MATLAB_VERSION_MAP[$Env:MATLAB_VERSION])"
$MATLAB_ROOT = ($MATLAB_REG).MATLABROOT

. $MATLAB_ROOT\bin\matlab.exe -nosplash -nodesktop -wait -batch $matlab_command
