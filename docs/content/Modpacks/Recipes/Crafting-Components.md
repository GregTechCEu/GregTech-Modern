---
title: "Crafting Components"
---

# Crafting Components

Crafting Components are a way to organize and simplify the various similar recipes that GregTech generates.
For example: writing out the crafting recipes for all tiers of the Alloy Smelter can be done in a condensed way.

GregTech makes use of its CraftingComponents to generate the majority of its recipes to make machines.

Crafting Components are a map pairing a Voltage Tier (the tier number) to a value.
The value can be a `MaterialEntry`, `ItemStack`, or `TagKey<Item>`.

## Modifying Entries

With KubeJS it is possible to modify the predefined components of existing GTCEu Modern Crafting Components.
You can replace singular entries, or do bulk modification of components.
You can also change the fallback entry.

```js title="startup/modification.js"
const Map = Java.loadClass('java.util.Map')

GTCEuStartupEvents.craftingComponents(event => {
    event.setItem(GTCraftingComponents.CIRCUIT, GTValues.MV, Item.of('minecraft:dirt')) // (1)
    event.setItems(GTCraftingComponents.PUMP, Map.of(
        GTValues.LV, Item.of('gtceu:lv_robot_arm'),
        GTValues.MV, Item.of('gtceu:mv_robot_arm'),
        GTValues.HV, Item.of('gtceu:hv_robot_arm'),
        GTValues.LuV, Item.of('gtceu:luv_robot_arm'),
    )) // (2)
    event.setTag(GTCraftingComponents.CASING, GTValues.EV, 'minecraft:logs') // (3)
    event.setMaterialEntry(GTCraftingComponents.PLATE, GTValues.UEV, new MaterialEntry('plate', 'gtceu:infinity')) // (4)
    event.removeTier(GTCraftingComponents.SENSOR, GTValues.HV) // (5)
    event.setFallbackItem(GTCraftingComponents.MOTOR, Item.of('minecraft:stone')) // 6
})
```

1. Replaces the MV circuit tag in all GT machine crafting recipes with a single block of `minecraft:dirt`.
2. Modifies the LV, MV, HV, and LuV Pumps in GT machine crafting recipes by replacing the Pump with a Robot Arm.
3. Replaces the EV casing with the `#minecraft:logs` tag. Note the lack of `#` at the beginning of the tag!
4. Adds a new entry to the plate component for UEV with prefix `plate` and material `gtceu:infinity`.
5. Removes the HV Tier entry of the Sensor Crafting Component, so it will default to the fallback for the component `(LV Sensor)`
6. Replaces the fallback item for the Motor Crafting Component with a block of `minecraft:stone`.

