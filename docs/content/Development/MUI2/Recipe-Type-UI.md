---
title: Recipe Type UIs
---

# Recipe Type UIs

A `GTRecipeType` carries a `GTRecipeTypeUILayout` that describes how recipes of that type are drawn.
The same layout drives two different UIs:

- the machine UI of singleblock machines using that recipe type, and
- the recipe viewer UI shown in recipe viewers.


## From Java
In machine UIs, the layout is declared with `GTRecipeType::UI`, which takes a `Consumer<GTRecipeTypeUILayout.Builder>`:

```java
public final static GTRecipeType CANNER_RECIPES = register("canner", ELECTRIC)
        .setMaxIOSize(2, 2, 1, 1)
        .setEUIO(IO.IN)
        .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CANNER)
                .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CANNER_OVERLAY)
                .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.CANISTER_OVERLAY)
                .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.CANISTER_OVERLAY)
                .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.DARK_CANISTER_OVERLAY)
                .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.DARK_CANISTER_OVERLAY))
        .setSound(GTSoundEntries.BATH);
```

Every recipe type starts with a default layout, so `UI` is only needed when you want to change
something. Left alone, a type draws with the `PROGRESS_ARROW` progress bar, no slot overlays, and item
and fluid grids sized from `setMaxIOSize` at up to three slots per row. If the type declares EU or
computation, the recipe viewer picks those lines up too.

The defaults cannot cover a custom `RecipeCapability`, which is described in
[Capability UIs](#capability-uis) below.

## Slot overlays

Overlays are set per capability, per slot index:

```java
.UI(builder -> builder.setItemSlotsOverlay(IO.IN, 0, 8, GTGuiTextures.ARROW_INPUT_OVERLAY))
```

| Method                                                            | Applies to                                     |
|-------------------------------------------------------------------|------------------------------------------------|
| `setItemSlotOverlay(IO, int slotIndex, IDrawable)`                 | one item slot                                  |
| `setFluidSlotOverlay(IO, int slotIndex, IDrawable)`                | one fluid slot                                 |
| `setItemSlotsOverlay(IO, int startIndex, int endIndex, IDrawable)` | a range of item slots, both ends inclusive     |
| `setFluidSlotsOverlay(IO, int startIndex, int endIndex, IDrawable)`| a range of fluid slots, both ends inclusive    |
| `setSlotOverlay(IO, int slotIndex, RecipeCapability<?>, IDrawable)`| one slot of any capability                     |
| `setSlotsOverlay(IO, int startIndex, int endIndex, RecipeCapability<?>, IDrawable)` | a range of slots of any capability |

The item and fluid methods are shorthands for the two generic ones.

Overlay textures come from [GTGuiTextures](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/common/mui/GTGuiTextures.java)
and are `UITexture`s.

## Progress bars

`setProgressBar` takes a `ProgressBarTextureSet`, which bundles the texture, its size and its fill
direction (and optionally bronze/steel variants used by steam machines):

```java
.UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
```

The fill direction is part of the texture set rather than a separate argument. If you need a set that
does not exist yet, construct your own:

```java
public static final ProgressBarTextureSet ALWAYS_FULL_ARROW =
        new ProgressBarTextureSet(20, ProgressDrawable.Direction.RIGHT, MY_ARROW_TEXTURE);
```

For anything that is not a simple filling texture, use `setProgressBarSupplier` and return a widget.
The supplier gets the layout, an `IDoubleValue<Double>` holding the 0–1 progress, and the machine
(`null` when the bar is being drawn in a recipe viewer):

```java
.UI(builder -> builder.setProgressBarSupplier((layout, value, machine) ->
        new CircularProgressDrawable()
                .emptyTexture(GTGuiTextures.PROGRESS_BATH[0])
                .filledTexture(GTGuiTextures.PROGRESS_BATH[1])
                .clockwise()
                .asWidget()
                .value(value)))
```

## Slot grids

Slots are laid out from a `String[]` matrix, where `'s'` marks a slot and a space marks a gap. This
is the same format MUI2's `SlotGroupWidget.builder().matrix(...)` takes. By default a grid is generated from
the recipe type's max slot count with a row size of at most 3, via `GTMuiWidgets.createGrid(amount,
rowSize, output, key)`.

Override it when the slot count depends on something. The macerator, for instance, shows a different
number of output slots per machine tier:

```java
.UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_MACERATE)
        .setMachineLayoutGridBuilder(ItemRecipeCapability.CAP, IO.OUT, (machine, layout) -> {
            int slots = layout.getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP);
            int width = 3;
            if (machine instanceof ITieredMachine tieredMachine) {
                if (tieredMachine.getTier() < GTValues.HV) {
                    slots = 1;
                    width = 1;
                } else if (tieredMachine.getTier() == GTValues.HV) {
                    slots = 3;
                } else {
                    slots = 4;
                    width = 2;
                }
            }
            return GTMuiWidgets.createGrid(slots, width, true, 's');
        }))
