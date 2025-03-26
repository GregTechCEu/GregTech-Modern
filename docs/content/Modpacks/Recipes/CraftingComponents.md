---
title: Crafting Components
---

# Crafting Components

Crafting Components are a way to organize and simplify the various similar recipes that GregTech generates. For example, writing out the recipes for all tiers of an alloy smelter can be done iteratively rather than hand written.

Crafting Components are a map, pairing a `Voltage Tier` (the tier number) to a value. The valid value can either be a `MaterialEntry`, `ItemStack`, or a `TagPrefix<Item>`.

## Changing a single entry

With KubeJS it is possible to modify the predefined components of existing GTCEu Modern machine crafting recipes.
You can replace singular entries, or do bulk modification of components.
-1 is defined as a fallback value if no other entries exist.

```js title="modification.js"
const Map = Java.loadClass("java.util.Map")

GTCEuServerEvents.craftingComponents(event => {
    event.modifyItem(GTCraftingComponent.CIRCUIT, GTValues.MV, Item.of('minecraft:dirt')) // (1)
    event.modifyItem(GTCraftingComponent.PUMP, Map.of(
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
    event.modifyMaterialEntry(CraftingComponent.PLATE, GTValues.UEV, new UnificationEntry('plate', 'gtceu:infinity')) // (4)
    event.removeTier("sensor", 3) // (6)
})
```
1. Replaces the MV circuit tag in all GT machine crafting recipes with a single block of `minecraft:dirt`.
2. Modifies all pumps in GT machine crafting recipes by replacing the pump with a robot arm, and reinserts the FALLBACK entry.
3. Replaces the EV casing with the `#minecraft:logs` tag. note the lack of `#` at the beginning of the tag!
4. Adds a new entry to the plate component for UEV with prefix `plate` and material `gtceu:infinity`.
5. Removes the 3rd entry `(HV Tier)` of the sensor crafting component, will default to the fallback `(LV Sensor)`


## Creating new components

It's also possible to create new crafting components with KubeJS.
The crafting component is constructed with a id and a fallback value. You can add entries by chaining `.add(tier, value)` methods after your construction.

```js title="creation.js"
const Map = Java.loadClass("java.util.Map")

let ITEM_CRAFTING_COMPONENT = null
let TAG_CRAFTING_COMPONENT = null
let UNIFICATION_CRAFTING_COMPONENT = null

GTCEuServerEvents.craftingComponents(event => {
    ITEM_CRAFTING_COMPONENT = event.createItem("item_component", Item.of('minecraft:cyan_stained_glass'), Map.of(
        GTValues.LV: Item.of('minecraft:cyan_stained_glass'),
        GTValues.MV: Item.of('minecraft:cyan_stained_glass'),
        GTValues.HV: Item.of('minecraft:cyan_stained_glass'),
        GTValues.EV: Item.of('minecraft:lime_stained_glass'),
        GTValues.IV: Item.of('minecraft:lime_stained_glass'),
        GTValues.LuV: Item.of('minecraft:lime_stained_glass'),
        GTValues.ZPM: Item.of('minecraft:magenta_stained_glass'),
        GTValues.UV: Item.of('minecraft:magenta_stained_glass'),
    )) // (1)
    TAG_CRAFTING_COMPONENT = event.createTag("tag_component", 'forge:barrels/wooden', Map.of(
        GTValues.LV: 'forge:chests/wooden',
        GTValues.MV: 'forge:chests/trapped',
        GTValues.HV: 'forge:chests/ender',
        GTValues.EV: 'forge:cobblestone',
        GTValues.IV: 'forge:cobblestone/normal',
        GTValues.LuV: 'forge:cobblestone/infested',
        GTValues.ZPM: 'forge:cobblestone/mossy',
        GTValues.UV: 'forge:cobblestone/deepslate',
    )) // (2)
    UNIFICATION_CRAFTING_COMPONENT = event.createMaterialEntry("material_entry_component", new UnificationEntry('plate', 'gtceu:infinity'), Map.of(
        GTValues.LV: new MaterialEntry('block', 'gtceu:infinity'),
        GTValues.MV: new MaterialEntry('ingot', 'gtceu:infinity'),
        GTValues.HV: new MaterialEntry('dust', 'gtceu:infinity'),
        GTValues.EV: new MaterialEntry('round', 'gtceu:infinity'),
        GTValues.IV: new MaterialEntry('foil', 'gtceu:infinity'),
        GTValues.LuV: new MaterialEntry('longRod', 'gtceu:infinity'),
        GTValues.ZPM: new MaterialEntry('rod', 'gtceu:infinity'),
        GTValues.UV: new MaterialEntry('bolt', 'gtceu:infinity'),
    )) // (3)
})
```

1. Creates a new crafting component with item stack entries.
2. Creates a new crafting component with item tag entries. note the lack of `#` at the beginning of the tag!
3. Creates a new crafting component with UnificationEntry entries.

## Retrieving existing crafting components

All `remove`, `modify*`, and `setFallback*` methods use a Crafting Component as its first argument, you can supply that argument with just a string matching the id of the crafting component

```js title="modify.js"

GTCEuServerEvents.craftingComponents(event => {
    event.remove('robot_arm', GTValues.EV) // (1)
})
```

1. Finds the crafting component with id `robot_arm` and removes the entry for `EV` tier

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
- `DIAMOND 'diamond'`
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
- `STICK_MAGNETIC 'rod_magnetic'`
- `STICK_DISTILLATION 'rod_distillation'`
- `STICK_ELECTROMAGNETIC 'rod_electromagnetic'`
- `STICK_RADIOACTIVE 'rod_radioactive'`
- `PIPE_REACTOR 'pipe_reactor'`
- `POWER_COMPONENT 'power_component'`
- `VOLTAGE_COIL 'voltage_coil'`
- `SPRING 'spring'`
- `CRATE 'crate'`
- `DRUM 'drum'`
- `FRAME 'frame'`
- `SMALL_SPRING_TRANSFORMER 'small_spring_transformer'`
- `SPRING_TRANSFORMER 'spring_transformer'`