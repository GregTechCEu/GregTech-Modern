# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This is GregTech CEu Modern, a Java NeoForge Minecraft mod. The active branch `gregtech-26-1-port` is an in-progress port to NeoForge 26.1.2; expect rough edges and migration TODOs.

## Toolchain

- **JDK 25** (`build.gradle:23`, `JavaLanguageVersion.of(25)`). Kotlin tests target `JVM_25`. Mixin compatibility level is `JAVA_25` (`src/main/resources/gtceu.mixins.json`).
- **Gradle 9.5.0** wrapper. Use `./gradlew` (Linux/macOS/nix-shell) or `.\gradlew.bat` (Windows). `gradle.properties` sets `-Xmx6G`; the dev tasks (`runClient`, `runData`) need this much heap to start.
- **NeoForge 26.1.2.29-beta**, Minecraft 26.1.2, ModDevGradle 2.0.141. Pinned in `gradle/forge.versions.toml`; build/lang plugin versions in `gradle/libs.versions.toml`. Use the `forge` catalog for mod deps and the `libs` catalog for plugins.
- IntelliJ users **must** install the Lombok plugin to open the project; the MC Dev plugin is recommended. `shell.nix` is provided for Nix users.

## Build, run, test

Use the wrapper. Beyond the obvious tasks, note these:

- `./gradlew runData` regenerates files into `src/generated/resources/`. That directory is checked in but treated as build output — re-run `runData` whenever a generator changes; hand edits to JSON under it get clobbered on the next run.
- `./gradlew runGameTestServer` runs registered NeoForge GameTests headlessly (matches the PR test workflow). `runGameTestClient` runs them in a dev client. Both auto-set `neoforge.enabledGameTestNamespaces=gtceu`.
- `./gradlew jacocoTestReport` auto-invokes `runGameTestServer` and writes coverage to `build/coverage/`. Excludes `client/**` and `mixins/**`.
- `./gradlew runClient2` launches a second dev client (for multiplayer testing).
- `./gradlew runRenderDocClient` only registers when the `RENDERDOC_LIB` env var points at a RenderDoc shared library; otherwise the task does not exist.
- `./gradlew spotlessCheck` / `spotlessApply` — see Spotless notes below.
- `./gradlew printVersion` is what CI uses; see `.github/workflows/auto-build.yml`.

PR workflows skip the build when only docs change (filter targets `src/**`, `*.gradle`, `gradle.properties`, `gradlew*`, `gradle/**`).

## Project layout

- Main code: `src/main/java/com/gregtechceu/gtceu/` — subpackages `api`, `client`, `common`, `core`, `data`, `integration`, `config`, `utils`. `GTCEu.java` is the `@Mod` entry. Resources/mixins under `src/main/resources/`.
- Five non-standard source sets: `main`, `client`, `extra`, `clientExtra`, `test`. Plus a `minecraftCompat` source set that supplies compile-only shims for vanilla classes renamed/moved in 26.1 (`gradle/scripts/jars.gradle`); its jars are intentionally suppressed.
- `injected_interfaces/interfaces.json` injects interfaces (e.g., `IMappedRegistryAccess`, `IGTBakedQuad`) onto vanilla classes via ModDevGradle.
- `libs/ldlib2/ldlib2-neoforge-26.1.2-gtceu.1.jar` is a **patched local LDLib2 jar** specific to this port; it is not on Maven. Some LDLib2 sources are also vendored under `src/main/java/com/lowdragmc/lowdraglib/`.
- `run/` holds per-config dirs: `run/server`, `run/data`, `run/gametest`, `run/ldlib2`. They are gitignored.
- Local docs reference: `docs/neoforged/` is a vendored snapshot of upstream NeoForge documentation. It is **gitignored** (excluded from PRs) but kept on disk; consult it first for NeoForge API and migration questions before browsing the web. `docs/neoforged/GTCEU-UPSTREAM.md` records the upstream commit and snapshot date. Project mod docs live in `docs/content/` (mkdocs).

## Spotless / formatting

Spotless is the source of truth (`gradle/scripts/spotless.gradle`). Two non-obvious points:

- `ratchetFrom 'upstream/gregtech-26-1-port'` — only files changed since that ref are formatted. This requires a git remote named `upstream` that has the `gregtech-26-1-port` branch fetched. CI checks out with `fetch-depth: 0` for the same reason. If `spotlessCheck` fails locally with a missing-ref error, add the upstream remote and `git fetch upstream`.
- Targets only `src/main/java/**` and `src/test/java/**`. The vendored `com/lowdraglib/**` and `net/minecraft/**` (compat shims) are deliberately excluded and left in their upstream formatting; running an editor formatter on them creates noise diffs and defeats the exclusion.

Use `// spotless:off` / `// spotless:on` to bracket regions Spotless must leave alone (used heavily for tabular data, e.g., `GTRegistries.java`). Eclipse formatter version is `4.36`. Import order groups: `com.gregtechceu` → `com.lowdragmc` + `net` → others → `java` → `javax` → static. Kotlin tests use ktlint. `.git-blame-ignore-revs` lists formatting commits — configure git to use it.

## Coding patterns

