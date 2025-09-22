---
title: "Greenhouse"
---


# Greenhouse Multiblock (by Drack.ion)


## Recipe Type

=== "JavaScript"
    ```js title="greenhouse_recipe_type.js"
    GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
        event.create('greenhouse')
            .category('drack')
            .setEUIO('in')
            .setMaxIOSize(3, 4, 1, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BATH)
    })
    ```

=== "Java"
    ```java title="RecipeTypes.java"
        public final static GTRecipeType GREENHOUSE_RECIPES = register("greenhouse", MULTIBLOCK)
            .setMaxIOSize(2, 1, 1, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BATH);
    ```

## Multiblock
=== "JavaScript"
    ```js title="greenhouse_multiblock.js"
    const $RecipeLogic = Java.loadClass('com.gregtechceu.gtceu.api.machine.trait.RecipeLogic')
    const $List = Java.loadClass('java.util.List')

    GTCEuStartupEvents.registry('gtceu:machine', event => {
        event.create('greenhouse', 'multiblock')
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType('greenhouse')
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition => FactoryBlockPattern.start()
                .aisle('CCC', 'CGC', 'CGC', 'CLC', 'CCC')
                .aisle('CMC', 'G#G', 'G#G', 'LIL', 'COC')
                .aisle('CKC', 'CGC', 'CGC', 'CLC', 'CNC')
                .where('K', Predicates.controller(Predicates.blocks(definition.get())))
                .where('M', Predicates.blocks('moss_block')
                    .or(Predicates.blocks('dirt'))
                    .or(Predicates.blocks('grass_block'))
                )
                .where('G', Predicates.blocks('ae2:quartz_glass'))
                .where('I', Predicates.blocks('glowstone'))
                .where('L', Predicates.blocks(GTBlocks.CASING_GRATE.get()))
                .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                )
                .where('O', Predicates.abilities(PartAbility.MUFFLER)
                    .setExactLimit(1)
                )
                .where('N', Predicates.abilities(PartAbility.MAINTENANCE))
                .where('#', Predicates.air())
                .build()
            )
            .modelProperty(GTModelProperties.RECIPE_LOGIC_STATUS, $RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"), GTCEu.id("block/multiblock/implosion_compressor"))
                ["andThen(java.util.function.Consumer)"](b => b.addDynamicRenderer(() => GTDynamicRenders.makeGrowingPlantRender($List.of(new Vector3f(0, 1, -1)))))
            )
    })
    ```
=== "Java"
    ```java title="MultiMachines.java"
        public static final MultiblockMachineDefinition GREENHOUSE = REGISTRATE
            .multiblock("greenhouse", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(RecipeTypes.GREENHOUSE_RECIPES)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCC", "CGC", "CGC", "CLC", "CCC")
                    .aisle("CMC", "G#G", "G#G", "LIL", "COC")
                    .aisle("CKC", "CGC", "CGC", "CLC", "CNC")
                    .where('K', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('M', Predicates.blocks(Blocks.MOSS_BLOCK)
                            .or(Predicates.blocks(Blocks.DIRT))
                            .or(Predicates.blocks(Blocks.GRASS_BLOCK)))
                    .where('G', Predicates.blocks(AEBlocks.QUARTZ_GLASS.block()))
                    .where('I', Predicates.blocks(Blocks.GLOWSTONE))
                    .where('L', Predicates.blocks(GTBlocks.CASING_GRATE.get()))
                    .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                    .where('O', Predicates.abilities(PartAbility.MUFFLER)
                            .setExactLimit(1))
                    .where('N', Predicates.abilities(PartAbility.MAINTENANCE))
                    .where('#', Predicates.air())
                    .build())
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"), GTCEu.id("block/multiblock/implosion_compressor"))
                .andThen(b -> b.addDynamicRenderer(() -> GTDynamicRenders.makeGrowingPlantRender(List.of(new Vector3f(0, 1, -1))))))
            .register();
    ```



## Lang

```json title="en_us.json"
{
    "block.gtceu.greenhouse": "Greenhouse",
    "gtceu.greenhouse": "Greenhouse"
}
```


## Recipes

