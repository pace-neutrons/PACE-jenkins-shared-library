. $PSScriptRoot/powershell_helpers.ps1 <# Imports:
  Write-And-Invoke, Get-From-Registry, Get-Conda-Env-Dir
#>

<#
  .SYNOPSIS
    Sets the PYTHON_EX_PATH environment variable, and executes a Matlab script to run integration tests
  .DESCRIPTION
    For a specific Conda environment (specified in the CONDA_ENV_DIR environment variable), determines
    the Python executable location and uses it to set the PYTHON_EX_PATH environment variable. It then
    uses the MATLAB_VERSION environment variable to find the Matlab executable path and runs the
    'setup_and_run_tests' script to run the integration tests. In this script the PYTHON_EX_PATH environment
    variable is used to activate Python from Matlab.
  .PARAMETER command
    The MATLAB command to execute
  .EXAMPLE
    execute_matlab_command.ps1 "setup_and_run_tests"
  .NOTES
    Required environment variables:
      CONDA_ENV_DIR  - Name of the conda environment containing the Python executable e.g. py36_pace_integration_2019b
      MATLAB_VERSION - Matlab version to use e.g. 2019b
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
# be accessed by the Matlab test script
$CONDA_ENV_DIR = Get-Conda-Env-Dir
Write-Output "$CONDA_ENV_DIR"
$PYTHON_EX_PATH = "$CONDA_ENV_DIR\python"
Write-And-Invoke "Set-Item -Path Env:PYTHON_EX_PATH -Value $PYTHON_EX_PATH"

# Get Matlab root directory from registry, and path to MATLAB exe
$MATLAB_REG = Get-From-Registry "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB\$($MATLAB_VERSION_MAP[$Env:MATLAB_VERSION])"
$MATLAB_ROOT = ($MATLAB_REG).MATLABROOT

. $MATLAB_ROOT\bin\matlab.exe -nosplash -nodesktop -wait -batch $matlab_command