All functions provided by the Crafting Components Event:
```ts
/**
 * Set a component's Item for a tier.
 * @param component The component to modify
 * @param tier      The tier to set the item for
 * @param item      The ItemStack to set, as a KubeJS item string.
 *                  E.g. `Item.of('minecraft:dirt')`.
 */
setItem(component: CraftingComponent, tier: int, item: ItemStack): void;

/**
 * Set a component's Tag for a tier.
 * @param component The component to modify
 * @param tier      The tier to set the tag for
 * @param tag       The Item Tag to set, as a ResourceLocation string.
 *                  E.g. `'minecraft:logs'` (note the lack of '#' at the beginning).
 */
setTag(component: CraftingComponent, tier: int, tag: Tag): void;

/**
 * Set a component's MaterialEntry for a tier.
 * @param component The component to modify
 * @param tier      The tier to set the MaterialEntry for
 * @param entry     The Entry to set, as a Java MaterialEntry Object.
 *                  E.g. `new MaterialEntry('plate', 'gtceu:brass')`.
 */
setMaterialEntry(component: CraftingComponent, tier: int, entry: MaterialEntry): void;

/**
 * Set a component's Items for many tiers.
 * @param component The component to modify
 * @param map       The map of Tiers to ItemStacks to set
 *                  E.g. `Map.of(GTValues.LV, Item.of('minecraft:dirt'), GTValues.HV, Item.of('minecraft:stone'))`.
 */
setItems(component: CraftingComponent, map: Map<int, ItemStack>): void;

/**
 * Set a component's Tags for many tiers.
 * @param component The component to modify
 * @param map       The map of Tiers to Item Tag ResourceLocation strings to set
 *                  E.g. `Map.of(GTValues.LV, 'minecraft:logs', GTValues.HV, 'forge:ingots')`.
 */
setTags(component: CraftingComponent, map: Map<int, Tag>): void;

/**
 * Set a component's MaterialEntries for many tiers.
 * @param component The component to modify
 * @param map       The map of Tiers to MaterialEntries to set
 *                  E.g. `Map.of(GTValues.LV, new MaterialEntry('plate', 'gtceu:brass'), GTValues.HV, new MaterialEntry('plate', 'gtceu:silver'))`.
 */
setMaterialEntries(component: CraftingComponent, map: Map<int, MaterialEntry>): void;

/**
 * Set a component's Objects for many tiers.
 * Functions identically to `setItems`, `setTags`, and `setEntries`, but allows providing a map containing all three types of objects.
 * @param component The component to modify
 * @param map       The map of Tiers to Objects to set
 *                  E.g. `Map.of(GTValues.LV, Item.of('minecraft:dirt'), GTValues.HV, new MaterialEntry('plate', 'gtceu:silver'))`.
 */
set(component: CraftingComponent, map: Map<int, ItemStack | Tag | MaterialEntry>): void;

/**
 * Set a component's fallback Item.
 *
 * @param component The component to modify
 * @param item      The ItemStack to set, as a KubeJS item string.
 *                  E.g. `Item.of('minecraft:dirt')`.
 */
setFallbackItem(component: CraftingComponent, item: ItemStack): void;

/**
 * Set a component's fallback Tag.
 *
 * @param component The component to modify
 * @param tag       The Item Tag to set, as a ResourceLocation string.
 *                  E.g. `'minecraft:logs'` (note the lack of '#' at the beginning).
setFallbackTag(component: CraftingComponent, tag: Tag): void;

/**
 * Set a component's fallback MaterialEntry.
 *
 * @param component The component to modify
 * @param entry     The Entry to set, as a Java MaterialEntry Object.
 *                  E.g. `new MaterialEntry('plate', 'gtceu:brass')`.
 */
setFallbackMaterialEntry(component: CraftingComponent, entry: MaterialEntry): void;

/**
 * Remove a tier for a component.
 * Usage of the component's for the tier will default to the fallback value unless set otherwise.
 *
 * @param component The component to modify
 * @param tier      The tier to remove
 */
removeTier(component: CraftingComponent, tier: int): void;

/**
 * Remove multiple tiers for a component.
 * Usage of the component's for these tiers will default to the fallback value unless set otherwise.
 *
 * @param component The component to modify
 * @param tiers     The tiers to remove
 */
removeTiers(component: CraftingComponent, tier: int...): void;
```

## Creating new components

It's also possible to create new crafting components with KubeJS.
The crafting component is constructed with a id and a fallback value.

You can add entries by chaining `.addX(tier, value)` method calls after construction.
You are free to use any combination of Items, Tags, and MaterialEntries in all CraftingComponents.

```js title="creation.js"
const GTCraftingComponents = Java.loadClass('com.gregtechceu.gtceu.data.recipe.GTCraftingComponents')
const Map = Java.loadClass('java.util.Map')

let ITEM_CRAFTING_COMPONENT = null
let TAG_CRAFTING_COMPONENT = null
let MATERIAL_ENTRY_CRAFTING_COMPONENT = null

GTCEuServerEvents.craftingComponents(event => {
    // (1)
    ITEM_CRAFTING_COMPONENT = event.createItem('item_component', 'minecraft:cyan_stained_glass')
        .addItem(GTValues.LV, Item.of('minecraft:cyan_stained_glass'))
        .addItem(GTValues.MV, Item.of('minecraft:cyan_stained_glass'))
        .addItem(GTValues.HV, Item.of('minecraft:cyan_stained_glass'))
        .addItem(GTValues.EV, Item.of('minecraft:lime_stained_glass'))
        .addItem(GTValues.IV, Item.of('minecraft:lime_stained_glass'))
        .addItem(GTValues.LuV, Item.of('minecraft:lime_stained_glass'))
        .addItem(GTValues.ZPM, Item.of('minecraft:magenta_stained_glass'))
        .addItem(GTValues.UV, Item.of('minecraft:magenta_stained_glass'))
    // (2)
    TAG_CRAFTING_COMPONENT = event.createTag('tag_component', 'forge:barrels/wooden')
        .addTag(GTValues.LV, 'forge:chests/wooden')
        .addTag(GTValues.MV, 'forge:chests/trapped')
        .addTag(GTValues.HV, 'forge:chests/ender')
        .addTag(GTValues.EV, 'forge:cobblestone')
        .addTag(GTValues.IV, 'forge:cobblestone/normal')
        .addTag(GTValues.LuV, 'forge:cobblestone/infested')
        .addTag(GTValues.ZPM, 'forge:cobblestone/mossy')
        .addTag(GTValues.UV, 'forge:cobblestone/deepslate')
    // (3)
    MATERIAL_ENTRY_CRAFTING_COMPONENT = event.createMaterialEntry('material_entry_component', new MaterialEntry('plate', 'gtceu:infinity'))
        .addMaterialEntry(GTValues.LV, new MaterialEntry('block', 'gtceu:infinity'))
        .addMaterialEntry(GTValues.MV, 'ingot', 'gtceu:infinity')
        .addMaterialEntry(GTValues.HV, new MaterialEntry('dust', 'gtceu:infinity'))
        .addMaterialEntry(GTValues.EV, new MaterialEntry('round', 'gtceu:infinity'))
        .addMaterialEntry(GTValues.IV, new MaterialEntry('foil', 'gtceu:infinity'))
        .addMaterialEntry(GTValues.LuV, 'longRod', 'gtceu:infinity')
        .addMaterialEntry(GTValues.ZPM, new MaterialEntry('rod', 'gtceu:infinity'))
        .addMaterialEntry(GTValues.UV, new MaterialEntry('bolt', 'gtceu:infinity'))
})
```

