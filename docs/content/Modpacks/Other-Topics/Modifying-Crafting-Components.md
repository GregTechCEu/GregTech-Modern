---
title: "Modifying Crafting Components"
---

# Modifying Crafting Components

## Changing a single entry

With KubeJS it is possible to modify the predefined components of existing GTCEu Modern machine crafting recipes.
You can replace singular entries, or do bulk modification of components.
-1 is defined as a fallback value if no other entries exist. if you modify in bulk, you **MUST** insert one if the component expects it to exist. otherwise the game may crash when loading recipes. Other numbers correspond to tier indices. for example: `3 == GTValues.HV`

```js title="modification.js"
const Map = Java.loadClass("java.util.Map")

GTCEuServerEvents.craftingComponents(event => {
    event.modifyItem(CraftingComponent.CIRCUIT, GTValues.MV, Item.of('minecraft:dirt')) // (1)
    event.modifyItem(CraftingComponent.PUMP, Map.of(
        GTValues.FALLBACK, Item.of('gtceu:lv_robot_arm'),
        GTValues.LV, Item.of('gtceu:lv_robot_arm'),
        GTValues.MV, Item.of('gtceu:mv_robot_arm'),
        GTValues.HV, Item.of('gtceu:hv_robot_arm'),
        GTValues.EV, Item.of('gtceu:ev_robot_arm'),
        GTValues.IV, Item.of('gtceu:iv_robot_arm'),
        GTValues.LuV, Item.of('gtceu:luv_robot_arm'),
        GTValues.ZPM, Item.of('gtceu:zpm_robot_arm'),
        GTValues.UV, Item.of('gtceu:uv_robot_arm'),
    )) // (2)
    event.modifyTag(CraftingComponent.CASING, GTValues.EV, 'minecraft:logs') // (3)
    event.modifyUnificationEntry(CraftingComponent.PLATE, GTValues.UEV, new UnificationEntry('plate', 'gtceu:infinity')) // (4)
})
```

1. Replaces the MV circuit tag in all GT machine crafting recipes with a single block of `minecraft:dirt`.
2. Modifies all pumps in GT machine crafting recipes by replacing the pump with a robot arm, and reinserts the FALLBACK entry.
3. Replaces the EV casing with the `#minecraft:logs` tag. note the lack of `#` at the beginning of the tag!
4. Adds a new entry to the plate component for UEV with prefix `plate` and material `gtceu:infinity`.

!!! tip "Bulk Callbacks"

    All the bulk callbacks (`modify*` functions that take two parameters instead of three, see example 2) work the same as the single modification functions, except they take a map of `int: value` instead of a single entry.


## Creating new components

It's also possible to create new crafting components with KubeJS.
You must add entries for all tiers you're planning to use, and a FALLBACK if the map *may* be indexed out of bounds at all.
It's recommended to always add a FALLBACK entry.

```js title="creation.js"
const Map = Java.loadClass("java.util.Map")

let ITEM_CRAFTING_COMPONENT = null
let TAG_CRAFTING_COMPONENT = null
let UNIFICATION_CRAFTING_COMPONENT = null

GTCEuServerEvents.craftingComponents(event => {
    ITEM_CRAFTING_COMPONENT = event.createItem(Map.of(
        GTValues.FALLBACK: Item.of('minecraft:cyan_stained_glass'),
        GTValues.LV: Item.of('minecraft:cyan_stained_glass'),
        GTValues.MV: Item.of('minecraft:cyan_stained_glass'),
        GTValues.HV: Item.of('minecraft:cyan_stained_glass'),
        GTValues.EV: Item.of('minecraft:lime_stained_glass'),
        GTValues.IV: Item.of('minecraft:lime_stained_glass'),
        GTValues.LuV: Item.of('minecraft:lime_stained_glass'),
        GTValues.ZPM: Item.of('minecraft:magenta_stained_glass'),
        GTValues.UV: Item.of('minecraft:magenta_stained_glass'),
    )) // (1)
    TAG_CRAFTING_COMPONENT = event.createTag(Map.of(
        GTValues.FALLBACK: 'forge:barrels/wooden'
        GTValues.LV: 'forge:chests/wooden'
        GTValues.MV: 'forge:chests/trapped'
        GTValues.HV: 'forge:chests/ender'
        GTValues.EV: 'forge:cobblestone'
        GTValues.IV: 'forge:cobblestone/normal'
        GTValues.LuV: 'forge:cobblestone/infested'
        GTValues.ZPM: 'forge:cobblestone/mossy'
        GTValues.UV: 'forge:cobblestone/deepslate'
    )) // (2)
    UNIFICATION_CRAFTING_COMPONENT = event.createUnificationEntry(Map.of(
        GTValues.FALLBACK: new UnificationEntry('plate', 'gtceu:infinity')
        GTValues.LV: new UnificationEntry('block', 'gtceu:infinity')
        GTValues.MV: new UnificationEntry('ingot', 'gtceu:infinity')
        GTValues.HV: new UnificationEntry('dust', 'gtceu:infinity')
        GTValues.EV: new UnificationEntry('round', 'gtceu:infinity')
        GTValues.IV: new UnificationEntry('foil', 'gtceu:infinity')
        GTValues.LuV: new UnificationEntry('longRod', 'gtceu:infinity')
        GTValues.ZPM: new UnificationEntry('rod', 'gtceu:infinity')
        GTValues.UV: new UnificationEntry('bolt', 'gtceu:infinity')
    )) // (3)
})
```

1. Creates a new crafting component with item stack entries.
2. Creates a new crafting component with item tag entries. note the lack of `#` at the beginning of the tag!
3. Creates a new crafting component with UnificationEntry entries.


### Builtin Crafting Components

- `CIRCUIT`
- `BETTER_CIRCUIT`
- `PUMP`
- `WIRE_ELECTRIC`
- `WIRE_QUAD`
- `WIRE_OCT`
- `WIRE_HEX`
- `CABLE`
- `CABLE_DOUBLE`
- `CABLE_QUAD`
- `CABLE_OCT`
- `CABLE_HEX`
- `CABLE_TIER_UP`
- `CABLE_TIER_UP_DOUBLE`
- `CABLE_TIER_UP_QUAD`
- `CABLE_TIER_UP_OCT`
- `CABLE_TIER_UP_HEX`
- `CASING`
- `HULL`
- `PIPE_NORMAL`
- `PIPE_LARGE`
- `PIPE_NONUPLE`
- `GLASS`
- `PLATE`
- `HULL_PLATE`
- `MOTOR`
- `ROTOR`
- `SENSOR`
- `GRINDER`
- `SAWBLADE`
- `DIAMOND`
- `PISTON`
- `EMITTER`
- `CONVEYOR`
- `ROBOT_ARM`
- `COIL_HEATING`
- `COIL_HEATING_DOUBLE`
- `COIL_ELECTRIC`
- `STICK_MAGNETIC`
- `STICK_DISTILLATION`
- `FIELD_GENERATOR`
- `STICK_ELECTROMAGNETIC`
- `STICK_RADIOACTIVE`
- `PIPE_REACTOR`
- `POWER_COMPONENT`
- `VOLTAGE_COIL`
- `SPRING`
- `CRATE`
- `DRUM`
- `FRAME`
