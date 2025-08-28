---
title: Tool Creation
---

You can make tools out of materials you create by calling toolStats inside your material's code. 

When working with tools you will need to load these classes at the top of your file.
```js
const $PropertyKey = Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey');
const $ToolProperty = Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty');
```

- `.toolStats(float harvestSpeed, float attackDamage, int durability, int harvestLevel, GTToolType[] types)`
    1. harvestSpeed is how fast the tool actually breaks blocks in world.
-      Takes a decimal number eg.(5.6).
    2. attackDamage is the amount of damage per hit you deal to mobs/players.
-      Also takes a decimal number.
    3. durability is how long it takes of use to breaks.
-      This applies to the durability towards both crafting use and normal use.
       Takes a whole number up to 2.147 billion eg.(700).
       Going up to max integer is not recommended.
    4. harvestLevel is the tier of block it can break.
-       Can take a number between 1-6 with 1 being wood, 6 being neutronium.
    5. GtToolType is a group of tools in an object.
-       Must pass these into the [] brackets.

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
-  Disable crafting tools being made from this Material.
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
        .build())
```

You can also add more tool types to a GT material that already has a tool property. You do, however, have to remove the current tool property as it is immutable (not changeable).
```js title="tool_replacement.js"
GTCEuStartupEvents.materialModification(event => {
    if (GTMaterials.Neutronium.hasProperty($PropertyKey.TOOL)) {
        GTMaterials.Neutronium.removeProperty($PropertyKey.TOOL);
    }
    GTMaterials.Neutronium.setProperty($PropertyKey.TOOL, new $ToolProperty.Builder.of(180, 5.9, 2147483647, 6,
       [GTToolType.SWORD,GTToolType.DRILL_LV,GTToolType.DRILL_MV,GTToolType.DRILL_HV,GTToolType.DRILL_EV,
        GTToolType.DRILL_IV,GTToolType.PICKAXE, GTToolType.SHOVEL, GTToolType.AXE, GTToolType.HOE, 
        GTToolType.WRENCH, GTToolType.HARD_HAMMER, GTToolType.SAW, GTToolType.FILE, GTToolType.SCREWDRIVER, 
        GTToolType.WIRE_CUTTER, GTToolType.KNIFE, GTToolType.SOFT_MALLET]).build());
});
```