=== "JavaScript"
    ```js title="greenhouse_recipes.js"
    ServerEvents.recipes(event => {
    
        ////// Machine Recipe //////
    
        event.shaped(
            'gtceu:greenhouse',
            ['AWA', 'ASA', 'WAW'],
            {
                A: '#forge:circuits/mv',
                W: 'gtceu:copper_single_cable',
                S: 'gtceu:solid_machine_casing'
            }
        ).id('gtceu:shaped/greenhouse')
    
    
        ////// Greenhouse Recipes //////
    
        function Greenhouse(id, input, fluid, output, boosted) {
            if (boosted) {
                event.recipes.gtceu.greenhouse(id)
                    .circuit(2)
                    .notConsumable(InputItem.of(input))
                    .itemInputs('4x gtceu:fertilizer')
                    .inputFluids(Fluid.of('minecraft:water', fluid))
                    .itemOutputs(output)
                    .duration(320)
                    .EUt(MV)
            } else {
                event.recipes.gtceu.greenhouse(id)
                    .circuit(1)
                    .notConsumable(InputItem.of(input))
                    .inputFluids(Fluid.of('minecraft:water', fluid))
                    .itemOutputs(output)
                    .duration(640)
                    .EUt(MV)
            }
        }
        
    
        ////// Trees //////
        
        // Rubber
        Greenhouse('rubber_sapling', 'gtceu:rubber_sapling', 1000, ['32x gtceu:rubber_log', '8x gtceu:sticky_resin', '4x gtceu:rubber_sapling'], false)
        Greenhouse('rubber_sapling_boosted', 'gtceu:rubber_sapling', 1000, ['64x gtceu:rubber_log', '16x gtceu:sticky_resin', '4x gtceu:rubber_sapling'], true)
        
        // Oak
        Greenhouse('oak_sapling', 'minecraft:oak_sapling', 1000, ['64x minecraft:oak_log', '4x minecraft:oak_sapling'], false)
        Greenhouse('oak_sapling_boosted', 'minecraft:oak_sapling', 1000, ['64x minecraft:oak_log', '64x minecraft:oak_log', '4x minecraft:oak_sapling'], true)
        
        // Dark Oak
        Greenhouse('dark_oak_sapling', 'minecraft:dark_oak_sapling', 1000, ['64x minecraft:dark_oak_log', '4x minecraft:dark_oak_sapling'], false)
        Greenhouse('dark_oak_sapling_boosted', 'minecraft:dark_oak_sapling', 1000, ['64x minecraft:dark_oak_log', '64x minecraft:dark_oak_log', '4x minecraft:dark_oak_sapling'], true)
        
        // Spruce
        Greenhouse('spruce_sapling', 'minecraft:spruce_sapling', 1000, ['64x minecraft:spruce_log', '4x minecraft:spruce_sapling'], false)
        Greenhouse('spruce_sapling_boosted', 'minecraft:spruce_sapling', 1000, ['64x minecraft:spruce_log', '64x minecraft:spruce_log', '4x minecraft:spruce_sapling'], true)
        
        // Birch
        Greenhouse('birch_sapling', 'minecraft:birch_sapling', 1000, ['64x minecraft:birch_log', '4x minecraft:birch_sapling'], false)
        Greenhouse('birch_sapling_boosted', 'minecraft:birch_sapling', 1000, ['64x minecraft:birch_log', '64x minecraft:birch_log', '4x minecraft:birch_sapling'], true)
        
        // Acacia
        Greenhouse('acacia_sapling', 'minecraft:acacia_sapling', 1000, ['64x minecraft:acacia_log', '4x minecraft:acacia_sapling'], false)
        Greenhouse('acacia_sapling_boosted', 'minecraft:acacia_sapling', 1000, ['64x minecraft:acacia_log', '64x minecraft:acacia_log', '4x minecraft:acacia_sapling'], true)
        
        // Jungle
        Greenhouse('jungle_sapling', 'minecraft:jungle_sapling', 1000, ['64x minecraft:jungle_log', '4x minecraft:jungle_sapling'], false)
        Greenhouse('jungle_sapling_boosted', 'minecraft:jungle_sapling', 1000, ['64x minecraft:jungle_log', '64x minecraft:jungle_log', '4x minecraft:jungle_sapling'], true)
        
        // Azalea
        Greenhouse('azalea_sapling', 'minecraft:azalea', 1000, ['64x minecraft:oak_log', '4x minecraft:azalea'], false)
        Greenhouse('azalea_boosted', 'minecraft:azalea', 1000, ['64x minecraft:oak_log', '64x minecraft:oak_log', '4x minecraft:azalea'], true)
        
        // Flowering Azalea
        Greenhouse('flowering_azalea', 'minecraft:flowering_azalea', 1000, ['64x minecraft:oak_log', '4x minecraft:flowering_azalea'], false)
        Greenhouse('flowering_azalea_boosted', 'minecraft:flowering_azalea', 1000, ['64x minecraft:oak_log', '64x minecraft:oak_log', '4x minecraft:flowering_azalea'], true)
        
        // Mangrove
        Greenhouse('mangrove_propagule', 'minecraft:mangrove_propagule', 1000, ['64x minecraft:mangrove_log', '4x minecraft:mangrove_propagule'], false)
        Greenhouse('mangrove_propagule_boosted', 'minecraft:mangrove_propagule', 1000, ['64x minecraft:mangrove_log', '64x minecraft:mangrove_log', '4x minecraft:mangrove_propagule'], true)
        
        ////// Crops //////
        
        // Sugarcane
        Greenhouse('sugar_cane', 'minecraft:sugar_cane', 1000, '24x minecraft:sugar_cane', false)
        Greenhouse('sugar_cane_boosted', 'minecraft:sugar_cane', 1000, '48x minecraft:sugar_cane', true)
        
        // Kelp
        Greenhouse('kelp', 'minecraft:kelp', 2000, '24x minecraft:kelp', false)
        Greenhouse('kelp_boosted', 'minecraft:kelp', 2000, '48x minecraft:kelp', true)
        
        // Bamboo
        Greenhouse('bamboo', 'minecraft:bamboo', 1000, '24x minecraft:bamboo', false)
        Greenhouse('bamboo_boosted', 'minecraft:bamboo', 1000, '48x minecraft:bamboo', true)
        
        // Cactus
        Greenhouse('cactus', 'minecraft:cactus', 1000, '24x minecraft:cactus', false)
        Greenhouse('cactus_boosted', 'minecraft:cactus', 1000, '48x minecraft:cactus', true)
        
        // Wheat
        Greenhouse('wheat', 'minecraft:wheat_seeds', 1000, '24x minecraft:wheat', false)
        Greenhouse('wheat_boosted', 'minecraft:wheat_seeds', 1000, '48x minecraft:wheat', true)
        
        // Carrot
        Greenhouse('carrot', 'minecraft:carrot', 1000, '24x minecraft:carrot', false)
        Greenhouse('carrot_boosted', 'minecraft:carrot', 1000, '48x minecraft:carrot', true)
        
        // Potato
        Greenhouse('potato', 'minecraft:potato', 1000, '24x minecraft:potato', false)
        Greenhouse('potato_boosted', 'minecraft:potato', 1000, '48x minecraft:potato', true)
        
        // Beetroot
        Greenhouse('beetroot', 'minecraft:beetroot_seeds', 1000, '24x minecraft:beetroot', false)
        Greenhouse('beetroot_boosted', 'minecraft:beetroot_seeds', 1000, '48x minecraft:beetroot', true)
        
        // Mellon
        Greenhouse('melon', 'minecraft:melon_seeds', 1000, '12x minecraft:melon', false)
        Greenhouse('melon_boosted', 'minecraft:melon_seeds', 1000, '24x minecraft:melon', true)
        
        // Pumpkin
        Greenhouse('pumpkin', 'minecraft:pumpkin_seeds', 1000, '12x minecraft:pumpkin', false)
        Greenhouse('pumpkin_boosted', 'minecraft:pumpkin_seeds', 1000, '24x minecraft:pumpkin', true)
        
        // Nether Wart
        Greenhouse('nether_wart', 'minecraft:nether_wart', 1000, '12x minecraft:nether_wart', false)
        Greenhouse('nether_wart_boosted', 'minecraft:nether_wart', 1000, '24x minecraft:nether_wart', true)
        
        // Red Mushroom
        Greenhouse('red_mushroom', 'minecraft:red_mushroom', 1000, '12x minecraft:red_mushroom', false)
        Greenhouse('red_mushroom_boosted', 'minecraft:red_mushroom', 1000, '24x minecraft:red_mushroom', true)
        
        // Brown Mushroom
        Greenhouse('brown_mushroom', 'minecraft:brown_mushroom', 1000, '12x minecraft:brown_mushroom', false)
        Greenhouse('brown_mushroom_boosted', 'minecraft:brown_mushroom', 1000, '24x minecraft:brown_mushroom', true)
    })
    ```

