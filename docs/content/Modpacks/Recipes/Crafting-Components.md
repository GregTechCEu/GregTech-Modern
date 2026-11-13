---
title: "Crafting Components"
---

# Crafting Components

Crafting Components are a way to organize and simplify the various similar recipes that GregTech generates. For example: writing out the recipes for all tiers of an alloy smelter can be done iteratively rather than one by one.

Crafting Components are a map pairing a Voltage tier (the tier number) to a value. The value can be a `MaterialEntry`, `ItemStack`, or `TagKey<Item>`.

## Changing a single entry

With KubeJS it is possible to modify the predefined components of existing GTCEu Modern machine crafting recipes.
You can replace singular entries, or do bulk modification of components.
-1 is defined as a fallback value if no other entries exist.

```js title="startup/modification.js"
const Map = Java.loadClass("java.util.Map")

GTCEuStartupEvents.craftingComponents(event => {
    event.setItem(GTCraftingComponents.CIRCUIT, GTValues.MV, Item.of('minecraft:dirt')) // (1)
    event.setItems(GTCraftingComponents.PUMP, Map.of(
        GTValues.LV, Item.of('gtceu:lv_robot_arm'),
        GTValues.MV, Item.of('gtceu:mv_robot_arm'),
        GTValues.HV, Item.of('gtceu:hv_robot_arm'),
        GTValues.EV, Item.of('gtceu:ev_robot_arm'),
        GTValues.IV, Item.of('gtceu:iv_robot_arm'),
        GTValues.LuV, Item.of('gtceu:luv_robot_arm'),
        GTValues.ZPM, Item.of('gtceu:zpm_robot_arm'),
        GTValues.UV, Item.of('gtceu:uv_robot_arm')
    )) // (2)
    event.setTag(GTCraftingComponents.CASING, GTValues.EV, 'minecraft:logs') // (3)
    event.setMaterialEntry(GTCraftingComponents.PLATE, GTValues.UEV, 'plate:infinity') // (4)
    event.removeTier("sensor", 3) // (5)
})
```
1. Replaces the MV circuit tag in all GT machine crafting recipes with a single block of `minecraft:dirt`.
2. Modifies all pumps in GT machine crafting recipes by replacing the pump with a robot arm.
3. Replaces the EV casing with the `#minecraft:logs` tag. note the lack of `#` at the beginning of the tag!
4. Adds a new entry to the plate component for UEV with prefix `plate` and material `gtceu:infinity`. A `MaterialEntry` can be written as a `'prefix:material'` string.
5. Removes the 3rd offset entry `(HV Tier)` of the sensor crafting component, will default to the fallback `(LV Sensor)`


## Creating new components

It's also possible to create new crafting components with KubeJS.
The crafting component is constructed with a id and a fallback value. You can add entries by chaining `.add(tier, value)` methods after your construction.

```js title="creation.js"
const Map = Java.loadClass("java.util.Map")

let ITEM_CRAFTING_COMPONENT = null
let TAG_CRAFTING_COMPONENT = null
let UNIFICATION_CRAFTING_COMPONENT = null

GTCEuStartupEvents.craftingComponents(event => {
    ITEM_CRAFTING_COMPONENT = event.createItem("item_component", 'minecraft:cyan_stained_glass')
        .addItem(GTValues.LV, Item.of('minecraft:cyan_stained_glass'))
        .addItem(GTValues.MV, Item.of('minecraft:cyan_stained_glass'))
        .addItem(GTValues.HV, Item.of('minecraft:cyan_stained_glass'))
        .addItem(GTValues.EV, Item.of('minecraft:lime_stained_glass'))
        .addItem(GTValues.IV, Item.of('minecraft:lime_stained_glass'))
        .addItem(GTValues.LuV, Item.of('minecraft:lime_stained_glass'))
        .addItem(GTValues.ZPM, Item.of('minecraft:magenta_stained_glass'))
        .addItem(GTValues.UV, Item.of('minecraft:magenta_stained_glass'))
    // (1)
    TAG_CRAFTING_COMPONENT = event.createTag("tag_component", 'c:barrels/wooden')
        .addTag(GTValues.LV, 'c:chests/wooden')
        .addTag(GTValues.MV, 'c:chests/trapped')
        .addTag(GTValues.HV, 'c:chests/ender')
        .addTag(GTValues.EV, 'c:cobblestones')
        .addTag(GTValues.IV, 'c:cobblestones/normal')
        .addTag(GTValues.LuV, 'c:cobblestones/infested')
        .addTag(GTValues.ZPM, 'c:cobblestones/mossy')
        .addTag(GTValues.UV, 'c:cobblestones/deepslate')
    // (2)
    UNIFICATION_CRAFTING_COMPONENT = event.createMaterialEntry("material_entry_component", 'plate:infinity')
        .addMaterialEntry(GTValues.LV, 'block:infinity')
        .addMaterialEntry(GTValues.MV, 'ingot', 'gtceu:infinity')
        .addMaterialEntry(GTValues.HV, 'dust:infinity')
        .addMaterialEntry(GTValues.EV, 'round:infinity')
        .addMaterialEntry(GTValues.IV, 'foil:infinity')
        .addMaterialEntry(GTValues.LuV, 'long_rod', 'gtceu:infinity')
        .addMaterialEntry(GTValues.ZPM, 'rod:infinity')
        .addMaterialEntry(GTValues.UV, 'bolt:infinity')
    // (3)
})
```

