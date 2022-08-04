. $PSScriptRoot/powershell_helpers.ps1 <# Imports:
  Write-And-Invoke, Get-Conda-Env-Dir
#>

Write-And-Invoke "conda remove --name $env:CONDA_ENV_NAME --all -y"

# Force remove any remaining files
$CONDA_ENV_DIR = Get-Conda-Env-Dir
try {
  Write-And-Invoke "Remove-Item -Force -Recurse -Path $CONDA_ENV_DIR -ErrorAction Stop"
} catch {
  Write-Output "Could not remove directory '$CONDA_ENV_DIR'`n$($_.Exception)"
}

Write-And-Invoke "conda create --name $env:CONDA_ENV_NAME python=$env:CONDA_PY_VERSION -y"
