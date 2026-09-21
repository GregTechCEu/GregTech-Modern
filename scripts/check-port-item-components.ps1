param([string]$JavaHome = $env:JAVA_HOME, [switch]$SkipAssets)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) { throw 'Set JAVA_HOME to Java 25 or pass -JavaHome.' }
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$previousJavaHome = $env:JAVA_HOME
try {
    $env:JAVA_HOME = $JavaHome
    $extraArguments = @()
    if ($SkipAssets) {
        # These tests use stack components only. No texture/audio resources or game world are loaded.
        # Supply launch metadata pointing to an explicitly empty test directory; do not claim assets were downloaded.
        $gradleCache = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { Join-Path $env:USERPROFILE '.gradle' }
        $manifest = Join-Path $gradleCache 'caches/neoformruntime/artifacts/minecraft_26.2_version_manifest.json'
        $assetIndex = (Get-Content -LiteralPath $manifest -Raw | ConvertFrom-Json).assetIndex.id
        $testAssets = Join-Path $projectRoot 'build/port-item-components/empty-assets'
        New-Item -ItemType Directory -Path $testAssets -Force | Out-Null
        $properties = Join-Path $projectRoot 'build/port-item-components/no-assets.properties'
        @("asset_index=$assetIndex", "assets_root=$($testAssets.Replace('\', '/'))") |
            Set-Content -LiteralPath $properties -Encoding ascii
        $extraArguments = @('-x', 'downloadAssets', "-PitemComponentTestAssetProperties=$properties")
    }
    & (Join-Path $projectRoot 'gradlew.bat') -p (Join-Path $projectRoot 'ports/ModularUI') `
        -I (Join-Path $PSScriptRoot 'port-item-components.init.gradle') test -PportDiagnostics `
        --tests 'com.gregtechceu.gtceu.api.item.data.ItemStackDataPortTest' --max-workers=1 --console=plain @extraArguments
    if ($LASTEXITCODE -ne 0) { throw 'GT item component checks failed.' }
} finally {
    $env:JAVA_HOME = $previousJavaHome
}
Write-Output 'Real Minecraft component checks passed; this does not load GT or validate equipment/capability gameplay.'
