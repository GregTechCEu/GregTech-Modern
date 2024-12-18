---
title: Material Flags
---


# Material Flags

Using material flags, you can specify several properties of each material, which
can influence how the material behaves, as well as which items are generated for it.

!!! example "Using material Flags"
    ```js
    GTCEuStartupEvents.registry('gtceu:material', event => {
        event.create('my_material')
            // ...
            .flags(GTMaterialFlags.FLAMMABLE)
    })
    ```


## Generate Items

- `FORCE_GENERATE_BLOCK`
- `GENERATE_BOLT_SCREW`
- `GENERATE_DENSE`
- `GENERATE_FINE_WIRE`
- `GENERATE_FOIL`
- `GENERATE_FRAME`
- `GENERATE_GEAR`
- `GENERATE_LENS`
- `GENERATE_LONG_ROD`
- `GENERATE_PLATE`
- `GENERATE_RING`
- `GENERATE_ROD`
- `GENERATE_ROTOR`
- `GENERATE_ROUND`
- `GENERATE_SMALL_GEAR`
- `GENERATE_SPRING`
- `GENERATE_SPRING_SMALL`


## Other Flags

- `BLAST_FURNACE_CALCITE_DOUBLE`
- `BLAST_FURNACE_CALCITE_TRIPLE`
- `DISABLE_ALLOY_BLAST`
- `DISABLE_ALLOY_PROPERTY`
- `CRYSTALLIZABLE`
- `DECOMPOSITION_BY_CENTRIFUGING`
- `DECOMPOSITION_BY_ELECTROLYZING`
- `DISABLE_DECOMPOSITION`
- `EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES`
- `EXCLUDE_BLOCK_CRAFTING_RECIPES`
- `EXCLUDE_PLATE_COMPRESSOR_RECIPES`
- `EXPLOSIVE`
- `FLAMMABLE`
- `HIGH_SIFTER_OUTPUT`
- `IS_MAGNETIC`
- `MORTAR_GRINDABLE`
- `NO_SMASHING`
- `NO_SMELTING`
- `NO_UNIFICATION`
- `NO_WORKING`
- `SOLDER_MATERIAL`
- `SOLDER_MATERIAL_BAD`
- `SOLDER_MATERIAL_GOOD`
- `STICKY`