---
title: Material Flags
---


# Material Flags

Using material flags, you can specify several properties of each material, which
can influence how the material behaves, as well as which items are generated for it.

=== "Javascript"
    ```js
    GTCEuStartupEvents.registry('gtceu:material', event => {
        event.create('my_material')
            // ...
            .flags(GTMaterialFlags.FLAMMABLE)
    })
    ```
=== "Java"
    ```java title="ModMaterials.java"
    public static final Material MyMaterial = new Material.Builder(ExampleMod.id("my_material"))
        // ...
        .flags(GTMaterialFlags.FLAMMABLE)
    ```
A full list of flags can be found in the [MaterialFlags](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/api/data/chemical/material/info/MaterialFlags.java) class

Note that some flags require other flags or properties to be enabled in order to work.

# Generic Flags

- `NO_UNIFICATION`
  - Description: Disables all automatic recipe generation for this material. This flag is deprecated, please use `DISABLE_MATERIAL_RECIPES` instead.

- `DISABLE_MATERIAL_RECIPES`
  - Description: Disables all automatic recipe generation for this material. This replaces `NO_UNIFICATION`.

- `DECOMPOSITION_BY_ELECTROLYZING`
    - Description: Enables electrolyzer decomposition recipe generation. Requires `.components(...)` to be set.

- `DECOMPOSITION_BY_CENTRIFUGING`
    - Description: Enables centrifuge decomposition recipe generation. Requires `.components(...)` to be set.

- `DISABLE_DECOMPOSITION`
    - Description: Disables decomposition recipe generation for this material.

- `EXPLOSIVE`
    - Description: Any material with this flag wont have implosion compression recipes, and will output ash when recycled with an arc furnace recycle instead.

- `FLAMMABLE`
    - Description: Prevents the items for this material from being smelted. An EBF recipe/furnace recipe won't be generated. Also disables implosion compressor recipes, similar to `EXPLOSIVE`.

- `STICKY`
    - Description: Changes the viscosity of the placed fluid. Only the oils and creosote have a placeable state by default.

- `PHOSPHORESCENT`
    - Description: Makes liquids have a luminosity of 15, no matter the fluid state (liquid, gas, plasma). Otherwise they default to 10 for specifically liquid state.

# Dust Flags

- `GENERATE_PLATE` 
     - Description: Generates a plate and double plate for this material.
     - Required Flags: `GENERATE_PLATE`.
     - Required Properties: `PropertyKey.DUST`.

- `GENERATE_DENSE` 
     - Description: Generates a dense plate for this material.
     - Required Flags: `GENERATE_PLATE`.
     - Required Properties: `PropertyKey.DUST`.

- `GENERATE_ROD`
    - Description: Generates a rod for this material.
    - Required Properties: `PropertyKey.DUST`.

- `GENERATE_BOLT_SCREW`
    - Description: Generates a bolt and screw for this material.
    - Required Flags: `GENERATE_ROD`.
    - Required Properties: `PropertyKey.DUST`.

- `GENERATE_FRAME`
    - Description: Generates a frame for this material.
    - Required Flags: `GENERATE_ROD`.
    - Required Properties: `PropertyKey.DUST`.

- `GENERATE_GEAR`
    - Description: Generates a gear for this material.
    - Required Flags: `GENERATE_PLATE`, `GENERATE_ROD`.
    - Required Properties: `PropertyKey.DUST`.

- `GENERATE_LONG_ROD`
    - Description: Generates a long rod for this material.
    - Required Flags: `GENERATE_ROD`.
    - Required Properties: `PropertyKey.DUST`.

- `FORCE_GENERATE_BLOCK`
    - Description: Forces a block to be generated for the material. Useful if it doesn't have an ingot or gem item.
    - Required Properties: `PropertyKey.DUST`.

- `EXCLUDE_BLOCK_CRAFTING_RECIPES`
    - Description: Disables shapeless crafting recipes for dust to block and vice versa, as well as extruding and alloy smelting. recipes via `SHAPE_EXTRUDING`/`MOLD_BLOCK`.
    - Required Properties: `PropertyKey.DUST`.

- `EXCLUDE_PLATE_COMPRESSOR_RECIPE`
    - Description: Disable the dust to plate recipe in a compressor.
    - Required Flags: `GENERATE_PLATE`.
    - Required Properties: `PropertyKey.DUST`.

- `EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES`
    - Description: Disables shapeless crafting recipes for dust to block and vice versa.
    - Required Properties: `PropertyKey.DUST`.

- `MORTAR_GRINDABLE`
    - Description: Enables grinding the material's ingot/gem into dust using a mortar.
    - Required Properties: `PropertyKey.DUST`.

