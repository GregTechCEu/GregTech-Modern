---
title: Tool Creation
---

You can make tools out of materials you create by calling toolStats inside your material's code. 

- `.toolStats(float harvestSpeed, float attackDamage, int durability, int harvestLevel, GTToolType[] types)`
    1. harvestSpeed is how fast the tool actually breaks blocks in world.
       Takes a decimal number eg.(5.6)
    2. attackDamage is the amount of damage per hit you deal to mobs/players.
       Also takes a decimal number
    3. durability is how long it takes of use to breaks
       This applies to the durability towards both crafting use and normal use.
       Takes a whole number up to 2.147 billion eg.(700)
       Going up to max integer is not recommended
    4. harvestLevel is the tier of block it can break
       Can take a number between 1-6 with 1 being wood, 6 being neutronium.
    5. GtToolType is a group of tools in an object
       Must pass these into the [] brackets.

An example of this being actually used is included below
```js title="example_tool_material.js"
GTCEuStartupEvents.registry('gtceu:material', event => {
    event.create('aluminfrost')
        .ingot()
        .color(0xadd8e6)
        .secondaryColor(0xc0c0c0).iconSet(GTMaterialIconSet.DULL)
        .toolStats(new ToolProperty(12, 7, 3072, 6,
            [
                GTToolType.DRILL_LV,
                GTToolType.MINING_HAMMER
            ]
        ))
```
You can also add further arguments onto your tools such as:
- `.unbreakable()`
- Makes electric tools bypass durability effectively making them never break
- `.magnetic()`
- Makes mined blocks and mob drops teleport to player inventory
- `attackSpeed(float)`
- Set the attack speed of a tool made from this Material (animation time).
  Takes a decimal number 
- `ignoreCraftingTools()`
  Disable crafting tools being made from this Material.
- `addEnchantmentForTools(enchantment, level)`
- Enchantment is the default enchantment applied on tool creation
  Level is the level of said enchantment
- `enchantability(int enchantability)`
- Set the base enchantability of a tool made from this Material. 
  Iron is 14, Diamond is 10, Stone is 5.
  Takes a whole number

Here is an example of using them in your material:
```js title="example_tool_material.js"
GTCEuStartupEvents.registry('gtceu:material', event => {
    event.create('aluminfrost')
        .ingot()
        .color(0xadd8e6)
        .secondaryColor(0xc0c0c0).iconSet(GTMaterialIconSet.DULL)
        .toolStats($ToolProperty.Builder.of(1.8, 1.7, 700, 3,
            [GTToolType.SWORD,
                GTToolType.PICKAXE,
                GTToolType.SHOVEL,
            ])
            .unbreakable()
            .build())
```
  