1. Creates a new crafting component with item stack entries.
2. Creates a new crafting component with item tag entries. note the lack of `#` at the beginning of the tag!
3. Creates a new crafting component with MaterialEntry entries.

## Retrieving existing crafting components

All `remove*`, `set*`, and `setFallback*` methods use a Crafting Component as its first argument, you can supply that argument with just a string matching the id of the crafting component

```js title="modify.js"

GTCEuStartupEvents.craftingComponents(event => {
    event.removeTier('robot_arm', GTValues.EV) // (1)

    event.removeTiers('pump', GTValues.EV, GTValues.IV, GTValues.LuV) // (2)
})
```

1. Finds the crafting component with id `robot_arm` and removes the entry for `EV` tier
2. Finds the crafting component with id `pump` and removes the entry for `EV, IV & LuV` tiers

### Builtin Crafting Components

- `CIRCUIT 'circuit'`
- `BETTER_CIRCUIT 'better_circuit'`
- `WIRE_ELECTRIC 'wire_single'`
- `WIRE_QUAD 'wire_quad'`
- `WIRE_OCT 'wire_oct'`
- `WIRE_HEX 'wire_hex'`
- `CABLE 'cable_single'`
- `CABLE_DOUBLE 'cable_double'`
- `CABLE_QUAD 'cable_quad'`
- `CABLE_OCT 'cable_oct'`
- `CABLE_HEX 'cable_hex'`
- `CABLE_TIER_UP 'cable_tier_up_single'`
- `CABLE_TIER_UP_DOUBLE 'cable_tier_up_double'`
- `CABLE_TIER_UP_QUAD 'cable_tier_up_quad'`
- `CABLE_TIER_UP_OCT 'cable_tier_up_oct'`
- `CABLE_TIER_UP_HEX 'cable_tier_up_hex'`
- `CASING 'casing'`
- `HULL 'hull'`
- `PIPE_NORMAL 'normal_pipe'`
- `PIPE_LARGE 'large_pipe'`
- `PIPE_NONUPLE 'nonuple_pipe'`
- `GLASS 'glass'`
- `PLATE 'plate'`
- `HULL_PLATE 'hull_plate'`
- `ROTOR 'rotor'`
- `GRINDER 'grinder'`
- `SAWBLADE 'sawblade'`
- `MOTOR 'motor'`
- `PUMP 'pump'`
- `PISTON 'piston'`
- `EMITTER 'emitter'`
- `SENSOR 'sensor'`
- `CONVEYOR 'conveyor'`
- `ROBOT_ARM 'robot_arm'`
- `FIELD_GENERATOR 'field_generator'`
- `COIL_HEATING 'coil_heating'`
- `COIL_HEATING_DOUBLE 'coil_heating_double'`
- `COIL_ELECTRIC 'coil_electric'`
- `ROD_MAGNETIC 'rod_magnetic'`
- `ROD_DISTILLATION 'rod_distillation'`
- `ROD_ELECTROMAGNETIC 'rod_electromagnetic'`
- `ROD_RADIOACTIVE 'rod_radioactive'`
- `PIPE_REACTOR 'pipe_reactor'`
- `POWER_COMPONENT 'power_component'`
- `VOLTAGE_COIL 'voltage_coil'`
- `SPRING 'spring'`
- `CRATE 'crate'`
- `DRUM 'drum'`
- `FRAME 'frame'`
- `SMALL_SPRING_TRANSFORMER 'small_spring_transformer'`
- `SPRING_TRANSFORMER 'spring_transformer'`