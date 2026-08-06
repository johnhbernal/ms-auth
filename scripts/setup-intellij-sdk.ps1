# Register Scoop Temurin 17 in IntelliJ IDEA (fixes "Project JDK is not defined"
# and false "Cannot resolve symbol 'String'" / unused Spring controller warnings).
#
# Usage (IDE closed or after File -> Invalidate Caches if still stale):
#   powershell -File scripts/setup-intellij-sdk.ps1
#
# Then: File -> Project Structure -> Project -> SDK = temurin-17 -> Apply
# Or click the yellow banner "Setup SDK" and pick temurin-17.

$ErrorActionPreference = 'Stop'

$jdkHome = Join-Path $env:USERPROFILE 'scoop\apps\temurin17-jdk\current'
if (-not (Test-Path (Join-Path $jdkHome 'bin\java.exe'))) {
    Write-Error "Temurin 17 not found at $jdkHome. Run: powershell -File scripts/setup-local.ps1"
}

$jdkHomeUnix = ($jdkHome -replace '\\', '/')
$sdkName = 'temurin-17'
$versionString = '17.0.20'

$jdkTable = @"
<application>
  <component name="ProjectJdkTable">
    <jdk version="2">
      <name value="$sdkName" />
      <type value="JavaSDK" />
      <version value="java version &quot;$versionString&quot;" />
      <homePath value="$jdkHomeUnix" />
      <roots>
        <annotationsPath>
          <root type="composite">
            <root url="jar://`$APPLICATION_HOME_DIR`$/plugins/java/lib/resources/jdkAnnotations.jar!/" type="simple" />
          </root>
        </annotationsPath>
        <classPath>
          <root type="composite">
            <root url="jrt://$jdkHomeUnix!/java.base" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.compiler" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.datatransfer" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.desktop" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.instrument" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.logging" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.management" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.management.rmi" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.naming" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.net.http" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.prefs" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.rmi" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.scripting" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.se" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.security.jgss" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.security.sasl" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.smartcardio" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.sql" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.sql.rowset" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.transaction.xa" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.xml" type="simple" />
            <root url="jrt://$jdkHomeUnix!/java.xml.crypto" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.accessibility" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.attach" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.compiler" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.crypto.cryptoki" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.crypto.ec" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.crypto.mscapi" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.dynalink" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.httpserver" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.internal.ed" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.internal.jvmstat" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.internal.le" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.internal.opt" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jartool" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.javadoc" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jcmd" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jconsole" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jdeps" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jdi" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jdwp.agent" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jfr" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jlink" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jpackage" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jshell" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.jsobject" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.localedata" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.management" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.management.agent" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.management.jfr" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.naming.dns" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.naming.rmi" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.net" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.nio.mapmode" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.random" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.sctp" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.security.auth" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.security.jgss" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.unsupported" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.unsupported.desktop" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.xml.dom" type="simple" />
            <root url="jrt://$jdkHomeUnix!/jdk.zipfs" type="simple" />
          </root>
        </classPath>
        <javadocPath>
          <root type="composite" />
        </javadocPath>
        <sourcePath>
          <root type="composite">
            <root url="jar://$jdkHomeUnix/lib/src.zip!/" type="simple" />
          </root>
        </sourcePath>
      </roots>
      <additional />
    </jdk>
  </component>
</application>
"@

$roaming = Join-Path $env:APPDATA 'JetBrains'
$written = @()
Get-ChildItem $roaming -Directory -Filter 'IntelliJIdea*' -ErrorAction SilentlyContinue | ForEach-Object {
    $options = Join-Path $_.FullName 'options'
    if (-not (Test-Path $options)) {
        New-Item -ItemType Directory -Path $options | Out-Null
    }
    $target = Join-Path $options 'jdk.table.xml'
    if (Test-Path $target) {
        $existing = Get-Content -Raw $target
        if ($existing -match [regex]::Escape($sdkName) -and $existing -match 'temurin17-jdk') {
            Write-Host "OK (already registered): $target"
            $written += $target
            return
        }
        $backup = "$target.bak-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
        Copy-Item $target $backup
        Write-Host "Backed up existing JDK table -> $backup"
    }
    Set-Content -Path $target -Value $jdkTable -Encoding UTF8
    Write-Host "Wrote: $target"
    $written += $target
}

$projectMisc = Join-Path (Split-Path -Parent $PSScriptRoot) '.idea\misc.xml'
if (Test-Path $projectMisc) {
    Write-Host "Project SDK name in .idea/misc.xml -> $sdkName (JDK_17)"
}

if ($written.Count -eq 0) {
    Write-Warning 'No IntelliJIdea* config dirs under AppData\Roaming\JetBrains. Open IntelliJ once, then re-run.'
} else {
    Write-Host ''
    Write-Host "Registered SDK '$sdkName' at:"
    Write-Host "  $jdkHome"
    Write-Host 'Restart IntelliJ (or File -> Invalidate Caches -> Just Restart), then:'
    Write-Host '  File -> Project Structure -> Project -> SDK = temurin-17'
}
