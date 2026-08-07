---
title: Machine UIs
---

# Machine UIs

When any machine that extends `IMuiMachine` or has a machine panel in its definition is right-clicked, it will attempt to open a UI. 
Implementing `IMuiMachine` on a `MetaMachine` subclass is all that is needed to get a working panel:

```java
public class MyMachine extends MetaMachine implements IMuiMachine {

    public MyMachine(BlockEntityCreationInfo info) {
        super(info);
    }
}
```

Right-clicking the machine now opens a panel with a title bar and the player inventory attached.
See [Making a MUI2 Test Machine](Test-Machine.md) for a machine definition you can register to try
this out.

## How a machine panel is built

`IMuiMachine` splits UI creation into a few overridable steps. The order they run in determines
which one you want to override:

```java
default ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
    var panelBuilder = getPanelBuilder(data, syncManager, settings);       // (1)
    panelBuilder.mainContents(parent -> buildMainUI(parent, data, syncManager, settings)); // (2)
    return panelBuilder.build(syncManager, settings);                      // (3)
}
```

1. `getPanelBuilder` returns a `MachineUIPanelBuilder`, which decides what the *frame* of the panel
   looks like: title bar, player inventory, configurator buttons.
2. `buildMainUI` fills the content area in the middle of that frame. This is the method to override
   in almost every case.
3. `build` assembles everything into a `MachineUIPanel`.

!!! note
    `buildUI`, `buildMainUI` and `getPanelBuilder` all run on both the client and the server. Any
    value read from the machine has to be wrapped in a sync handler before a widget can display it.
    See [Sync Basics](Syncing/Sync-Basics.md).

## `buildMainUI`

`buildMainUI` receives the panel's main content area as a `ParentWidget<?>`, and adds children to it.
The content area defaults to `MachineUIPanel.DEFAULT_CONTENT_WIDTH` × `DEFAULT_CONTENT_HEIGHT`
(169 × 77) but will cover larger children.

```java
@Override
public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                        UISettings settings) {
    IntSyncValue bucketSyncer = new IntSyncValue(() -> cache.getFluidInTank(0).getAmount(), v -> {});
    syncManager.syncValue("bucket_amount", bucketSyncer);

    mainWidget.child(new ParentWidget<>()
            .background(GTGuiTextures.DISPLAY)
            .size(90, 63)
            .center()
            .child(Text.lang("gtceu.gui.fluid_amount").asWidget()
                    .color(0xffffff)
                    .margin(8, 0, 8, 0))
            .child(Text.dynamic(() -> Component.literal(
                            FormattingUtil.formatBuckets(bucketSyncer.getIntValue())))
                    .asWidget()
                    .margin(8, 0, 20, 0)));
}
```

`PumpMachine`, `FisherMachine` and `MinerMachine` are good references for progressively more
complicated content areas.

## `getPanelBuilder`

Override `getPanelBuilder` when you need to change the panel frame itself. `MachineUIPanelBuilder`
is a fluent builder with the following options:

| Method                            | Default                    | Effect                                                                                            |
|-----------------------------------|----------------------------|---------------------------------------------------------------------------------------------------|
| `attachInventory(boolean)`        | `true`                     | Attaches the player inventory below the content area.                                              |
| `addTitleBar(boolean)`            | `true`                     | Draws the machine name title bar.                                                                  |
| `drawGTLogo(boolean)`             | `false`                    | Draws a logo in the bottom right corner.                                                           |
| `gtLogoTexture(UITexture)`        | `GTGuiTextures.GREGTECH_LOGO` | The logo texture to use when `drawGTLogo` is enabled.                                           |
| `addDefaultConfigurators(boolean)`| `true`                     | Adds the power button, voiding button, distinct-buses button, batch mode button and recipe type button, when the machine supports them. |
| `addTraitConfigurators(boolean)`  | `true`                     | Lets attached traits implementing `IAttachConfiguratorsTrait` add their own configurators.          |
| `leftConfigurators(Consumer<Flow>)` | no-op                    | Adds your own buttons to the left configurator column.                                             |
| `rightConfigurators(Consumer<Flow>)` | no-op                   | Adds your own buttons to the right configurator column.                                            |

A machine that wants an extra toggle button next to the stock ones:

```java
@Override
public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager,
                                             UISettings settings) {
    return MachineUIPanelBuilder.panelBuilder(this)
            .rightConfigurators(configurators -> configurators
                    .child(new ToggleButton()
                            .value(new BoolValue.Dynamic(this::isJunkEnabled, this::setJunkEnabled))
                            .overlay(new ItemDrawable(Items.NAME_TAG))));
}
```

A machine that wants a bare panel with none of the stock buttons, as the primitive multiblocks do:

```java
@Override
public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager,
                                             UISettings settings) {
    return MachineUIPanelBuilder.panelBuilder(this)
            .addDefaultConfigurators(false)
            .addTraitConfigurators(false);
}
```

`MachineUIPanelBuilder.defaultSteamMachinePanelBuilder(machine)` is a shorthand for the above, and is
what `SteamBoilerMachine` uses.

!!! note
    The configurator columns are *panels of their own*, drawn to the left and right of the machine
    panel. They hide themselves when they have no children, so a machine that adds no configurators
    shows neither column.

## `shouldOpenUI`

Return `false` to suppress opening the UI for a particular interaction. The default implementation
always returns `true`. The block's `onUse` handling runs before this, so this hook only decides
whether the panel opens, not whether the click is consumed.

```java
@Override
public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
    return isFormed();
}
```

## Overriding `buildUI` directly

Override `buildUI` when you need more granular control outside what getPanelBuilder would allow you. 
One way to do this is to reproduce the default three steps, then keep working with the returned panel like so:

