param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$GradleUserHome = $env:GRADLE_USER_HOME
)

$ErrorActionPreference = 'Stop'
$libraryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$manifest = Join-Path $libraryRoot 'build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties'
$minecraftJar = Join-Path $libraryRoot 'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar'
$outputDirectory = Join-Path $libraryRoot 'build/port-render-check'

if (-not $JavaHome) { throw 'Set JAVA_HOME to a Java 25 installation, or pass -JavaHome.' }
if (-not $GradleUserHome) { $GradleUserHome = Join-Path $env:USERPROFILE '.gradle' }
if (-not (Test-Path -LiteralPath $manifest) -or -not (Test-Path -LiteralPath $minecraftJar)) {
    throw 'Run the ModularUI createMinecraftArtifacts Gradle task first.'
}
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null

$renderClasspath = @(Get-Content -LiteralPath $manifest | Where-Object {
    $_ -match '=' -and $_ -notmatch '-sources.jar$'
} | ForEach-Object {
    (($_ -split '=', 2)[1]).Replace('\:', ':').Replace('\\', '\')
})
$renderClasspath += $minecraftJar
$renderClasspath += $outputDirectory
foreach ($artifact in @('org.junit.jupiter/junit-jupiter-api/5.9.2',
        'org.opentest4j/opentest4j', 'org.junit.platform/junit-platform-commons', 'org.apiguardian/apiguardian-api',
        'org.projectlombok/lombok/1.18.42')) {
    $artifactPath = Join-Path $GradleUserHome ('caches/modules-2/files-2.1/' + $artifact)
    $jar = Get-ChildItem -LiteralPath $artifactPath -Recurse -Filter '*.jar' |
        Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1 -ExpandProperty FullName
    if (-not $jar) { throw "Missing cached test dependency: $artifact" }
    $renderClasspath += $jar
}

$sourceFiles = @(
    'src/main/java/brachy/modularui/drawable/GuiTint.java',
    'src/main/java/brachy/modularui/drawable/GuiStencil.java',
    'src/main/java/brachy/modularui/drawable/GuiShapeRenderState.java',
    'src/main/java/brachy/modularui/drawable/GuiShapeBuilder.java',
    'src/main/java/brachy/modularui/drawable/GuiTextureRenderState.java',
    'src/main/java/brachy/modularui/drawable/GuiEntityPreviewState.java',
    'src/main/java/brachy/modularui/client/GuiEntityPreviewRenderer.java',
    'src/main/java/brachy/modularui/utils/MUIRenderTypes.java',
    'src/main/java/brachy/modularui/utils/SpriteHelper.java',
    'src/main/java/brachy/modularui/drawable/schema/DummyLightTexture.java',
    'src/main/java/brachy/modularui/drawable/schema/SchemaCameraTransform.java',
    'src/main/java/brachy/modularui/drawable/schema/BlockHighlight.java',
    'src/main/java/brachy/modularui/drawable/schema/SchemaGeometry.java',
    'src/main/java/brachy/modularui/drawable/schema/SchemaRenderState.java',
    'src/main/java/brachy/modularui/client/SchemaPreviewRenderer.java',
    'src/test/java/brachy/modularui/SchemaRenderingTest.java',
    'src/test/java/brachy/modularui/GuiShapeRenderStateTest.java'
) | ForEach-Object { Join-Path $libraryRoot $_ }

# An explicit source path prevents javac from silently compiling dependency source jars.
& (Join-Path $JavaHome 'bin/javac.exe') -proc:none -sourcepath $outputDirectory `
    -cp ($renderClasspath -join ';') -d $outputDirectory @sourceFiles
if ($LASTEXITCODE -ne 0) { throw "Isolated renderer compilation failed ($LASTEXITCODE)." }
& (Join-Path $JavaHome 'bin/java.exe') -cp ($renderClasspath -join ';') brachy.modularui.GuiShapeRenderStateTest
if ($LASTEXITCODE -ne 0) { throw "GUI geometry checks failed ($LASTEXITCODE)." }
& (Join-Path $JavaHome 'bin/java.exe') -cp ($renderClasspath -join ';') brachy.modularui.SchemaRenderingTest
if ($LASTEXITCODE -ne 0) { throw "Structure geometry checks failed ($LASTEXITCODE)." }
Write-Output 'Isolated renderer checks passed. This does not verify the full library build or in-game rendering.'

