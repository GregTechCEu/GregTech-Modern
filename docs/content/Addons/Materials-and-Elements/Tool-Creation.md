---
title: Tool Creation
---

## Creating tools for a material
Tools can be generated for any material by calling the`.toolStats(ToolProperty property)` method inside the Material builder.

`toolStats` requires a `ToolProperty` to be passed as a parameter. It is recommended to build `ToolProperty` using a builder instead of a constructor. 

By default, `ToolProperty` requires the following to be defined:

- `float harvestSpeed` -> how fast the tool breaks blocks.
- `float attackDamage` -> amount of damage dealt per hit.
- `int durability` -> the number of times the tool can be used before it breaks.
- `int harvestLevel` -> the tier of block it can break. Can take an integer between 0-6.

!!! note "Crafting vs General Durability"
    Crafting uses consume 2 points of durability, as opposed to general uses like breaking blocks, stripping logs, attack, etc.

??? info "Harvest levels for reference" 
    Tier 0 - Wood

    Tier 1 - Stone
        
    Tier 2 - Iron
        
    Tier 3 - Diamond
        
    Tier 4 - Netherite
        
    Tier 5 - Duranium (added by GregTech)
        
    Tier 6 - Neutronium (added by GregTech)

Additionally, you can call more (optional) methods in the Builder to further modify your tools.

- `addTypes(GTToolType... types)` -> Generate only the specified tools for this material. Excluding this option generates all tools for this material.
- `types(GTToolType... types)` -> Works the same as `addTypes()`.
- `attackSpeed(float attackSpeed)` -> Set the attack speed tools made from this material.
- `enchantability(int enchantability)` -> Set the base enchantability of a tool made from this material. See more [here](https://minecraft.wiki/w/Enchanting_mechanics#Enchantability).
- `durabilityMultipler(int multiplier)` -> Sets a multiplier to the base durability of tools made from this material.
- `ignoreCraftingTools()` -> Prevent crafting tools (mortar, wrench, etc.) being generated for this material. Other tools such as scythes, mining hammers, swords, etc. will still be generated.
- `unbreakable()` -> Makes tools unbreakable. Self explanatory.
- `magnetic()` -> Enables the Magnetic tool behaviors for this material's tools (relocate mob drops and mined blocks directly to inventory).

```java title="ModMaterials.java"
public static final Material MyMaterial = new Material.Builder(ExampleMod.id("aluminfrost"))
    // ...
    .toolStats(new ToolProperty.Builder.of(12.0F, 7.0F, 3072, 6) // (1)
        .addTypes(GTToolType.DRILL_LV, GTToolType.MINING_HAMMER)
        .attackSpeed(0.3F).enchantability(33)
        .enchantment(Enchantments.MOB_LOOTING, 3)
        .enchantment(Enchantments.BLOCK_FORTUNE, 2)
        .build())
    // ...
    .buildAndRegister();
```

1. Generates tools with 12 harvest speed, 7 attack damage, 3072 durability, and harvest level 6 (Neutronium)

## Modifying pre-existing tool properties

You can also change the `ToolProperty` of a GT material that already has one. However, you have to remove the pre-existing property to do so, as it is immutable.

!!! info
    All material modification must be done during `PostMaterialEvent`.

```java title="CommonProxy.java"
@SubscribeEvent
public static void modifyMaterials(PostMaterialEvent event) {
    // Check if the property exists, and remove it if found
    if (GTMaterials.TungstenCarbide.hasProperty(PropertyKey.TOOL)) {
        GTMaterials.TungstenCarbide.removeProperty(PropertyKey.TOOL);
    }

    TungstenCarbide.setProperty(PropertyKey.TOOL, (ToolProperty.Builder.of(180, 5.9, 2147483647, 6)
        .addTypes(GTToolType.SOFT_MALLET, GTToolType.DRILL_LV)
        .build()));
}
```

## Creating new tool types

A list of every `GTToolType` and their definitions can be found [here](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/api/item/tool/GTToolType.java)

New `GTToolType`s must be added to materials during `PostMaterialEvent`. An example is provided below:

```java title="MaterialModification.java"
@SubscribeEvent
public static void modifyMaterials(PostMaterialEvent event) {
    // Get all materials from the GTM registry
    for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
        ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);
        
        // Ignore all materials without a tool property
        if (toolProperty == null) {
            continue;
        }

        // Add our custom tool to all materials with a tool property
        toolProperty.addTypes(ModToolTypes.CUSTOM_TOOL_2)

        // Add our custom tool only if another tool type (LV screwdriver in this case) exists
        if (toolProperty.hasType(GTToolType.SCREWDRIVER_LV)) {
            toolProperty.addTypes(ModToolTypes.CUSTOM_TOOL_1);
        }
    }
}
```
