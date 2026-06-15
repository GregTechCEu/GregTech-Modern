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
    ```java
    public static Material MY_MATERIAL;
    public static void register() {
       MY_MATERIAL = new Material.Builder(
            your_mod_id.id('my_material'))
            // ...
            .flags(GTMaterialFlags.FLAMMABLE)
            .buildAndRegister();
        }
    ```


# Generic Flags

- `NO_UNIFICATION`
  - Description: Add to material to disable automatic recipe generation for it fully. This flag is deprecated, please use DISABLE_MATERIAL_RECIPES instead.

- `DISABLE_MATERIAL_RECIPES`
  - Description: Add to material to disable automatic recipe generation for it fully. This replaces NO_UNIFICATION.

- `DECOMPOSITION_BY_ELECTROLYZING`
    - Description: Enables electrolyzer decomposition recipe generation Requires `.components(...)` to be set.

- `DECOMPOSITION_BY_CENTRIFUGING`
    - Description: Enables centrifuge decomposition recipe generation. Requires `.components(...)` to be set.

- `DISABLE_DECOMPOSITION`
    - Description: Disables decomposition recipe generation for this material.

- `EXPLOSIVE`
    - Description: Any material with this flag wont have implosion compression recipes, and it will give ash when you arc furnace recycle it instead of that material.

- `FLAMMABLE`
    - Description: Adding this flag means you cant smelt that material and thus wont generate an ebf recipe/furnace recipe. Also disables implosion compressor recipes like `EXPLOSIVE` does.

- `STICKY`
    - Description: Add to material if it is sticky. This changes the viscosity of the placed fluid. Only the oils and creosote have a placeable state by default.

- `PHOSPHORESCENT`
    - Description: Adding this flag onto a material gives liquids a luminosity of 15, no matter the fluid state(liquid, gas, plasma). Otherwise they default to 10 for specifically liquid state.

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
    - Description: Add this to a material to force generate a block.
    - Required Properties: `PropertyKey.DUST`.

- `EXCLUDE_BLOCK_CRAFTING_RECIPES`
    - Description: This will prevent material from creating Shapeless recipes for dust to block and vice versa. Also preventing extruding and alloy smelting recipes via `SHAPE_EXTRUDING`/`MOLD_BLOCK`.
    - Required Properties: `PropertyKey.DUST`.

- `EXCLUDE_PLATE_COMPRESSOR_RECIPE`
    - Description: Add this to material if you want to disable the forge hammer plate recipe.
    - Required Flags: `GENERATE_PLATE`.
    - Required Properties: `PropertyKey.DUST`.

- `EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES`
    - Description: This will prevent material from creating Shapeless recipes for dust to block and vice versa.
    - Required Properties: `PropertyKey.DUST`.

- `MORTAR_GRINDABLE`
    - Description: Adds a mortar grinding recipe to this material.
    - Required Properties: `PropertyKey.DUST`.

- `NO_WORKING`
    - Description: Add to material if it cannot be worked by any other means, than smashing or smelting. This is used for coated materials.
    - Required Properties: `PropertyKey.DUST`.

- `NO_SMASHING`
    - Description: Add to material if it cannot be used for regular metal working techniques since it is not possible to bend it.
    - Required Properties: `PropertyKey.DUST`.

- `NO_SMELTING`
    - Description: Add to material if it's impossible to smelt it.
    - Required Properties: `PropertyKey.DUST`.

- `NO_ORE_SMELTING`
    - Description: Add to material if it's impossible to smelt it from an ore.
    - Required Properties: `PropertyKey.DUST`.

- `NO_ORE_PROCESSING_TAB`
    - Description: Add to a material to disable creating an ore processing tab.
    - Required Properties: `PropertyKey.ORE`.

- `BLAST_FURNACE_CALCITE_DOUBLE`
    - Description: Add this to your material if you want to have its ore calcite heated in a Blast Furnace for double output. Already listed are: Iron, Pyrite, PigIron, WroughtIron.
    - Required Properties: `PropertyKey.DUST`.

- `BLAST_FURNACE_CALCITE_TRIPLE`
    - Description: Add this to your material if you want to have its ore calcite heated in a Blast Furnace for triple output.
    - Required Properties: `PropertyKey.DUST`.

- `DISABLE_ALLOY_BLAST`
    - Description: Use to disable alloy blast recipes from generating.
    - Required Properties: `PropertyKey.BLAST`, `PropertyKey.FLUID`.

- `DISABLE_ALLOY_PROPERTY`
    - Description: Use to disable everything related to alloy blasting.
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
    - Description: Add this to your Material if it is a magnetized form of another Material. When this flag is on a Material, it macerates into the non-magnetic version of said Material, which is then used for certain crafting recipes.
    - Required Properties: `PropertyKey.INGOT`.

# Gem Flags

- `CRYSTALLIZABLE`
    - Description: If this material can be crystallized (turned back into gem by autoclave).
    - Required Properties: `PropertyKey.GEM`.

- `GENERATE_LENS`
    - Description: Generates a lens for this material.
    - Required Flags: `GENERATE_PLATE`.
    - Required Properties: `PropertyKey.GEM`.

# Ore Flags
- `HIGH_SIFTER_OUTPUT`
    - Description: Boosts sifter output of the gem ore for the material.
    - Required Properties: `PropertyKey.GEM`, `PropertyKey.ORE`.
