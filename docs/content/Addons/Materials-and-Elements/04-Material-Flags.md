---
title: Material Flags
---


# Material Flags

Using material flags, you can specify specific properties for Materials, which
can influence how it behaves, as well as items or recipes generated for it.

!!! example "Using Material Flags"
    ```java title="ModMaterials.java"
    public static final Material MyMaterial = new Material.Builder(ExampleMod.id("my_material"))
        // ...
        .flags(GTMaterialFlags.FLAMMABLE)
    ```

A full list of flags can be found in the [MaterialFlags](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/api/data/chemical/material/info/MaterialFlags.java) class

Note that some flags require other flags or properties in order to work.

## Generate Items

- `GENERATE_PLATE`
- `GENERATE_DENSE`
- `GENERATE_ROD`
- `GENERATE_BOLT_SCREW`
- `GENERATE_FRAME`
- `GENERATE_GEAR`
- `GENERATE_LONG_ROD`
- `FORCE_GENERATE_BLOCK` -> Forces a block to be generated for the material. Useful if it doesn't have an ingot or gem item
- `GENERATE_FOIL`
- `GENERATE_RING`
- `GENERATE_SPRING`
- `GENERATE_SPRING_SMALL`
- `GENERATE_SMALL_GEAR`
- `GENERATE_FINE_WIRE`
- `GENERATE_ROTOR`
- `GENERATE_ROUND`
- `GENERATE_LENS`


## Other Flags

- `NO_UNIFICATION` -> Disables unification entirely
- `DISABLE_MATERIAL_RECIPES`
- `DECOMPOSITION_BY_ELECTROLYZING` -> Enables electrolyzer decomposition
- `DECOMPOSITION_BY_CENTRIFUGING` -> Enables centrifuge decomposition
- `DISABLE_DECOMPOSITION` -> Disables decomposition recipes
- `EXPLOSIVE`
- `FLAMMABLE`
- `STICKY`
- `PHOSPHORECENT`
- `EXCLUDE_BLOCK_CRAFTING_RECIPES` -> Disables shapeless crafting, extruder, and alloy smelter recipes for dust to block and vice versa
- `EXCLUDE_PLATE_COMPRESSOR_RECIPES` -> Disables recipes for compressing dusts into plates
- `EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES` -> Disables shapeless crafting recipes only for dust to block and vice versa
- `MORTAR_GRINDABLE` -> Enables grinding the material's ingot/gem into dust using a mortar
- `NO_WORKING` -> Disable metalworking by any means, except smashing or smelting. This is used for coated materials.
- `NO_SMASHING` -> Disable regular metalworking techniques, since the material cannot be bent 
- `NO_SMELTING` -> Disables smelting for the material
- `NO_ORE_SMELTING` -> Disables smelting the material from an ore
- `NO_ORE_PROCESSING_TAB` -> Disables the ore processing tab from being created for the material
- `BLAST_FURNACE_CALCITE_DOUBLE` -> Enables ore + calcite heating in a blast furnace for extra output (eg. Iron, Pyrite, Wrought Iron)
- `BLAST_FURNACE_CALCITE_TRIPLE`
- `DISABLE_ALLOY_BLAST` -> Disables alloy blast recipes from being generated
- `DISABLE_ALLOY_PROPERTY` -> Disables everything related to alloy blasting
- `SOLDER_MATERIAL` -> NYI; used for soldering
- `SOLDER_MATERIAL_BAD` -> NYI; used for soldering, requires more fluid than `SOLDER_MATERIAL` per recipe
- `SOLDER_MATERIAL_GOOD` -> NYI; used for soldering, requires less fluid than `SOLDER_MATERIAL` per recipe
- `IS_MAGNETIC` -> Enables polarizer and related magnetic recipes for the material. (eg. Magnetic Iron)
- `CRYSTALLIZABLE` -> Enables dust to gem in autoclave and other crystallization-related recipes
- `HIGH_SIFTER_OUTPUT` -> Increases chances of obtaining higher quality gems from sifting (eg. Ruby)