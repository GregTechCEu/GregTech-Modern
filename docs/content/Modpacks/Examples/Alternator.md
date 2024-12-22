---
title: "Alternator"
---


## Alternator Multiblock (by Drack.ion)

### Recipe Type

```js title="alternator_recipe_type.js"
GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
    event.create('basic_alternator')
        .category('multiblock')
        .setEUIO('out')
            .setMaxIOSize(1, 0, 0, 0)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
        .setSound(GTSoundEntries.ARC)
        .setMaxTooltips(6)
})
```

### Multiblock

```js title="alternator_multiblock.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('basic_alternator', 'multiblock')
        .rotationState(RotationState.NON_Y_AXIS)
        .recipeType('basic_alternator')
        .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
        .generator(true)
        .pattern(definition => FactoryBlockPattern.start()
            .aisle("CWC", "CWC", "#W#")
            .aisle("CWC", "K#E", "CWC")
            .aisle("CWC", "CWA", "#W#")
            .where('A', Predicates.controller(Predicates.blocks(definition.get())))
            .where('W', Predicates.blocks(GTBlocks.COIL_CUPRONICKEL.get()))
            .where("C", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
            .where('#', Predicates.any())
            .where('K', Predicates.abilities(PartAbility.INPUT_KINETIC).setExactLimit(1))
            .where('E', Predicates.abilities(PartAbility.OUTPUT_ENERGY).setExactLimit(1))
            .build()
        )
        .workableCasingRenderer(
            "gtceu:block/casings/solid/machine_casing_solid_steel",
            "gtceu:block/multiblock/implosion_compressor", false
        )
})
```

### Lang

```json title="en_us.json"
{
    "block.gtceu.basic_alternator": "Basic Alternator",
    "gtceu.basic_alternator": "Basic Alternator"
}
```

### Recipes

```js title="alternator_recipes.js"
ServerEvents.recipes(event => {
    function basic_alt(id, rpm, eu){
        event.recipes.gtceu.basic_alternator(id)
            .circuit(1)
            .rpm(rpm)
            .duration(2)
            .EUt(eu)
    }
    basic_alt('lv_1_amp', 32, -32)
})
```
