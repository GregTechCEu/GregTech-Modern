---
title: Tool Creation
---

Tools can be made out of materials you create by calling toolStats inside the material's code.

When working with tools you will need to load these classes at the top of your file.
```js
const $PropertyKey = Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey');
const $ToolProperty = Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty');
```
toolStats has the following arguments:

`.toolStats(float harvestSpeed, float attackDamage, int durability, int harvestLevel, GTToolType[] types)`

- `harvestSpeed` is how fast the tool actually breaks blocks in world.
  - Takes a decimal number eg.(5.6).
- `attackDamage` is the amount of damage per hit you deal to mobs/players. 
  - Also takes a decimal number.
- `durability` is the number of times the tool can be used before it breaks. 
  - Takes a positive whole number up to 2.147 billion (e.g 700).
    This applies to both crafting use and in-world use.
    Crafting generally consumes 2 points of durability per use.
- `harvestLevel` is the tier of block it can break.
  - Can take a number between 1-6 with 1 being wood, 6 being neutronium.
- `GtToolType` is a group of tools in an object. 
  - Must pass these as an array, using the [] notation.
    This argument can be left out if you want it to apply to all tool types.

An example of this being actually used is included below
```js title="example_tool_material.js"
GTCEuStartupEvents.registry('gtceu:material', event => {
    event.create('aluminfrost')
        .ingot()
        .color(0xadd8e6).secondaryColor(0xc0c0c0).iconSet(GTMaterialIconSet.DULL)
        .toolStats(new ToolProperty(12, 7, 3072, 6,
            [
                GTToolType.DRILL_LV,
                GTToolType.MINING_HAMMER
            ]
        ))
});
```
You can also add further arguments onto your tools such as:
- `.unbreakable()`
  - Makes electric tools bypass durability effectively making them never break.
- `.magnetic()`
  - Makes mined blocks and mob drops teleport to player inventory.
- `attackSpeed(float)`
  - Set the attack speed of a tool made from this Material (animation time).
    Takes a decimal number.
- `ignoreCraftingTools()`
  - Disable crafting tools being made from this Material.
- `addEnchantmentForTools(enchantment, level)`
  - Enchantment is the default enchantment applied on tool creation.
    Level is the level of said enchantment.
- `enchantability(int enchantability)`
  - Set the base enchantability of a tool made from this Material.
    Iron is 14, Diamond is 10, Stone is 5.
    Takes a whole number.

Here is an example of using them in your material:
```js title="example_tool_material.js"
GTCEuStartupEvents.registry('gtceu:material', event => {
    event.create('aluminfrost')
        .ingot()
        .color(0xadd8e6).secondaryColor(0xc0c0c0).iconSet(GTMaterialIconSet.DULL)
        .toolStats($ToolProperty.Builder.of(1.8, 1.7, 700, 3,
            [GTToolType.SWORD,
            GTToolType.PICKAXE,
            GTToolType.SHOVEL,
            ])
        .unbreakable()
        .addEnchantmentForTools(silk_touch, 1)
        .build())
});
```

You can also add more tool types to a GT material that already has a tool property. You do, however, have to remove the current tool property as it is immutable (not changeable).
```js title="tool_replacement.js"
GTCEuStartupEvents.materialModification(event => {
    if (GTMaterials.Iron.hasProperty($PropertyKey.TOOL)) {
        GTMaterials.Iron.removeProperty($PropertyKey.TOOL);
    }
    GTMaterials.Neutronium.setProperty($PropertyKey.TOOL, 
        $ToolProperty.Builder.of(180, 5.9, 2147483647, 6,
           [
              GTToolType.SOFT_MALLET,
              GTToolType.DRILL_LV
           ]
       ).build());
});
```
Here is a list of all the GtToolTypes
- SWORD,
- PICKAXE,
- SHOVEL,
- AXE,
- HOE,
- MINING_HAMMER,
- SPADE,
- SAW,
- HARD_HAMMER,
- SOFT_MALLET,
- WRENCH,
- FILE,
- CROWBAR,
- SCREWDRIVER,
- MORTAR,
- WIRE_CUTTER,
- SCYTHE,
- KNIFE,
- BUTCHERY_KNIFE,
- PLUNGER,
- DRILL_LV,
- DRILL_MV,
- DRILL_HV,
- DRILL_EV,
- DRILL_IV,
- CHAINSAW_LV,
- WRENCH_LV,
- WRENCH_HV,
- WRENCH_IV,
- BUZZSAW,
- SCREWDRIVER_LV,
- WIRE_CUTTER_LV,
- WIRE_CUTTER_HV,
- WIRE_CUTTER_IV,
