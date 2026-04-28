---
title: LDLib2 Migration State
---


# LDLib2 Migration State

This page records the current LDLib2 migration state for the 26.1.2 port. It is a working snapshot from
2026-04-29, not a final migration report. Treat it as the handoff page for future LDLib2 UI and renderer work.

GTCEu is running on Minecraft `26.1.2`, NeoForge `26.1.2.29-beta`, and the vendored patched LDLib2 artifact
`libs/ldlib2/ldlib2-neoforge-26.1.2-gtceu.1.jar`. The port is intentionally using compatibility shims so the old UI
and renderer call sites do not all have to be rewritten by hand before the game can launch.

!!! warning "Migration is still in progress"

    Client startup and formatting currently pass, but machine rendering is only partially restored and data generation
    currently fails before writing resources. See the verification and known issues sections before assuming a surface is
    fully ported.


## Shim Baseline Commit

The committed baseline for this phase is:

```text
b69ac013210bf2043c21414366b6c221b9f63b0c fix(ldlib): add bundled compatibility shims
```

That commit is `HEAD` on the local `1.21` branch and matches `upstream/1.21` at the time of this snapshot. It added
the first bundled compatibility surface:

- 114 files changed.
- 6522 insertions and 2 deletions.
- New `com.lowdragmc.lowdraglib` compatibility classes for widgets, textures, modular UI, recipe viewer wrappers,
  render helpers, dummy worlds, sync payloads, and utility types.
- A small update to `com.gregtechceu.gtceu.core.LDLibRuntimeHooks`.

The baseline commit should be read as "old LDLib namespace is available enough to keep porting moving." It is not the
complete 26.1.2 migration state.


## Current Working Diff From Baseline

At the time of this snapshot, the working tree has a large migration diff on top of `b69ac0132`.

| Area | Current state |
| --- | --- |
| Non-generated tracked diff | 115 files, 2462 insertions, 1093 deletions, excluding generated machine blockstate and item JSON. |
| Generated machine assets | 2452 checked-in files changed under `src/generated/resources/assets/gtceu`: 1226 blockstates and 1226 item definitions. |
| New bridge files | `LegacyCustomBlockStateModel.java`, `LegacyCustomItemModel.java`, `ModelBakingUtil.java`, and `core/compat/GuiGraphics.java`. |

Important changes in that diff:

| Area | Files and behavior |
| --- | --- |
| Build and runtime classpath | `gradle/forge.versions.toml` still records the required LDLib2 version as `26.1.2-gtceu.1`, while `dependencies.gradle` compiles and runs against the tracked patched jar under `libs/ldlib2`. `build.gradle` routes `extraLocalRuntime` onto the client extra runtime path instead of leaving the old `26.0.0` runtime artifact active. |
| Shim follow-up work | The bundled `com.lowdragmc.lowdraglib` classes have been adjusted for 26.1.2 rendering, GUI, item stack, and recipe viewer shapes after the baseline commit. |
| GTCEu UI call sites | Many `api/gui`, `api/recipe/ui`, `api/machine/feature`, and `integration/ae2/gui` call sites now use the compatibility surface instead of directly depending on old LDLib behavior that changed under LDLib2. |
| Compatibility GUI helpers | `src/main/java/com/gregtechceu/gtceu/core/compat/GuiGraphics.java` and the `src/minecraftCompat/java/net/minecraft/client/gui` classes bridge GUI drawing methods that moved or changed shape. |
| Legacy model bridge | `LegacyCustomBlockStateModel`, `LegacyCustomItemModel`, and `ModelBakingUtil` bridge GTCEu's existing machine and pipe model generators into the NeoForge 26.1.2 model and item-definition systems. |
| Dynamic client resources | `ClientProxy` registers `gtceu:legacy_model` and `gtceu:legacy_item_model` codecs and injects dynamic blockstate/item definitions for machines and pipe blocks through `GTDynamicResourcePack`. |
| Model generation | `GTMachineModels` emits legacy blockstate models, while `MachineBuilder` emits legacy item definitions for machine items. The checked-in generated JSON reflects that bridge. |
| Baked model compatibility | `client/model/compat/BakedModel.java` now gathers directional and non-directional quads for item rendering, preserves model identity, handles foil and render flags, and cooperates with tint lookups. |
| Tint handling | `GTBlockTintSources` and `GTItemColors` normalize legacy RGB colors through ARGB conversion so block and item tints use the 26.1.2 color contract. |
| Quad and ore rendering | `OreBlockRenderer`, `StaticFaceBakery`, `GTQuadTransformers`, and the material-set ore model JSONs were updated for the current quad/render type assumptions. Material blocks no longer need composite models just to choose render type; the quad render type follows its texture. |
| Mixins | Client bakery/accessor mixins and LDLib dummy-world mixins were adjusted for current Minecraft/NeoForge classes, and a stale mixin entry was removed from `gtceu.mixins.json`. |