```

| Method                                                | Affects                                                          |
|--------------------------------------------------------|------------------------------------------------------------------|
| `setMachineLayoutGridBuilder(cap, io, gridBuilder)`    | machine UI only; the builder receives the `MetaMachine`           |
| `setRecipeViewerLayoutGridBuilder(cap, io, gridBuilder)` | recipe viewer UI only                                           |
| `setLayoutGridBuilder(cap, io, gridBuilder)`           | both, from a single `Function<GTRecipeTypeUILayout, String[]>`    |

## Recipe UI modifiers

A `RecipeUIModifier` is a `(GTRecipe recipe, GTRecipeViewerWidget widget) -> void` callback that runs
once per recipe when the recipe viewer entry is built. It is handed the widget tree, so it can attach
text, slots, rows, tooltips or anything else. This is how per-recipe extra information gets into the
recipe viewer.

```java
.UI(builder -> builder.addRecipeUIModifier((recipe, widget) ->
        widget.textComponents.child(
                Text.lang("emi_info.example.projector_info", recipe.data.getByte("projector_tier"))
                        .asWidget())))
```

### Where to attach

`GTRecipeViewerWidget` exposes a few public attachment points:

| Field                     | What it is                                                                                  |
|---------------------------|---------------------------------------------------------------------------------------------|
| `textComponents`          | The column of text lines under the recipe. Disabled children collapse, so empty lines vanish. |
| `additionalRecipeContent` | The full-width area below the recipe row that `textComponents` lives in.                     |
| `inputColumn`             | The column of input slots.                                                                   |
| `outputColumn`            | The column of output slots.                                                                  |
| `recipeContentRow`        | The row holding inputs, progress bar and outputs.                                             |

Most modifiers want `textComponents`.

### Conditional lines

To show a line only for some recipes, add the widget unconditionally and add a condition to only show it with `.setEnabledIf`.
`textComponents` is set to `collapseDisabledChildren`, so a disabled line takes up no space:

```java
.addRecipeUIModifier((recipe, widget) -> widget.textComponents
        .child(Text.dynamic(() -> {
                    if (!recipe.data.contains("updated_microverse")) return Component.empty();
                    return Component.translatable("emi_info.monilabs.new_microverse",
                            Component.translatable(
                                    Microverse.values()[recipe.data.getInt("updated_microverse")].langKey));
                })
                .asWidget()
                .setEnabledIf(w -> recipe.data.contains("updated_microverse"))))
```

Use `Text.lang(...)`/`Text.str(...)` for text that never changes, and `Text.dynamic(...)` for text
that is recomputed while the panel is open (for example when the viewer's overclock tier button is
used).

!!! note
    There is no need to reserve vertical space for lines you might add later. The text column sizes
    itself to its enabled children.

### Reusable modifiers

`RecipeUIModifier` has helpers for the common cases:

```java
RecipeUIModifier.textLine(Text.lang("gtceu.recipe.byproduct_tier", GTValues.VNF[GTValues.HV]))
RecipeUIModifier.all(modifierA, modifierB)   // run several
modifierA.then(modifierB, modifierC)         // run this one, then the others
```

GTM ships a few in `GTRecipeUIModifiers`:

- `TEMP_COIL_INFO` adds the EBF temperature line plus the coils that satisfy it. Used by
  `BLAST_RECIPES` and the GCYM alloy blast smelter.
- `RESEARCH_INFO` adds the data item catalyst slots for research-gated recipes.

### From recipe conditions

`RecipeCondition::modifyUI` returns a `RecipeUIModifier`, so a condition can render itself. The base
implementation writes the condition's tooltip as a text line:

```java
public RecipeUIModifier modifyUI() {
    return RecipeUIModifier.textLine(Text.of(getTooltips()));
}
```

Override it on your own condition when a text line is not enough. `AdjacentBlockCondition`,
`AdjacentFluidCondition`, `DimensionCondition` and `ResearchCondition` all do. Condition modifiers
run before the layout's own modifiers.

## Capability UIs

Everything above is per-slot styling of the *default* item and fluid layouts. When you add a custom
`RecipeCapability`, the layout does not know how to draw it, so you have to supply that yourself.
Three separate hooks are involved, because the machine UI, the recipe viewer layout and the recipe
*contents* are built at different times.

### 1. Machine UI layout, `setMachineCapabilityLayoutBuilder`

A `MachineCapabilityLayoutBuilder` creates the widgets for one capability inside a singleblock
machine UI. It receives the machine, the layout, the `GTRecipeTypeMachineWidget` and the IO mode, and
is expected to attach its widgets to `widget.inputColumn` or `widget.outputColumn`.

The defaults are `MachineCapabilityLayoutBuilder.ITEM` and `.FLUID`, which read the machine's
`NotifiableItemStackHandler`/`NotifiableFluidTank` and build a `SlotGroupWidget` from the capability's
machine grid.

### 2. Recipe viewer layout, `setRecipeViewerLayoutCapabilityLayoutBuilder`

A `RecipeViewerCapabilityLayoutBuilder` does the same job for the recipe viewer. There is no machine
to read from here, so it creates empty widgets that get filled in step 3. Each widget must be named
`GTRecipeViewerWidget.capabilityWidgetName(cap, io, index)`, which is how the content builder finds
it later.

Defaults exist for `ITEM`, `FLUID`, `EU` and `COMPUTATION`. The EU and computation ones show how to
handle a capability that is not slot-shaped: instead of slots they attach an empty `Flow` column to
`widget.textComponents`.

### 3. Recipe contents, `setCapabilityContentBuilder`

A `CapabilityContentBuilder` takes in one `Content` and the widget that step 2 created and then builds the content into it:

```java
void buildWidgetContent(IWidget widget, Content content, IO io, boolean perTick,
                        GTRecipeType recipeType, GTRecipe recipe, int chanceTier, int recipeTier);
