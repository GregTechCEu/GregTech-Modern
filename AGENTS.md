# Repository Guidelines

## Project Structure & Module Organization

This repository is a Java 21 NeoForge Minecraft mod. Main code lives in `src/main/java/com/gregtechceu/gtceu`, with runtime assets, data, mixins, and metadata in `src/main/resources`. Generated data is checked in under `src/generated/resources`; update it through data generation rather than hand-editing when possible. Tests live in `src/test/java`, with GameTest structures in `src/test/resources/data/gtceu/structure`. Project documentation is under `docs/content`; local upstream NeoForged documentation is mirrored under `docs/neoforged` for 26.1/26.1.2 porting and development reference. Gradle logic lives under `gradle/scripts`, and injected interface metadata under `injected_interfaces`.

## Build, Test, and Development Commands

Use the Gradle wrapper. On Windows, replace `./gradlew` with `.\gradlew.bat`.

- `./gradlew assemble`: compiles and packages the mod jars into `build/libs`.
- `./gradlew spotlessCheck`: verifies Java/Kotlin formatting used by CI.
- `./gradlew spotlessApply`: formats Java/Kotlin sources according to the checked-in Spotless config.
- `./gradlew runClient`: starts the full development client.
- `./gradlew runServer`: starts a development dedicated server in `run/server`.
- `./gradlew runData`: regenerates data into `src/generated/resources`.
- `./gradlew runGameTestServer`: runs registered NeoForge GameTests, matching the PR test workflow.

## Coding Style & Naming Conventions

Spotless is the source of truth for formatting. Java uses the Eclipse formatter at `spotless/spotless.eclipseformat.xml`, import order from `spotless/spotless.importorder`, UTF-8 encoding, trailing newline enforcement, and unused import removal. Kotlin tests use ktlint with `spotless/spotless.ktlint`. Keep Java packages under `com.gregtechceu.gtceu`. Use `PascalCase` for classes, `lowerCamelCase` for methods and fields, and `UPPER_SNAKE_CASE` for constants. Prefer existing registry, recipe, machine, and data-generation patterns over new abstractions.

## Testing Guidelines

Add tests beside the feature package under `src/test/java` and suffix classes with `Test` or `Tests`. GameTest classes should use `@GameTestHolder(GTCEu.MOD_ID)` and `@GameTest(template = "...", batch = "...")` where applicable. Store required `.nbt` templates in `src/test/resources/data/gtceu/structure`; keep names descriptive and stable. Use `src/test/README.md` for GameTest authoring details. Run `./gradlew runGameTestServer` before PRs that touch code, machines, recipes, or data behavior.

## Commit & Pull Request Guidelines

Recent history favors short imperative commits such as `Fix CM test` or `Add missing ResLoc mixin (#4787)`. Keep subjects focused, mention issue or PR numbers when relevant, and avoid bundling unrelated work. PRs should follow `.github/pull_request_template.md`: explain what changed, implementation details, outcome, how it was tested, screenshots for GUI/rendering changes, and compatibility notes for API, item, block, material, machine, or recipe changes.

## Documentation Notes

For docs pages, follow `docs/CONTRIBUTING.md`: front matter with a title, one H1 per page, language-tagged code blocks, and descriptive file names using letters, numbers, dashes, or underscores.

Use `docs/neoforged` as the first stop for NeoForge API and migration reference before browsing. The current NeoForge docs snapshot lives in `docs/neoforged/docs`, older versioned docs are under `docs/neoforged/versioned_docs`, and `docs/neoforged/GTCEU-UPSTREAM.md` records the upstream commit and snapshot date.