=== "Java"
    ```java title="Recipes.java"
        private static void greenhouseHelper(Consumer<FinishedRecipe> provider, String id, Item input, ItemStack output_normal, ItemStack output_boosted) {
            greenhouseHelper(provider, id, input, List.of(output_normal), List.of(output_boosted));
    
        }
        private static void greenhouseHelper(Consumer<FinishedRecipe> provider, String id, Item input, List<ItemStack> output_normal, List<ItemStack> output_boosted) {
    
            GREENHOUSE_RECIPES.recipeBuilder(id)
                    .circuitMeta(2)
                    .notConsumable(input)
                    .inputItems(FERTILIZER.get(), 4)
                    .inputFluids(Water, 1000)
                    .outputItems(output_normal)
                    .duration(320)
                    .EUt(MV)
                    .save(provider);
            GREENHOUSE_RECIPES.recipeBuilder(id + "_boosted")
                    .circuitMeta(1)
                    .notConsumable(input)
                    .inputFluids(Water, 1000)
                    .outputItems(output_boosted)
                    .duration(320)
                    .EUt(MV)
                    .save(provider);
        }
    
        private static void loadGreenhouseRecipes(Consumer<FinishedRecipe> provider){
            VanillaRecipeHelper.addShapedRecipe(provider, true, GTCEu.id("greenhouse"),
                    GTMultiMachines.GREENHOUSE.asStack(),
                    "AWA",
                    "ASA",
                    "WAW",
                    "A", CustomTags.MV_CIRCUITS,
                    "W", ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.Copper),
                    "S", GTBlocks.CASING_STEEL_SOLID.asItem());
    
            // Rubber
            greenhouseHelper(provider, "rubber_sapling", GTBlocks.RUBBER_SAPLING.asItem(),
                    List.of(new ItemStack(GTBlocks.RUBBER_LOG.get(), 64), new ItemStack(GTItems.STICKY_RESIN.get(), 8), new ItemStack(GTBlocks.RUBBER_SAPLING.asItem(), 4)),
                    List.of(new ItemStack(GTBlocks.RUBBER_LOG.get(), 64), new ItemStack(GTItems.STICKY_RESIN.get(), 16), new ItemStack(GTBlocks.RUBBER_SAPLING.asItem(), 4)));
    
            // Oak
            greenhouseHelper(provider, "oak_sapling", Blocks.OAK_SAPLING.asItem(),
                    List.of(new ItemStack(Blocks.OAK_LOG, 64), new ItemStack(Blocks.OAK_SAPLING.asItem(), 4)),
                    List.of(new ItemStack(Blocks.OAK_LOG, 64), new ItemStack(Blocks.OAK_LOG, 64), new ItemStack(Blocks.OAK_SAPLING.asItem(), 4)));
    
            // Dark Oak
            greenhouseHelper(provider, "dark_oak_sapling", Blocks.DARK_OAK_SAPLING.asItem(),
                    List.of(new ItemStack(Blocks.DARK_OAK_LOG, 64), new ItemStack(Blocks.DARK_OAK_SAPLING.asItem(), 4)),
                    List.of(new ItemStack(Blocks.DARK_OAK_LOG, 64), new ItemStack(Blocks.DARK_OAK_LOG, 64), new ItemStack(Blocks.DARK_OAK_SAPLING.asItem(), 4)));
    
            // Spruce
            greenhouseHelper(provider, "spruce_sapling", Blocks.SPRUCE_SAPLING.asItem(),
                    List.of(new ItemStack(Blocks.SPRUCE_LOG, 64), new ItemStack(Blocks.SPRUCE_SAPLING.asItem(), 4)),
                    List.of(new ItemStack(Blocks.SPRUCE_LOG, 64), new ItemStack(Blocks.SPRUCE_LOG, 64), new ItemStack(Blocks.SPRUCE_SAPLING.asItem(), 4)));
    
            // Birch
            greenhouseHelper(provider, "birch_sapling", Blocks.BIRCH_SAPLING.asItem(),
                    List.of(new ItemStack(Blocks.BIRCH_LOG, 64), new ItemStack(Blocks.BIRCH_SAPLING.asItem(), 4)),
                    List.of(new ItemStack(Blocks.BIRCH_LOG, 64), new ItemStack(Blocks.BIRCH_LOG, 64), new ItemStack(Blocks.BIRCH_SAPLING.asItem(), 4)));
    
            // Acacia
            greenhouseHelper(provider, "acacia_sapling", Blocks.ACACIA_SAPLING.asItem(),
                    List.of(new ItemStack(Blocks.ACACIA_LOG, 64), new ItemStack(Blocks.ACACIA_SAPLING.asItem(), 4)),
                    List.of(new ItemStack(Blocks.ACACIA_LOG, 64), new ItemStack(Blocks.ACACIA_LOG, 64), new ItemStack(Blocks.ACACIA_SAPLING.asItem(), 4)));
    
            // Jungle
            greenhouseHelper(provider, "jungle_sapling", Blocks.JUNGLE_SAPLING.asItem(),
                    List.of(new ItemStack(Blocks.JUNGLE_LOG, 64), new ItemStack(Blocks.JUNGLE_SAPLING.asItem(), 4)),
                    List.of(new ItemStack(Blocks.JUNGLE_LOG, 64), new ItemStack(Blocks.JUNGLE_LOG, 64), new ItemStack(Blocks.JUNGLE_SAPLING.asItem(), 4)));
    
    
            // Azalea
            greenhouseHelper(provider, "azalea_sapling", Blocks.AZALEA.asItem(),
                    List.of(new ItemStack(Blocks.OAK_LOG, 64), new ItemStack(Blocks.AZALEA.asItem(), 4)),
                    List.of(new ItemStack(Blocks.OAK_LOG, 64), new ItemStack(Blocks.OAK_LOG, 64), new ItemStack(Blocks.AZALEA.asItem(), 4)));
    
    
            // Flowering Azalea
            greenhouseHelper(provider, "flowering_azalea", Blocks.FLOWERING_AZALEA.asItem(),
                    List.of(new ItemStack(Blocks.OAK_LOG, 64), new ItemStack(Blocks.FLOWERING_AZALEA.asItem(), 4)),
                    List.of(new ItemStack(Blocks.OAK_LOG, 64), new ItemStack(Blocks.OAK_LOG, 64), new ItemStack(Blocks.AZALEA.asItem(), 4)));
    
            // Mangrove
            greenhouseHelper(provider, "mangrove_propagule", Blocks.MANGROVE_PROPAGULE.asItem(),
                    List.of(new ItemStack(Blocks.MANGROVE_LOG, 64), new ItemStack(Blocks.MANGROVE_PROPAGULE.asItem(), 4)),
                    List.of(new ItemStack(Blocks.MANGROVE_LOG, 64), new ItemStack(Blocks.MANGROVE_LOG, 64), new ItemStack(Blocks.MANGROVE_PROPAGULE.asItem(), 4)));
    
    
            ////// Crops //////
    
            // Sugarcane
            greenhouseHelper(provider, "sugar_cane", Items.SUGAR_CANE, new ItemStack(Items.SUGAR_CANE, 24), new ItemStack(Items.SUGAR_CANE, 48));
    
            // Kelp
            greenhouseHelper(provider, "kelp", Items.KELP, new ItemStack(Items.KELP, 24), new ItemStack(Items.KELP, 48));
    
            // Bamboo
            greenhouseHelper(provider, "bamboo", Items.BAMBOO, new ItemStack(Items.BAMBOO, 24), new ItemStack(Items.BAMBOO, 48));
    
            // Cactus
            greenhouseHelper(provider, "cactus", Items.CACTUS, new ItemStack(Items.CACTUS, 24), new ItemStack(Items.CACTUS, 48));
    
            // Wheat
            greenhouseHelper(provider, "wheat", Items.WHEAT_SEEDS, new ItemStack(Items.WHEAT, 24), new ItemStack(Items.WHEAT, 48));
    
            // Carrot
            greenhouseHelper(provider, "carrot", Items.CARROT, new ItemStack(Items.CARROT, 24), new ItemStack(Items.CARROT, 48));
    
            // Potato
            greenhouseHelper(provider, "potato", Items.POTATO, new ItemStack(Items.POTATO, 24), new ItemStack(Items.POTATO, 48));
    
            // Beetroot
            greenhouseHelper(provider, "beetroot", Items.BEETROOT_SEEDS, new ItemStack(Items.BEETROOT, 24), new ItemStack(Items.BEETROOT, 48));
    
            // Mellon
            greenhouseHelper(provider, "melon", Items.MELON_SEEDS, new ItemStack(Items.MELON, 12), new ItemStack(Items.MELON, 24));
    
            // Pumpkin
            greenhouseHelper(provider, "pumpkin", Items.PUMPKIN_SEEDS, new ItemStack(Items.PUMPKIN, 12), new ItemStack(Items.PUMPKIN, 24));
    
            // Nether Wart
            greenhouseHelper(provider, "nether_wart", Items.NETHER_WART, new ItemStack(Items.NETHER_WART, 12), new ItemStack(Items.NETHER_WART, 24));
    
            // Red Mushroom
            greenhouseHelper(provider, "red_mushroom", Items.RED_MUSHROOM, new ItemStack(Items.RED_MUSHROOM, 12), new ItemStack(Items.RED_MUSHROOM, 24));
    
            // Brown Mushroom
            greenhouseHelper(provider, "brown_mushroom", Items.BROWN_MUSHROOM, new ItemStack(Items.BROWN_MUSHROOM, 12), new ItemStack(Items.BROWN_MUSHROOM, 24))
        }
    ```