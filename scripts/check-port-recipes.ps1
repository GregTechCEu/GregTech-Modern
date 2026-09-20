param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) { throw 'Set JAVA_HOME to Java 25 or pass -JavaHome.' }
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$gradle = Join-Path $projectRoot 'gradlew.bat'
$libraryRoot = Join-Path $projectRoot 'ports/ModularUI'
$previousJavaHome = $env:JAVA_HOME
try {
    $env:JAVA_HOME = $JavaHome
    try {
        & $gradle -p $libraryRoot -I (Join-Path $PSScriptRoot 'port-recipes.init.gradle') test -PportDiagnostics `
            --tests 'com.gregtechceu.gtceu.api.recipe.*PortTest' --max-workers=1 --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Recipe port checks failed.' }
    } finally {
        # Remove the test-only mixin declaration from generated ModularUI metadata.
        # No checked-in ModularUI metadata is modified by the test harness.
        & $gradle -p $libraryRoot processResources --max-workers=1 --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Could not restore normal ModularUI generated metadata.' }
    }
} finally {
    $env:JAVA_HOME = $previousJavaHome
}
Write-Output 'Recipe data and mixin checks passed; this does not load the full GT mod.'