1. Creates a new crafting component with ItemStack entries.
2. Creates a new crafting component with Item Tag entries. Note the lack of `#` at the beginning of the tag!
3. Creates a new crafting component with MaterialEntry entries.

### Using Crafting Components in Recipes

Currently, Crafting Components cannot be easily used to bulk-generate recipes in KubeJS.
However, GregTech CEu Modern and addons using CraftingComponents will use KubeJS's modifications.

### Built-in Crafting Components

The following is a list of built-in CraftingComponents and their associated id.

- `CIRCUIT`: `'circuit'`
- `BETTER_CIRCUIT`: `'better_circuit'`
- `WIRE_ELECTRIC`: `'wire_single'`
- `WIRE_QUAD`: `'wire_quad'`
- `WIRE_OCT`: `'wire_oct'`
- `WIRE_HEX`: `'wire_hex'`
- `CABLE`: `'cable_single'`
- `CABLE_DOUBLE`: `'cable_double'`
- `CABLE_QUAD`: `'cable_quad'`
- `CABLE_OCT`: `'cable_oct'`
- `CABLE_HEX`: `'cable_hex'`
- `CABLE_TIER_UP`: `'cable_tier_up_single'`
- `CABLE_TIER_UP_DOUBLE`: `'cable_tier_up_double'`
- `CABLE_TIER_UP_QUAD`: `'cable_tier_up_quad'`
- `CABLE_TIER_UP_OCT`: `'cable_tier_up_oct'`
- `CABLE_TIER_UP_HEX`: `'cable_tier_up_hex'`
- `CASING`: `'casing'`
- `HULL`: `'hull'`
- `PIPE_NORMAL`: `'normal_pipe'`
- `PIPE_LARGE`: `'large_pipe'`
- `PIPE_NONUPLE`: `'nonuple_pipe'`
- `GLASS`: `'glass'`
- `PLATE`: `'plate'`
- `HULL_PLATE`: `'hull_plate'`
- `ROTOR`: `'rotor'`
- `GRINDER`: `'grinder'`
- `SAWBLADE`: `'sawblade'`
- `DIAMOND`: `'diamond'`
- `MOTOR`: `'motor'`
- `PUMP`: `'pump'`
- `PISTON`: `'piston'`
- `EMITTER`: `'emitter'`
- `SENSOR`: `'sensor'`
- `CONVEYOR`: `'conveyor'`
- `ROBOT_ARM`: `'robot_arm'`
- `FIELD_GENERATOR`: `'field_generator'`
- `COIL_HEATING`: `'coil_heating'`
- `COIL_HEATING_DOUBLE`: `'coil_heating_double'`
- `COIL_ELECTRIC`: `'coil_electric'`
- `STICK_MAGNETIC`: `'rod_magnetic'`
- `STICK_DISTILLATION`: `'rod_distillation'`
- `STICK_ELECTROMAGNETIC`: `'rod_electromagnetic'`
- `STICK_RADIOACTIVE`: `'rod_radioactive'`
- `PIPE_REACTOR`: `'pipe_reactor'`
- `POWER_COMPONENT`: `'power_component'`
- `VOLTAGE_COIL`: `'voltage_coil'`
- `SPRING`: `'spring'`
- `CRATE`: `'crate'`
- `DRUM`: `'drum'`
- `FRAME`: `'frame'`
- `SMALL_SPRING_TRANSFORMER`: `'small_spring_transformer'`
- `SPRING_TRANSFORMER`: `'spring_transformer'`