```java
@Override
public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
    var panelBuilder = getPanelBuilder(data, syncManager, settings);
    panelBuilder.mainContents(parent -> buildMainUI(parent, data, syncManager, settings));
    var machinePanel = panelBuilder.build(syncManager, settings);

    // ... attach extra widgets and panels to or make further changes to machinePanel here

    return machinePanel;
}
```

If you skip `getPanelBuilder`/`build` entirely and construct a plain `ModularPanel` yourself, you
lose the title bar, player inventory and configurators. Only do that for a machine whose UI has
nothing in common with the standard machine layout.

## Definition-level panels

A UI can also be attached to the machine *definition* rather than the machine class, with
`MachineBuilder::ui`. It takes a `PanelFactory`, which is the same thing as `IMuiMachine` except the
machine is handed to it as an argument instead of being `this`:

```java
ModularPanel<?> buildUIFunction(PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                MetaMachine machine);
```

```java
REGISTRATE.machine("my_machine", MyMachine::new)
        .ui((data, syncManager, settings, machine) -> MachineUIPanelBuilder.panelBuilder(machine)
                .mainContents(parent -> parent.child(Text.str("Hello").asWidget()))
                .build(syncManager, settings))
        .register();
```

This is how the shared singleblock recipe machine UI works: every simple machine gets
`GTSingleblockMachinePanels.GENERAL_MACHINE`, a `PanelFactory` that reads the machine's recipe type
UI layout and builds the slots from it. See [Recipe Type UIs](Recipe-Type-UI.md).

!!! warning
    A definition-level UI takes precedence over `IMuiMachine`. If a machine has both, only the
    definition's `PanelFactory` runs, and `buildUI`/`buildMainUI` on the machine are never called.

## Related methods

- `tryToOpenUI(Player, InteractionHand, BlockHitResult)` is called by `MetaMachineBlock`. It calls
  `shouldOpenUI` and then opens the panel through `MachineUIFactory`. You normally do not override
  this.
- `createScreen(PosGuiData, ModularPanel)` is client-only, and returns the `ModularScreen` wrapper.
  It defaults to `GTGuiScreen`. Override it only if you need a custom screen implementation.

## Multiblock display text

Multiblock controllers do not build their status readout in `buildMainUI`. `WorkableElectricMultiblockMachine`
already overrides `buildMainUI` to draw the black display box in the middle of the panel, and fills it
from `getWidgetsForDisplay(PanelSyncManager)`, a flat `List<IWidget>` rendered one entry per line.

The column that holds them is set to `collapseDisabledChildren`, so a disabled line takes up no space.
Give a widget a `setEnabledIf` to hide it without leaving a gap.

### Adding lines from the definition

The usual way to add a line is `additionalDisplay` on the machine builder, which does not require a
custom machine class. It is a `BiFunction<MultiblockControllerMachine, PanelSyncManager, List<IWidget>>`:

```java
public static final MultiblockMachineDefinition MY_MULTIBLOCK = REGISTRATE
        .multiblock("my_multiblock", WorkableElectricMultiblockMachine::new)
        // ... rotation, recipe types, pattern, model ...
        .additionalDisplay((controller, syncManager) -> {
            if (!(controller instanceof WorkableElectricMultiblockMachine machine))
                return Collections.emptyList();

            BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed",
                    BooleanSyncValue.class,
                    () -> new BooleanSyncValue(controller::isFormed));
            IntSyncValue tier = syncManager.getOrCreateSyncHandler("machineTier", IntSyncValue.class,
                    () -> new IntSyncValue(machine::getTier));

            return List.of(Text
                    .dynamic(() -> Component.translatable("my_addon.multiblock.my_multiblock.tier",
                            GTValues.VNF[tier.getIntValue()]))
                    .asWidget()
                    .setEnabledIf(w -> isFormed.getBoolValue()));
        })
        .register();
```

Most real uses need the same things this example does:

- The function is handed a `MultiblockControllerMachine`, which has no tier, no recipe logic and no
  coils. Anything more specific needs an `instanceof` check and an `emptyList()` bail-out. `getTier()`
  above comes from `ITieredMachine`, not from the base class.
- `getWidgetsForDisplay` runs on both the client and the server, and the controller's fields are only
  meaningful on the server, so anything the text depends on goes through a sync handler. Use
  `getOrCreateSyncHandler` to reuse a handler if another line already made one under that name.
- `Text.lang` and `Text.str` are evaluated once when the panel is built, while `Text.dynamic`
  re-evaluates while it is open. Use `Text.dynamic` for text that changes.
- Most status text is meaningless for an unformed structure, so guard it with `setEnabledIf`.

### Adding the same line from the machine class

If the multiblock already has its own class, override `getWidgetsForDisplay` instead. The same line as
above becomes:

```java
public class MyMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);

        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed",
                BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));
        IntSyncValue tier = syncManager.getOrCreateSyncHandler("machineTier", IntSyncValue.class,
                () -> new IntSyncValue(this::getTier));

        widgets.add(Text
                .dynamic(() -> Component.translatable("my_addon.multiblock.my_multiblock.tier",
                        GTValues.VNF[tier.getIntValue()]))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue()));

        return widgets;
    }
}
```

The syncing, `Text.dynamic` and `setEnabledIf` points above apply here too. The main difference is
that you call `super.getWidgetsForDisplay` first and add to what it returns. Dropping the `super`
call replaces the entire readout instead of adding to it. Because you own the list here, you can also
insert a line at a specific position or remove a default one.

### Where the lines end up

Lines added through `additionalDisplay` are inserted part-way down the standard list rather than
appended to the end of it, below the machine's core status lines and above the parallel and output
ones. The exact position is decided by `WorkableMultiblockMachine.getWidgetsForDisplay`.