```

`GTRecipeViewerWidget` walks the recipe's contents per capability, looks up the widget with the
matching name, and hands both to this builder. The builder is responsible for checking the widget
type it got. `CapabilityContentBuilder.ITEM` and `.FLUID` start with
`if (!(widget instanceof RecipeViewerSlotWidget<?> slot)) return;`, while `.COMPUTATION` and `.EU`
expect a `Flow` and add text children to it.

`perTick` distinguishes per-tick contents from per-recipe ones, and `chanceTier` and `recipeTier` are
the tier the recipe is currently being previewed at, which the recipe viewer's tier button changes.

A minimal custom capability therefore looks like:

```java
.UI(builder -> builder
        .setRecipeViewerLayoutCapabilityLayoutBuilder(MyCapabilities.CHROMA, (layout, widget, io) -> {
            if (layout.getRecipeType().getMaxSlots(MyCapabilities.CHROMA, io) == 0) return;
            widget.textComponents.child(Flow.col()
                    .coverChildrenHeight()
                    .widthRel(1f)
                    .name(GTRecipeViewerWidget.capabilityWidgetName(MyCapabilities.CHROMA, io, 0)));
        })
        .setCapabilityContentBuilder(MyCapabilities.CHROMA,
                (widget, content, io, perTick, recipeType, recipe, chanceTier, recipeTier) -> {
                    if (!(widget instanceof Flow flow)) return;
                    flow.child(Text.lang("my.recipe.chroma",
                            MyCapabilities.CHROMA.of(content.content())).asWidget());
                }))
```

## Replacing the whole layout

`customRecipeTypeUI(Function<GTRecipe, Flow>)` replaces the generated input/progress/output row
outright and returns your own `Flow`. This overrides the grid builders, the recipe viewer capability
layout builders and the progress bar for that recipe type, so only use it when the standard
three-column shape does not fit.

## In KubeJS

KubeJS scripts do not build or touch widgets directly. `GTRecipeTypeBuilder`
exposes the common options as plain builder methods instead:

```js
GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
    event.create('test_recipe_type')
        .category('test')
        .setEUIO('in')
        .setMaxIOSize(3, 3, 3, 3)
        .setProgressBar(GTGuiTextures.PROGRESS_ARROW)
        .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.SOLIDIFIER_OVERLAY)
        .addRecipeInfo(recipe => Text.literal(`Temperature: ${recipe.data.getInt('RequiredTemp')}K`))
        .setSound(GTSoundEntries.COOLING)
})
```

| Method                                                        | Wraps                                              |
|----------------------------------------------------------------|----------------------------------------------------|
| `setProgressBar(textureSet)`                                    | `setProgressBar`                                   |
| `setItemSlotOverlay(io, index, overlay)`                        | `setItemSlotOverlay`                               |
| `setItemSlotsOverlay(io, startIndex, endIndex, overlay)`        | `setItemSlotsOverlay`                              |
| `setFluidSlotOverlay(io, index, overlay)`                       | `setFluidSlotOverlay`                              |
| `setFluidSlotsOverlay(io, startIndex, endIndex, overlay)`       | `setFluidSlotsOverlay`                             |
| `setSlotOverlay(io, index, cap, overlay)`                       | `setSlotOverlay`                                   |
| `setSlotsOverlay(io, startIndex, endIndex, cap, overlay)`       | `setSlotsOverlay`                                  |
| `addRecipeInfo(recipe => component)`                            | `addRecipeUIModifier`, adding one text line        |

`addRecipeInfo` is the KubeJS-facing form of a recipe UI modifier. It is given the recipe and returns
the component to show for it. Returning `Text.empty()` draws no line, which is how a line is limited
to some recipes. Call it more than once for more than one line.

```js
.addRecipeInfo(recipe => recipe.data.contains('RequiredTemp') ?
    Text.literal(`Temperature: ${recipe.data.getInt('RequiredTemp')}K`) : Text.empty())
```

Build the return value with KubeJS's `Text` bindings, such as `Text.literal(string)` and
`Text.translate(langKey, args...)`.

`GTGuiTextures`, `IO` and `RecipeCapability` are bound as globals. Capabilities also accept their id
as a string, so `setSlotsOverlay(IO.IN, 0, 2, 'item', overlay)` works.

As in Java, calling none of these is fine, and the type keeps its default layout.
