---
title: "Greenhouse"
---


# Greenhouse Multiblock (by Drack.ion)


## Recipe Type

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


## Multiblock

```js title="greenhouse_multiblock.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('greenhouse', 'multiblock')
        .rotationState(RotationState.NON_Y_AXIS)
        .recipeType('greenhouse')
        .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
        .pattern(definition => FactoryBlockPattern.start()
            .aisle('CCC', 'CGC', 'CGC', 'CLC', 'CCC')
            .aisle('CMC', 'GSG', 'G#G', 'LIL', 'COC')
            .aisle('CKC', 'CGC', 'CGC', 'CLC', 'CNC')
            .where('K', Predicates.controller(Predicates.blocks(definition.get())))
            .where('M', Predicates.blocks('moss_block')
                .or(Predicates.blocks('dirt'))
                .or(Predicates.blocks('grass_block'))
            )
            .where('G', Predicates.blocks('ae2:quartz_glass'))
            .where('S', Predicates.blocks('oak_sapling')
                .or(Predicates.blocks('dark_oak_sapling'))
                .or(Predicates.blocks('spruce_sapling'))
                .or(Predicates.blocks('birch_sapling'))
                .or(Predicates.blocks('jungle_sapling'))
                .or(Predicates.blocks('acacia_sapling'))
                .or(Predicates.blocks('azalea'))
                .or(Predicates.blocks('flowering_azalea'))
                .or(Predicates.blocks('mangrove_propagule'))
                .or(Predicates.blocks('gtceu:rubber_sapling'))
            )
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
        .workableCasingRenderer('gtceu:block/casings/solid/machine_casing_solid_steel', 'gtceu:block/multiblock/implosion_compressor', false)
})
```


## Lang

```json title="en_us.json"
{
    "block.gtceu.greenhouse": "Greenhouse",
    "gtceu.greenhouse": "Greenhouse"
}
```


## Recipes

```js title="greenhouse_recipes.js"
ServerEvents.recipes(event => {

    ////// Machine Recipe //////

    event.shaped(
        'gtceu:greenhouse',
        ['AWA', 'CSC', 'WCW'],
        {
            A: '#forge:circuits/mv',
            W: 'gtceu:copper_single_cable',
            C: '#forge:circuits/mv',
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