## Current Model Contract

Machine blockstates are being represented as legacy custom blockstate models. A generated blockstate now looks like:

```json
{
  "variants": {
    "": {
      "model": "gtceu:block/machine/lv_electric_furnace",
      "type": "gtceu:legacy_model"
    }
  }
}
```

Machine items use the 26.1.2 item definition path and point at the same legacy model:

```json
{
  "model": {
    "type": "gtceu:legacy_item_model",
    "model": "gtceu:block/machine/lv_electric_furnace"
  }
}
```

The codecs for those two `type` values are registered from `ClientProxy`. The backing implementations try to bake the
existing `IUnbakedGeometry` model first, then fall back to wrappers around vanilla block or item model output when a
model is not one of GTCEu's legacy geometry types.


## Verification Status

| Command | Status | Notes |
| --- | --- | --- |
| `.\gradlew.bat spotlessCheck` | Passes | Verified after running `spotlessApply`; current Java/Kotlin formatting is clean. |
| `.\gradlew.bat assemble` | Passes | The mod compiles and packages with the current compatibility layer. |
| `.\gradlew.bat runClient` | Passes startup | Verified after the `TabsWidget` null-tab guard. The latest client launch loaded `LowDragLib2 26.1.2-gtceu.1`, exited cleanly, and did not report `gtceu:legacy_model`, `gtceu:legacy_item_model`, or machine block model parse failures. It still reports the known `neoforge:fluid_container` item model loader errors. |
| `.\gradlew.bat runData` | Fails | Fails during bootstrap with `NoClassDefFoundError: com/lowdragmc/lowdraglib2/gui/factory/BlockUIMenuType$BlockUI`. The datagen run did not include LDLib2 in the loaded mod list, so this is currently treated as a datagen classpath issue. |


## Known Remaining Issues

- Machine rendering is improved but not complete. The front face is visible in-world, but other sides and item icons
  still need validation and follow-up fixes.
- Restrictive item pipe variants still log missing block models during client resource loading.
- Controller overlay UVs were reported wrong and still need targeted renderer verification.
- Several fluid container item models still log `Unknown loader: neoforge:fluid_container`.
- Machine GUI opening still needs validation. The null-tab screen crash found during this pass is guarded, but the
  actual in-world machine GUI path still needs targeted testing.
- LDLib and GTCEu `@OnlyIn` warnings remain noisy during client startup.
- `runData` cannot be trusted until the LDLib2 datagen classpath issue is fixed. Do not hand-edit broad generated
  machine JSON as the final solution; update the generators and rerun datagen once it boots.
- The patched LDLib2 jar is vendored because `26.1.2-gtceu.1` is a local GTCEu migration build, not a normal upstream
  published version. Keep the binary, sources jar, and LGPL-3.0 license together under `libs/ldlib2` until it is
  replaced by a public Maven artifact.


## Working Rules For Further Porting

- Use `b69ac0132` as the shim baseline when explaining history, but use the current working diff for actual migration
  state.
- Prefer extending the compatibility shims when that keeps existing UI code working with minimal behavioral change.
- Move call sites to direct LDLib2 APIs only when the new API is stable and the conversion is local enough to review.
- Keep generated machine asset changes tied to `GTMachineModels`, `MachineBuilder`, and runtime dynamic resource
  generation. Once `runData` is fixed, regenerate instead of preserving stale JSON by hand.
- Recheck `spotlessCheck`, `assemble`, and `runClient` after UI, renderer, or shim edits. Recheck `runData` after any
  datagen classpath or generated-resource fix.