- **Registrate** is the registration backbone. The single instance is `GTRegistration.REGISTRATE`. Custom builders live under `api/registry/registrate/` (`GTBlockBuilder`, `GTItemBuilder`, `MachineBuilder`, `MultiblockMachineBuilder`, `GTRegistrate`). Prefer extending these over rolling new abstractions.
- New content typically lives in one of the static `GT*` "data classes" under `common/data/` (`GTBlocks`, `GTMachines`, `GTItems`, `GTMaterialBlocks`, `GTConfiguredFeatures`, …) rather than a new top-level class.
- Datapack registry keys (`gtceu:ore_vein`, `gtceu:bedrock_fluid`, `gtceu:bedrock_ore`) live in `api/registry/GTRegistries.java`.
- Two datagen entry points, both required: `data/GregTechDatagen.java` (registrate-driven, `initPre`/`initPost`) and `data/DataGenerators.java` (`GatherDataEvent.Client`, datapack registries + sounds + biome/damage tags). Generated JSON uses 4-space indent (`DataProvider.INDENT_WIDTH = 4` in `GregTechDatagen.java:26`); preserve this when adding new datagen code so output matches existing files.
- Lombok 1.18.46, JetBrains nullability annotations, and several `@ApiStatus.*` are configured copyable in `lombok.config`.
- Conventions: packages stay under `com.gregtechceu.gtceu`; PascalCase for classes, lowerCamelCase for members, UPPER_SNAKE_CASE for constants.

## Mixins

- Single config `src/main/resources/gtceu.mixins.json` with package `com.gregtechceu.gtceu.core.mixins` and plugin `core/mixins/GTMixinPlugin.java`. The plugin gates per-mod compat subpackages (`emi`, `jei`, `top`, `ftbchunks`, `xaerominimap`, `xaeroworldmap`, `kubejs`, `rei`, `ldlib`→`ldlib2`) and a `dev/` subpackage that runs only outside production (`!FMLEnvironment.isProduction()`).
- New mixins must be added to the JSON's `client` or `mixins` array **and** placed in the matching subpackage. Mod-compat mixins go into `core/mixins/<modkey>/` to be auto-gated by `GTMixinPlugin`.
- Two recipe-manager mixins exist: `RecipeManagerEarlyMixin` (early-phase injection) and `RecipeManagerLateMixin` (late-phase). Pick whichever phase your hook needs and add it to that class rather than introducing a third recipe-manager mixin file.
- Mixin annotation processor uses `-Aquiet=true`. The jar manifest pins `MixinConfigs: gtceu.mixins.json` (`gradle/scripts/jars.gradle`).

## Testing (GameTests)

- Tests live under `src/test/java/com/gregtechceu/gtceu/gametest/` (and `util/TestUtils.java`). Suffix classes with `Test`/`Tests`. Use `@GameTestHolder(GTCEu.MOD_ID)` plus `@GameTest(template = "...", batch = "...")` and optional `@PrefixGameTestTemplate(false)`.
- Templates go in `src/test/resources/data/gtceu/structure/` (**singular** — note: `src/test/README.md` says `structures/` but the live path is `structure/`).
- Run with `./gradlew runGameTestServer` before any PR that touches code, machines, recipes, or data behavior. CI applies `Tests: Passed`/`Tests: Failed` PR labels.

## Commits & PRs

- On `gregtech-26-1-port`, use **Conventional Commits** — e.g., `chore: port to NeoForge 26.1.2`, `fix(ldlib): add bundled compatibility shims`, `docs: exclude vendored NeoForge docs`. Older upstream history used short imperative subjects; keep new commits on this branch in the conventional style and avoid mixing both styles within a single PR.
- PRs follow `.github/pull_request_template.md`: sections `What`, `Implementation Details`, `Outcome` (use `Closes`/`Fixes`/`Resolves #N`), `How Was This Tested`, `Additional Information` (screenshots for GUI/rendering), `Potential Compatibility Issues`. The compatibility section is **mandatory** for API/item/block/material/machine/recipe changes; remove unused sections and the placeholder italics.
- Re-run `./gradlew runData` and commit its output rather than hand-editing files under `src/generated/resources/` — hand edits there get clobbered the next time someone runs the task. The bulk of the staged blockstate JSON changes on this branch came from datagen, not by hand.
- `AGENTS.md` was removed in this port — its previous content was folded into this CLAUDE.md, so treat it as gone.

## Port-specific gotchas (gregtech-26-1-port)

- Several integrations (JEI, Jade, Create/Ponder/Flywheel, EMI, KubeJS/Rhino, CC: Tweaked) are pinned to **1.21.1 builds** as `compileOnly` with `TODO:` migration markers in `gradle/forge.versions.toml` and `dependencies.gradle`; their `@JeiPlugin`/`@WailaPlugin` entry points are gated off. Treat any `TODO:`-marked dep as load-bearing in its current `compileOnly` form — promoting it to `localRuntime` before migrating the integration code will fail at runtime.
- Working 26.1.2 runtime deps: AE2, spark, ModernFix, Sodium, Iris, ldlib2 (the patched local jar). KubeJS plugins file is `src/main/resources/kubejs.plugins.txt`.
- `parchment` mappings were removed in this port.
- Dev runs add `-ea:com.gregtechceu.gtceu...` (assertions on) and set `forge.logging.markers=REGISTRIES` at INFO level. Behavior may differ in production where assertions are off.
- `runData` does not propagate `localRuntime`/`extraLocalRuntime`, which is why `ldlib2` is wired as `runtimeOnly` in `dependencies.gradle`. If a new runtime-only dep is needed during datagen, it must also be `runtimeOnly` (or test/verify with the data run specifically).
