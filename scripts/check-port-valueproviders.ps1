param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) { throw 'Set JAVA_HOME to Java 25 or pass -JavaHome.' }
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$previousJavaHome = $env:JAVA_HOME
try {
    $env:JAVA_HOME = $JavaHome
    & (Join-Path $projectRoot 'gradlew.bat') -p (Join-Path $projectRoot 'ports/ModularUI') `
        -I (Join-Path $PSScriptRoot 'port-valueproviders.init.gradle') test -PportDiagnostics `
        --tests com.gregtechceu.gtceu.common.valueprovider.PortValueProviderCheck --max-workers=1 --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'GT provider checks failed.' }
} finally {
    $env:JAVA_HOME = $previousJavaHome
}
Write-Output 'These isolated checks use the NeoForge loader but do not load the full GT mod or validate its registration.'
