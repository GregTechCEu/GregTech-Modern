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

Once complete, close your builder using `.build()`. Additionally, you can call more (optional) methods in the Builder to further modify your tools.

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
Custom tools can be created using the `GTToolType` builder. Similar to ToolProperty, use `.build()` to close your builder once complete.

 The following optional methods are available for use:

- `idFormat(String name)` -> Set a custom format for you item id. By default, this is `"%s_" + name`
- `modelLocation(ResourceLocation location)` -> Specify a custom location for your item model(s). By default, this is `GTCEu.id("item/tools/" + name)`
- `toolTag(ToolItemTagType, TagKey<Item>... tags)` -> Specify item tags to be added to the tool. 
- `toolTag(TagKey<Item>... tags)` -> Specify item tags to be added to the tool. Uses `ToolItemTagType.NONE` by default.
- `harvestTag(TagKey<Block>... tags)` -> Specify blocks this tool can break.
- `defaultActions(ToolAction... abilities)` | `defaultActions(Collection<ToolAction> abilities)` | `defaultActions(Collection<ToolAction> abilities, ToolAction... extra)` -> Specify all tool actions this tool can perform. A full list of `ToolActions` and their functions can be found [here](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/common/data/item/GTToolActions.java)
- `toolClasses(GTToolType... classes)` -> Specify a custom class to you use for your tool. Useful if you wish to define custom behavior for your tool items.
- `toolClassNames(String... classes)` -> Works the same as `toolClasses`, except using a classpath.
- `toolStats(<ToolDefinitionBuilder> builder)` -> <!--TOOD-->
- `sound(SoundEntry sound)` | `sound(SoundEntry sound, boolean playSoundOnBlockBreak)` -> Sound to play on using the item. By default, `playSoundOnBlockBreak` is false.
- `electric(int tier) | tier(int tier)` -> Specify if your tool is an electric tool, and what tier of battery charge it uses.
- `constructor(ToolConstructor constructor)` -> Constructor to use for your tool. By default, this points to `GTToolType::new`.
- `materialAmount(int amount)` -> Amount of material to return while recycling.
- `symbol(char symbol)` -> Symbol used to specify tools of this type in crafting recipes. This only applies to recipes built using `VanillaRecipeHelper#addShapelessRecipe` or `VanillaRecipeHelper#addShapedRecipe`.


??? tip "Sets of tool actions"
    Some tools added by GT have a set of tool actions associated with them. You can add all of their associated actions by passing `GTToolActions.DEFAULT_<GTToolType>_ACTIONS` in your `.defaultActions()` call.

    Alternatively, you can also build your own `Set<ToolActions>` and pass those into `defaultActions()` if you wish to reuse them.

??? info "ToolItemTagTypes and using them"
    ToolItemTagType contains three entries:

    - `ToolItemTagType.CRAFTING` -> Used to specify tags used for crafting ONLY
    - `ToolItemTagType.MATCH` -> Used to specify tags NOT used for crafting
    - `ToolItemTagType.NONE` -> No specification


```java title="ModToolTypes.java"
public static final GTToolType MY_CUSTOM_TOOL = GTToolType.builder("my_custom_tool")
    .idFormat("%s_custom_tool") // (1)
    .toolTag(CustomTags.CRAFTING_SCREWDRIVERS)
    .toolTag(CustomTags.SCREWDRIVERS)
    .toolStats(b -> b.crafting().sneakBypassUse()
        .attackDamage(-1.0F).attackSpeed(3.0F).durabilityMultiplier(2.0F)
        .behaviors(new EntityDamageBehavior(3.0F, Spider.class))
        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_MV))
    .sound(GTSoundEntries.SCREWDRIVER_TOOL)
    .electric(GTValues.HV)
    .toolClasses(GTToolType.SCREWDRIVER)
    .defaultActions(GTToolActions.DEFAULT_SCREWDRIVER_ACTIONS)
    .build();
```

1. `%s` here is used to pass in the material. For example, a copper custom tool would have the ID `gtceu:copper_custom_tool` using the `idFormat` specified here

A list of every `GTToolType` and their definitions can be found [here](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/api/item/tool/GTToolType.java)

New `GTToolTypes` must be added to materials during `PostMaterialEvent`. An example is provided below:

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