- `NO_WORKING`
    - Description: Disable metalworking by any means, except smashing or smelting. This is used for coated materials.
    - Required Properties: `PropertyKey.DUST`.

- `NO_SMASHING`
    - Description: Disable regular metalworking techniques, since the material cannot be bent.
    - Required Properties: `PropertyKey.DUST`.

- `NO_SMELTING`
    - Description: Disables smelting recipes for the material.
    - Required Properties: `PropertyKey.DUST`.

- `NO_ORE_SMELTING`
    - Description: Disables smelting the material from an ore.
    - Required Properties: `PropertyKey.DUST`.

- `NO_ORE_PROCESSING_TAB`
    - Description: Disables the ore processing tab from being created for the material.
    - Required Properties: `PropertyKey.ORE`.

- `BLAST_FURNACE_CALCITE_DOUBLE`
    - Description: Enables ore + calcite heating in a blast furnace for doubled output. Eg. Iron, Pyrite, Yellow Limonite.
    - Required Properties: `PropertyKey.DUST`.

- `BLAST_FURNACE_CALCITE_TRIPLE`
    - Description: Enables ore + calcite heating in a blast furnace for tripled output. Eg. Goethite, Wrought Iron
    - Required Properties: `PropertyKey.DUST`.

- `DISABLE_ALLOY_BLAST`
    - Description: Disables alloy blast recipes from being generated.
    - Required Properties: `PropertyKey.BLAST`, `PropertyKey.FLUID`.

- `DISABLE_ALLOY_PROPERTY`
    - Description: Disables all recipes related to alloy blasting
    - Required Flags: `DISABLE_ALLOY_BLAST`.
    - Required Properties: `PropertyKey.BLAST`, `PropertyKey.FLUID`.

# Fluid Flags

- `SOLDER_MATERIAL`
    - Description: Allows this material to be used in place of soldering alloy.
    - Required Properties: `PropertyKey.FLUID`.

- `SOLDER_MATERIAL_BAD`
    - Description: Not yet implemented. Supposed to set this material as a bad soldering material.
    - Required Properties: `PropertyKey.FLUID`.

- `SOLDER_MATERIAL_GOOD`
    - Description: Not yet implemented. Supposed to set this material as a good soldering material.
    - Required Properties: `PropertyKey.FLUID`.

# Ingot Flags

- `GENERATE_FOIL`
    - Description: Generates a foil for this material.
    - Required Flags: `GENERATE_PLATE`.
    - Required Properties: `PropertyKey.INGOT`.

- `GENERATE_RING`
    - Description: Generates a ring for this material.
    - Required Flags: `GENERATE_ROD`.
    - Required Properties: `PropertyKey.INGOT`.

- `GENERATE_SPRING`
    - Description: Generates a spring for this material.
    - Required Flags: `GENERATE_LONG_ROD`.
    - Required Properties: `PropertyKey.INGOT`.

- `GENERATE_SPRING_SMALL`
    - Description: Generates a small spring for this material.
    - Required Flags: `GENERATE_ROD`.
    - Required Properties: `PropertyKey.INGOT`.

- `GENERATE_SMALL_GEAR`
    - Description: Generates a small gear for this material.
    - Required Flags: `GENERATE_PLATE`, `GENERATE_ROD`.
    - Required Properties: `PropertyKey.INGOT`.

-   `GENERATE_FINE_WIRE`
    - Description: Generates all wires for this material.
    - Required Flags: `GENERATE_FOIL`.
    - Required Properties: `PropertyKey.INGOT`.

- `GENERATE_ROTOR`
    - Description: Generates a rotor for this material.
    - Required Flags: `GENERATE_BOLT_SCREW`, `GENERATE_RING, GENERATE_PLATE`.
    - Required Properties: `PropertyKey.INGOT`.

- `GENERATE_ROUND`
    - Description: Generates a round for this material.
    - Required Properties: `PropertyKey.INGOT`.

- `IS_MAGNETIC`
    - Description: Declares your material as the magnetized form of another material. When this flag is enabled, all metalworking recipes output the non-magnetic version of said material. A polarizer recipe is also generated for all associated TagPrefixes
    - Required Properties: `PropertyKey.INGOT`.

# Gem Flags

- `CRYSTALLIZABLE`
    - Description: Enables dust to gem in autoclave and other cryallization-related recipes.
    - Required Properties: `PropertyKey.GEM`.

- `GENERATE_LENS`
    - Description: Generates a lens for this material.
    - Required Flags: `GENERATE_PLATE`.
    - Required Properties: `PropertyKey.GEM`.

# Ore Flags
- `HIGH_SIFTER_OUTPUT`
    - Description: Increases chances of obtaining higher quality gems from sifting for the material. (eg. Ruby)
    - Required Properties: `PropertyKey.GEM`, `PropertyKey.ORE`.
