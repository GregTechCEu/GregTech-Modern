---
title: Tool Creation
---

Tools can be made out of materials you create by calling toolStats inside the material's code.

toolStats has the following arguments:

`.toolStats(float harvestSpeed, float attackDamage, int durability, int harvestLevel, GTToolType[] types)`

- `harvestSpeed: float` is how fast the tool actually breaks blocks in world.
- `attackDamage: float` is the amount of damage per hit you deal to mobs/players.
- `durability: int` is the number of times the tool can be used before it breaks.
    - This applies to both crafting use and in-world use.
      Crafting generally consumes 2 points of durability per use.
- `harvestLevel: int` is the tier of block it can break. 
    - Can take an integer between 0-6 with 0 being wood, 6 being neutronium.
- `types: GTToolType[]` is an array of tools in an object.
    - Must pass these as an array, using the [] notation.  
      This argument can be left out if you want your material to apply to all tool types.

An example of this being used is included below.
=== "JavaScript"
    ```js title="example_tool_material.js"
    // When working with tools in kubejs you will need to load these classes at the top of your file.
    Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey');
    Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty');
    GTCEuStartupEvents.registry('gtceu:material', event => {
        event.create('aluminfrost')
            .ingot()
            .color(0xadd8e6).secondaryColor(0xc0c0c0).iconSet(GTMaterialIconSet.DULL)
            .toolStats(new ToolProperty(12.0, 7.0, 3072, 6,
                [
                    GTToolType.DRILL_LV,
                    GTToolType.MINING_HAMMER
                ]
            ))
    });
    ```
=== "Java"
    ```java title="ExampleToolMaterial.java"
            public static Material ALUMINFROST;
            ALUMINFROST = new Material.Builder(
                your_mod_id.id("aluminfrost"))
                .color(0xadd8e6).secondaryColor(0xc0c0c0).iconSet(MaterialIconSet.DULL)
                .toolStats(new ToolProperty(12.0F, 7.0F, 3072, 6,
                        new GTToolType[] { GTToolType.DRILL_LV, GTToolType.MINING_HAMMER }))
                .buildAndRegister();
    ```
Using the ToolProperties.Builder, you can also add further arguments onto your tools.
The builder has the same arguments as the constructor, and can have chained methods such as:

- `.unbreakable()`
    - Makes electric tools bypass durability effectively making them never break.
- `.magnetic()`
    - Makes mined blocks and mob drops teleport to player inventory.
- `attackSpeed(float attackSpeed)`
    - Set the attack speed of a tool made from this Material (animation time).
- `ignoreCraftingTools()`
    - Disable crafting tools being made from this Material.

- `addEnchantmentForTools(Enchantment enchantment, int level)`
    - Enchantment is the default enchantment applied on tool creation.  
      Level is the level of said enchantment.
- `enchantability(int enchantability)`
    - Set the base enchantability of a tool made from this Material.  
      Iron is 14, Diamond is 10, Stone is 5.

Here is an example of using the builder in a material:
=== "JavaScript"
    ```js title="example_tool_material.js"
    GTCEuStartupEvents.registry('gtceu:material', event => {
        event.create('aluminfrost')
            .ingot()
            .color(0xadd8e6).secondaryColor(0xc0c0c0).iconSet(GTMaterialIconSet.DULL)
            .toolStats(
                ToolProperty.Builder.of(1.8, 1.7, 700, 3,
                    [
                        GTToolType.SWORD,
                        GTToolType.PICKAXE,
                        GTToolType.SHOVEL,
                    ]
                )
                .unbreakable()
                .addEnchantmentForTools(silk_touch, 1)
                .build()
            ) 
    });
    ```
=== "Java"
    ```java title="ExampleToolMaterial.java"
        public static Material ALUMINFROST;
        ALUMINFROST = new Material.Builder(
                your_mod_id.id("aluminfrost"))
                .ingot()
                .color(0xadd8e6).secondaryColor(0xc0c0c0).iconSet(MaterialIconSet.DULL)
                .toolStats(ToolProperty.Builder.of(1.8F, 1.7F, 700, 3)
                        .types(
                                GTToolType.SWORD,
                                GTToolType.PICKAXE,
                                GTToolType.SHOVEL)
                        .unbreakable()
                        .enchantment(SILK_TOUCH, 1)
                        .build())
                .buildAndRegister();
    ```

You can also change the tool property of a GT material that already has a tool property. You do, however, have to remove the current tool property as it is immutable.
=== "JavaScript"
    ```js title="tool_replacement.js"
    GTCEuStartupEvents.materialModification(event => {
        if (GTMaterials.TungstenCarbide.hasProperty(PropertyKey.TOOL)) {
            GTMaterials.TungstenCarbide.removeProperty(PropertyKey.TOOL);
        }
        GTMaterials.TungstenCarbide.setProperty(PropertyKey.TOOL, 
            ToolProperty.Builder.of(180, 5.9, 2147483647, 6,
            [
                GTToolType.SOFT_MALLET,
                GTToolType.DRILL_LV
            ]
        ).build());
    });
    ```
=== "Java"
    ```java title="ToolReplacement.java"
    public static void modifyMaterials() {
        if (GTMaterials.TungstenCarbide.hasProperty(PropertyKey.TOOL)) {
            GTMaterials.TungstenCarbide.removeProperty(PropertyKey.TOOL);
        }
        TungstenCarbide.setProperty(PropertyKey.TOOL,
                (ToolProperty.Builder.of(180, 5.9, 2147483647, 6, GTToolType.SOFT_MALLET, GTToolType.DRILL_LV)
                        .build()));
    }
    
    ```

Here is a list of all the GtToolTypes.

- SWORD
- PICKAXE
- SHOVEL
- AXE
- HOE
- MINING_HAMMER
- SPADE
- SAW
- HARD_HAMMER
- SOFT_MALLET
- WRENCH
- FILE
- CROWBAR
- SCREWDRIVER
- MORTAR
- WIRE_CUTTER
- SCYTHE
- KNIFE
- BUTCHERY_KNIFE
- PLUNGER
- DRILL_LV
- DRILL_MV
- DRILL_HV
- DRILL_EV
- DRILL_IV
- CHAINSAW_LV
- WRENCH_LV
- WRENCH_HV
- WRENCH_IV
- BUZZSAW
- SCREWDRIVER_LV
- WIRE_CUTTER_LV
- WIRE_CUTTER_HV
- WIRE_CUTTER_IV
