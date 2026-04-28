---
title: Testing and Datagen
---


# Testing and Datagen

This page covers the contributor workflow for generated data, formatting, builds, and GameTests.


## Gradle Tasks

Use the Gradle wrapper from the repository root.

| Task | Use it for |
| --- | --- |
| `.\gradlew.bat assemble` | Compile and package the mod jars into `build/libs`. |
| `.\gradlew.bat spotlessCheck` | Verify Java/Kotlin formatting used by CI. |
| `.\gradlew.bat spotlessApply` | Apply the configured Spotless formatters. |
| `.\gradlew.bat runClient` | Launch the full development client. |
| `.\gradlew.bat runServer` | Launch a development dedicated server in `run/server`. |
| `.\gradlew.bat runData` | Regenerate data into `src/generated/resources`. |
| `.\gradlew.bat runGameTestServer` | Run registered NeoForge GameTests and exit. |

On non-Windows systems, replace `.\gradlew.bat` with `./gradlew`.


## Data Generation

Generated resources are checked in under `src/generated/resources`. Prefer updating the generator and running
`runData` instead of editing generated JSON by hand.

!!! warning "Current LDLib2 migration caveat"

    The 2026-04-29 LDLib2 migration snapshot currently fails `.\gradlew.bat runData` during bootstrap with
    `NoClassDefFoundError: com/lowdragmc/lowdraglib2/gui/factory/BlockUIMenuType$BlockUI`. The data run did not load
    LDLib2, so treat this as a datagen classpath gap. The generated machine blockstate and item-definition changes in
    the working tree are migration output that should be regenerated once the classpath issue is fixed.

The important code paths are:

- `gradle/scripts/moddevgradle.gradle`, which defines the `data` run configuration;
- `build.gradle`, which adds generated resources to `main.resources` and fixes `MOD_CLASSES` for `runData`;
- `data/GregTechDatagen.java`, which installs GTCEu's model, blockstate, tag, language, and data map providers;
- `data/recipe`, which owns generated recipe families;
- `data/tags`, `data/lang`, `data/loot`, `data/datamap`, and `data/model`, which own their matching generated outputs.

`GregTechDatagen.initPre()` runs before content registration so model/blockstate providers are ready. `initPost()` runs
after content registration so tags, language, and data maps see the registered content.


## Dynamic Runtime Data

Not every generated asset or recipe is checked into `src/generated/resources`.

GTCEu also generates runtime packs:

- `GTDynamicResourcePack` for client assets such as dynamic model, item definition, and blockstate output;
- `GTDynamicDataPack` for server data such as generated recipes and loot;
- `RegisterDynamicResourcesEvent` for client resource regeneration during reload;
- `ReloadableServerResourcesMixin` for injecting dynamic server data during reload.

If a resource is missing in game, first decide which of these three buckets it belongs to:

1. checked-in static resource under `src/main/resources`;
2. checked-in datagen output under `src/generated/resources`;
3. runtime dynamic pack output.


## GameTests

GameTests live in `src/test/java` beside the feature package they cover. Structures live in
`src/test/resources/data/gtceu/structure`.

The 26.1.2 port uses a custom bootstrap:

- `GTCEu.registerGameTestBootstrap` loads the test bootstrap only when `TEST` is set.
- `GTGameTestBootstrap.init` listens for `RegisterGameTestsEvent`.
- `GTGameTestBootstrap` reflects GTCEu's test annotations into NeoForge `GameTestInstance`s.
- `LegacyGameTestHelper` and `LegacyExtendedGameTestHelper` keep older helper method shapes usable.

To add a GameTest:

1. Put the class under the matching package in `src/test/java`.
2. Annotate the class with `@GameTestHolder(GTCEu.MOD_ID)`.
3. Add `@PrefixGameTestTemplate(false)` if the template should be resolved directly by name.
4. Annotate static test methods with `@GameTest(template = "...", batch = "...")`.
5. Add required `.nbt` structures to `src/test/resources/data/gtceu/structure`.
6. Add the class to `GTGameTestBootstrap.TEST_CLASSES`, or guard it with `addOptionalTest` if it depends on an optional mod.
7. Run `.\gradlew.bat runGameTestServer`.

Use `TestUtils` for common GTCEu assertions, machine setup, cover placement, fluid/item comparisons, redstone checks, and
delayed success handling.


## When to Run What

| Change | Minimum checks |
| --- | --- |
| Markdown docs only | Check links and headings locally. |
| Formatting-only Java/Kotlin change | `spotlessCheck`. |
| Build logic, dependency, source-set, or metadata change | `assemble`. |
| Generated recipes, tags, models, language, loot, or data maps | `runData`, then inspect `src/generated/resources`. |
| Machines, covers, recipes, storage, pipenets, worldgen, or data behavior | `runGameTestServer`. |
| Client rendering, UI, item models, overlays, or dynamic assets | `runClient` and test resource reload behavior. |

Pull requests that change behavior should include the exact commands run and whether generated resources changed.
