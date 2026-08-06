# Local CI parity for ms-auth: ./mvnw.cmd -B verify (includes JaCoCo ≥ 0.70)
# Usage: powershell -File scripts/ci-local.ps1

$ErrorActionPreference = 'Stop'

$jdk = Join-Path $env:USERPROFILE 'scoop\apps\temurin17-jdk\current'
$mvnScoop = Join-Path $env:USERPROFILE 'scoop\apps\maven\current\bin'

if (Test-Path (Join-Path $jdk 'bin\java.exe')) {
    $env:JAVA_HOME = $jdk
    $env:Path = "$jdk\bin;$mvnScoop;" + $env:Path
}

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$mvnw = Join-Path $root 'mvnw.cmd'
if (-not (Test-Path $mvnw)) {
    Write-Error 'mvnw.cmd not found. Run: mvn -N wrapper:wrapper "-Dmaven=3.9.16"'
}

Write-Host "JAVA_HOME=$env:JAVA_HOME"
& $mvnw -B verify
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
