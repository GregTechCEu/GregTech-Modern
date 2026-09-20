param([string]$JavaHome = $env:JAVA_HOME, [string]$GradleUserHome = $env:GRADLE_USER_HOME)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) { throw 'Set JAVA_HOME to Java 25 or pass -JavaHome.' }
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (-not $GradleUserHome) { $GradleUserHome = Join-Path $env:USERPROFILE '.gradle' }
$libraryRoot = Join-Path $projectRoot 'ports/ModularUI'
$manifest = Join-Path $libraryRoot 'build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties'
$minecraftJar = Join-Path $libraryRoot 'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar'
$outputDirectory = Join-Path $projectRoot 'build/port-nbt-check'
if (-not (Test-Path -LiteralPath $manifest) -or -not (Test-Path -LiteralPath $minecraftJar)) {
    throw 'Run the ModularUI createMinecraftArtifacts Gradle task first.'
}
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
$predicateClasspath = @(Get-Content -LiteralPath $manifest | Where-Object {
    $_ -match '=' -and $_ -notmatch '-sources.jar$'
} | ForEach-Object {
    (($_ -split '=', 2)[1]).Replace('\:', ':').Replace('\\', '\')
})
$predicateClasspath += $minecraftJar
$predicateClasspath += $outputDirectory
foreach ($artifact in @('org.junit.jupiter/junit-jupiter-api/5.9.2',
        'org.opentest4j/opentest4j', 'org.junit.platform/junit-platform-commons', 'org.apiguardian/apiguardian-api')) {
    $artifactPath = Join-Path $GradleUserHome ('caches/modules-2/files-2.1/' + $artifact)
    $jar = Get-ChildItem -LiteralPath $artifactPath -Recurse -Filter '*.jar' |
        Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1 -ExpandProperty FullName
    if (-not $jar) { throw "Missing cached test dependency: $artifact" }
    $predicateClasspath += $jar
}
$sourceDirectory = Join-Path $projectRoot 'src/main/java/com/gregtechceu/gtceu/api/recipe/ingredient/nbtpredicate'
$sourceFiles = @('NBTPredicate', 'NBTPredicateUtils', 'ComparisonNBTPredicate', 'EqualsNBTPredicate') |
    ForEach-Object { Join-Path $sourceDirectory "${_}.java" }
$sourceFiles += Join-Path $projectRoot 'src/test/java/com/gregtechceu/gtceu/api/recipe/ingredient/nbtpredicate/PortNBTPredicateCheck.java'
& (Join-Path $JavaHome 'bin/javac.exe') -proc:none -sourcepath $outputDirectory `
    -cp ($predicateClasspath -join ';') -d $outputDirectory @sourceFiles
if ($LASTEXITCODE -ne 0) { throw 'GT NBT predicate compilation failed.' }
& (Join-Path $JavaHome 'bin/java.exe') -cp ($predicateClasspath -join ';') `
    com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.PortNBTPredicateCheck
if ($LASTEXITCODE -ne 0) { throw 'GT NBT predicate checks failed.' }
Write-Output 'These isolated checks do not load GT or verify component-backed ingredients and recipe registration.'
