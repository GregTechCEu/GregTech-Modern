# TODO — NeoForge 26.1.2 port

Active follow-up tracker for the `gregtech-26-1-port` branch. Update as
items are picked up, parked, or completed; do not delete completed
items — strike them through and keep them for context, since the
"resolved" rationale is often what saves a future session from
re-treading the same path.

## How to maintain

- Each item has a stable ID (`T-001` style) — reference it in commit
  messages so the trail is searchable.
- Statuses: `[ ]` open, `[~]` in progress, `[!]` blocked, `[x]` done.
- Severity: `crit` (mod broken), `high` (audit reject driver),
  `med` (maintainability), `low` (style/polish).
- When you finish an item, flip the box to `[x]`, prepend the resolving
  commit short SHA, and leave the rationale text alone. When something
  turns out to be wrong-premised, mark it `[x] (wrong)` and explain.

## Status board

| Status | Count |
|---|---:|
| `[ ]` Open | 4 |
| `[~]` In progress | 0 |
| `[!]` Blocked | 1 |
| `[x]` Done | 8 |
| `[x] (wrong)` Audit was wrong | 4 |

---

## Active

### `[ ] T-001` — `med` Verify multiblock preview camera/viewport in-game

`SceneWidget.drawInBackground` now invokes `renderer.render(...)` on
LDLib2's `WorldSceneRenderer`, and `setCameraLookAt(center, zoom,
pitch, yaw)` flips yaw/pitch to match LDLib2's orbital signature.
Data plumbing is correct; what is untested is whether the camera
position/scale match the legacy LDLib1 widget visuals.

**To verify:** open any multiblock in JEI/REI ("Multiblock Info"
category), or open the directional cover configurator. Confirm the
scene renders with the controller centred and the cursor pickable. If
scale/centring is off, tune the `setCameraLookAt` defaults in
`SceneWidget` and the per-call args in `PatternPreviewWidget` /
`FancyUIMachineUI`.

**Files:**
- `src/main/java/com/lowdragmc/lowdraglib/gui/widget/SceneWidget.java`
- `src/main/java/com/lowdragmc/lowdraglib/client/scene/WorldSceneRenderer.java`
- `src/main/java/com/gregtechceu/gtceu/api/gui/widget/PatternPreviewWidget.java`
- `src/main/java/com/gregtechceu/gtceu/api/gui/widget/directional/CombinedDirectionalConfigurator.java`

**Shipped in:** `b9f903ffe`

### `[ ] T-002` — `low` Migrate `core/compat/GuiGraphics` to `GuiGraphicsExtractor`

60-line method-name adapter (`drawString → text`, `renderItem → item`,
`renderFakeItem → fakeItem`, `renderItemDecorations → itemDecorations`)
over the canonical `net.minecraft.client.gui.GuiGraphicsExtractor`.
**53 importers.** Migration touches 100+ method-call rename sites.

**Skip unless** you are already touching all 53 importers for an
unrelated refactor — diff cost outweighs deleting one self-documenting
file. Once migrated, delete
`src/main/java/com/gregtechceu/gtceu/core/compat/GuiGraphics.java`.

### `[ ] T-003` — `low` Rename `client/model/compat/` → `client/model/legacy/`

Audit flagged the package as shim cruft, but `BakedModel`,
`BakedModelWrapper`, `ChunkRenderTypeSet`, `ItemTransform`,
`ItemTransforms` carry real legacy-model bridge logic that
`gtceu:legacy_model` / `gtceu:legacy_item_model` JSON and the
tagprefix render pipeline depend on at runtime. The tiny stubs
(`IDynamicBakedModel`, `ItemOverrides`, `ModelResourceLocation`,
`IQuadTransformer`, `QuadTransformers`) hang off the bridge classes
and should NOT be deleted standalone.

A package rename to `client/model/legacy/` would make the intent
obvious to a reviewer reading the diff and remove the "compat shim"
optic entirely. ~46 importers; mechanical `import` rewrite.

### `[ ] T-004` — `low` Consolidate 5 `GT*Ids.java` integration helpers

`integration/{jei,jade/provider,rei,emi,map}/GT*Ids.java` are
near-identical 25-line wrappers around `GTCEu.id` plus
`Identifier ↔ ResourceLocation` conversions. Folding into one
`GTIntegrationIds` (or moving each method onto `GTCEu`) touches **50+
call sites**. Skip unless one of the integration packages is being
rewritten anyway.

---

## Blocked

### `[!] T-005` — `med` Move `ldlib1` shim out of the published jar

Move `src/main/java/com/lowdragmc/lowdraglib/**` (~113 files / ~6.7
kLoC) to `src/minecraftCompat/java/com/lowdragmc/lowdraglib/**`.
`minecraftCompat`'s `Jar` and `SourcesJar` outputs are already
disabled in `gradle/scripts/jars.gradle:14-17`, so the move drops
~6.7 kLoC from the published artifact without affecting compile
output.

**Blocked by:** the shim has reverse dependencies on
`gtceu.core.compat.GuiGraphics` (T-002) and
`gtceu.client.model.compat.*` (T-003). A naïve `git mv` produces 75
compile errors because the `minecraftCompat` source set's
`compileClasspath` does not include `main.output`. Resolve T-002 and
T-003 first (or move the gtceu-side bridges into `minecraftCompat`
together so the dependency cycle stays inside one source set).

### `[ ] T-006` — `med` Restore "current BE save/load registries" hook only if needed

`LDLibRuntimeHooks` was simplified in `1a4b17c69` (deleted the no-op
auto-persist / auto-sync paths and the `BlockEntityMixin` they fed).
The `MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES` thread-local was
removed because nothing populated it. `RecipeLogic.toNbt` now reads
`HolderLookup.Provider` from `ValueTransformer.TransformerContext`
directly.

**Action only if** a future serializer turns up that lacks
`ValueOutput.lookup()` access. Restore the mixin with a real
`IPersistedSerializable`/`IManaged` instanceof check (LDLib2's
modern equivalents) — never with the silent reflection that was
there before.

---

## Done

### `[x] T-100` — `crit` Make `ldlib2` a hard runtime dependency

Resolved `94c313bb4`. `neoforge.mods.toml` was `optional` with no
`jarJar` bundling, so end users hit `NoClassDefFoundError` at install
time. Restored `type = "required"` and documented the user-install
step in `README.md`.

### `[x] T-101` — `high` Replace reflective cover lookups with method refs

Resolved `677154cc4`. Deleted `CoverBehaviourProviders.java` and
30+ `Class.forName("…FacadeCover")` calls in `GTCovers`; restored
`FacadeCover::new`-style method references.

### `[x] T-102` — `high` Drop dead `WorldSceneRendererMixin`

Resolved `63153b274`. The `@Overwrite` had a 7-param signature that
did not match LDLib2's real 10-param `renderBlocks`, AND the bundled
LDLib1 stub `WorldSceneRenderer` has no `renderBlocks` method at
all — the mixin silently failed to apply since the port. Deleted;
unregistered from `gtceu.mixins.json`.

### `[x] T-103` — `high` Drop `TagProviderCompat` reflection

Resolved `9abaa8415`. `RegistrateTagsProvider.IntrinsicImpl` exposes
`tag(TagKey)`, `rawBuilder(TagKey)`, `registry()` as public methods
that compile to the same `invokespecial` as the parent's protected
methods. The `setAccessible(true)` reflection was unnecessary.

### `[x] T-104` — `high` Rewire `LDLibRuntimeHooks` against LDLib2's API

Resolved `1a4b17c69`. The hooks were doing reflection on LDLib1
class paths that don't exist in LDLib2 (`IAutoPersistBlockEntity`,
`IAutoSyncBlockEntity` were removed; `AsyncThreadData` moved to
`lowdraglib2.async`). All but one path was always returning `false`.
Replaced with compile-time refs to `lowdraglib2.utils.virtuallevel.DummyWorld`
and `lowdraglib2.async.AsyncThreadData`; deleted the dead
`BlockEntityMixin` and `MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES`
thread-local since nothing populated them.

### `[x] T-105` — `high` ServiceLoader bridge for GameTest bootstrap

Resolved `3640c9f15` + `2512419eb`. `GTCEu.registerGameTestBootstrap`
no longer reflects `Class.forName("…GTGameTestBootstrap").getMethod`;
it goes through `ServiceLoader<IGTGameTestBootstrap>` for type safety.
Must be gated behind `if (System.getenv("TEST") == null) return;`
because MDG places the test source set on every dev-run classpath
(see T-202 lesson).

### `[x] T-106` — `high` Restore strong typing on the UI surface

Resolved across `82c9f336b`, `4e2e81ecb`, `97626f3b4`, `fb1bbd4c5`,
`52876dd7e`, `d9e4f9a2b`, `d16fdfe42`, `a32d8811d`, `e1de3b2c7`,
`41d8c01b5`. Approximately 278 `Object`-typed UI signatures restored
to their concrete LDLib types; 63 of 84 cosmetic `*UI.java` helper
splits inlined; 6 method/field access modifiers restored from
package-private back to `private`; `@Override` checks restored across
the UI provider hierarchy.

### `[x] T-107` — `high` Wire multiblock preview to LDLib2's WorldSceneRenderer

Resolved `b9f903ffe`. The legacy LDLib1 stub `WorldSceneRenderer` was
a complete no-op. Now extends `lowdragmc.lowdraglib2.client.scene.WorldSceneRenderer`
and `SceneWidget.drawInBackground` actually invokes
`renderer.render(...)`. Visual verification still pending — see
T-001.

---

## Audit findings that were wrong

These are recorded so the next reader does not chase them again.

### `[x] T-200` (wrong) — `RecipeSerializer<T>` is a record, not an interface

NeoForge 26.1.2 has
`record RecipeSerializer<T extends Recipe<?>>(MapCodec<T>, StreamCodec<RegistryFriendlyByteBuf, T>)`.
You cannot `implements` it. The `new RecipeSerializer<>(CODEC, STREAM_CODEC)`
pattern in `GTRecipeSerializers` is the correct 26.1.2 idiom. The
unavoidable cast in each subclass `getSerializer()` (covariant return
through invariant generics) was tightened in `13bdae9ca` to drop only
the `rawtypes` portion of `@SuppressWarnings`.

### `[x] T-201` (wrong) — `dev.vfyjxf:taffy` resolves from Maven Central

Audit claimed no maven hosts it. Confirmed with HTTP HEAD against
`repo1.maven.org`; `mavenCentral()` resolves the dependency without
any extra repository declaration.

### `[x] T-202` (wrong) — Kotlin `jvmTarget` already aligned with Java 25

Audit was reading the pre-`61da7d892` state. Trusted commit aligned
Kotlin to `JvmTarget.JVM_25` at `build.gradle:28`.

### `[x] T-203` (wrong) — Several "Codex-introduced" debt markers are upstream's

The `// TODO fix this.` block in `SimpleTieredMachineUI`, the
`// temporary` markers in `MonitorComponentIcons` /
`BatteryBufferMachine` / `HullMachine`, and most of the `// TODO`
comments in machine and config files are upstream-existing markers
preserved from the 1.21 branch (since at least `27612c50a`). Do not
strip them from re-PR commits. The single Codex-introduced one was
the orphan `// editor.codeEditor.setLanguageDefinition(...)` line in
`TextModuleBehaviourUI`, deleted in `ec117bda9`.

---

## Lessons learned (do not retry)

Pulled out of resolved regressions during Sessions B/C so a future
session does not re-tread the same path.

### `L-001` — `minecraftCompat` is compile-only

Shims placed in `src/minecraftCompat/java/` never reach the runtime
classpath; the source set's `Jar` and `SourcesJar` are explicitly
disabled. Anything that gtceu code or a mod-loaded plugin (LDLib2
mixin, JEI plugin, etc.) calls into at runtime must live in
`src/main/java/`. Mixin "@Mixin target was not found" warnings on
`ldlib.*Mixin` are the canonical signal that a target shim was
moved into `minecraftCompat` by mistake. Resolved by `52ca2a42c`.

### `L-002` — `INBTSerializable` lives in `gtceu.api.nbt.*` now

NeoForge 26.1 removed `net.neoforged.neoforge.common.util.INBTSerializable`
(replaced with `ValueIOSerializable`, which uses `ValueOutput`/`ValueInput`).
gtceu's `Tag`-shaped serialization callers continue to use the
`Tag`-based contract through `com.gregtechceu.gtceu.api.nbt.INBTSerializable`.
Never migrate gtceu callers back to the NeoForge FQN — it does not
resolve at runtime. Resolved by `144cd188c`.

### `L-003` — `GameTest` bootstrap must be `TEST`-env-gated

`gradle/scripts/moddevgradle.gradle:35` registers `sourceSets.test` as
part of the gtceu mod, so the test source set output (including
`META-INF/services/com.gregtechceu.gtceu.api.gametest.IGTGameTestBootstrap`)
lands on every dev-run classpath including `runClient`. Without the
`if (System.getenv("TEST") == null) return;` gate ahead of
`ServiceLoader.load`, `runClient` fires `RegisterGameTestsEvent` which
calls `Class#getDeclaredMethods()` on test classes and eagerly
resolves `GameTestPlayer` (only on the test classpath) →
`NoClassDefFoundError`. Resolved by `2512419eb`.

### `L-004` — `runData` does not propagate `localRuntime`

ModDevGradle's `data` run config does not extend the
`localRuntime` / `extraLocalRuntime` configurations into its launch
classpath. Anything required during datagen — including `ldlib2` —
must be wired as `runtimeOnly` in `dependencies.gradle`. The
`gradle/scripts/moddevgradle.gradle:148` `configureEach` block
already routes the local-runtime configurations into every other
dev-run classpath; the `if (name != "data")` guard there is
deliberate.

### `L-005` — Re-PR strategy reminder

Even after all of the above, the cumulative diff against `origin/26.1`
is large because `src/generated/resources/` is regenerated. For the
re-PR:

1. Squash-merge (or rebase down to 2 commits: code + regen).
2. Open PR #1 with code only — `1,307 files / +32k / -15k`,
   reviewable. Cherry-pick excluding `src/generated/resources/`.
3. Open PR #2 (depends on #1) with the regen pass — easy to verify
   by running `./gradlew runData` on a fresh checkout.
4. In both descriptions, point reviewers at
   `git diff --shortstat origin/26.1...HEAD -- ':!src/generated/resources/'`
   so they see the substantive change set.
5. `CLAUDE.md` is project-local guidance, not for upstream. Either
   delete it from the PR branch before submitting or move any
   upstream-relevant content into `docs/CONTRIBUTING.md`.
