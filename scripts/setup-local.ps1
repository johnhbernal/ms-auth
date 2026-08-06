# Setup local toolchain for ms-auth (JDK 17 + Maven via Scoop).
# Run once:  powershell -File scripts/setup-local.ps1

$ErrorActionPreference = 'Stop'

if (-not (Get-Command scoop -ErrorAction SilentlyContinue)) {
    Write-Error 'Scoop not found. Install from https://scoop.sh then re-run.'
}

scoop bucket add java 2>$null
scoop install java/temurin17-jdk main/maven

$jdk = Join-Path $env:USERPROFILE 'scoop\apps\temurin17-jdk\current'
$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;$(Join-Path $env:USERPROFILE 'scoop\apps\maven\current\bin');$env:Path"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
java -version
mvn -version

Write-Host ''
Write-Host 'Register JDK in IntelliJ (fixes false String / unused warnings):'
Write-Host '  powershell -File scripts/setup-intellij-sdk.ps1'
& "$PSScriptRoot\setup-intellij-sdk.ps1"

Write-Host ''
Write-Host 'Local CI gate (must be green before push):'
Write-Host '  .\mvnw.cmd -B verify'
Write-Host '  # or: powershell -File scripts/ci-local.ps1'
Write-Host 'Run app (dev profile):'
Write-Host '  .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"'
