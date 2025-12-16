---
title: Material Creation
---


Materials are in-game items or fluids. They can be dusts, ingots, gems, fluids and all their derivatives.
!!! note
    To add a material that is present on the periodic table, but doesn't have any in-game items/fluids, look at the [material modification page](./Modifying-Existing-Materials.md).

You can change the properties of the material by adding any combination of the following calls:

- `.ingot()` will make the material have both an ingot and dust form.
- `.dust()` will make the material have a dust form. Don't use this together with `.ingot()`.
- `.gem()` will make the material have both a gem form and a dust form. Don't use those together with `.dust()` or `.ingot()`.
- `.liquid()` will make the material have a liquid (fluid) form with liquid properties.
- `.block()` will make the material have a placeable (block) fluid form. Requires `.liquid()`.
- `.gas()` will make the material have a gas (fluid) form with gas properties.
- `.plasma()` will make the material have a plasma (fluid) form with plasma properties.
- `.polymer()` will make the material have a dust form with polymer properties.
- `.ore()` will create an ore from the material.
    - Optionally you can add any of these sets of parameters: 
        1. `boolean isEmissive` -> `true` for emissive textures
        2. `int oreMultiplier, int byproductMultiplier` -> how many crushed ores will be given from one raw ore and how many byproducts dusts will be given throughout the ore processing 
        3. `int oreMultiplier, int byproductMultiplier, boolean isEmissive` -> see previous points
- `.burnTime(int burnTime)` will turn the material into a furnace fuel.
- `.fluidBurnTime(int burnTime)` defines how long the fluid of the material will burn.
- `.components(component1, component2, ...)` describes the composition. The components are a list of elements of the following form: `'Kx material_name'`, where `K` is a positive integer.
- `.element(element)` is similar to `.components()`, but is used when the material represents an element.
- `.iconSet(set)` gives the material an icon set.
- `.color(int colorCode)` gives the material a color. The color must be provided as a hex value in the following form: `0xRRGGBB`.
- `.secondaryColor(int colorCode)` gives the material a secondary color. If this is not being called, the secondary value will default to white(0xffffff).
    - The secondary color is the overlay over the primary color on the material. This can be seen in the dust of a material, as the secondary color outline is visible. Rotors are another solid example.
- `.addDefaultEnchant(string EnchantName, int level)` gives the material a default enchant. 

!!! tip "Harvest Level & Burn Time"
    For `.ingot()`, `.dust()` and `.gem()`, optionally you can put inside the parentheses any of these sets of parameters:

    1. harvest level (e.g. `.ingot(2)` will make the material have the harvest level of iron tools) 
    2. harvest level, burn time (e.g. `ingot(2, 2000)` will make the material have the harvest level of iron tools and will burn in furnaces as fuel for 2000 ticks or 100 seconds).

!!! tip "Choosing EU/t"
    GT has some builtin constants to ease choosing the required EU/t:

    - `GTValues.V` for a full amp of power at the selected tier

    - `GTValues.VA` for a full amp, adjusted for cable loss

    - `GTValues.VH` for half an amp

    - `GTValues.VHA` for half an amp, adjusted for cable loss

    These values are arrays containing the respective EU/t values for each tier.  
    For example, you can get a full amp of EV power, adjusted for cable loss like this:

    ```js
    GTValues.VA[GTValues.EV]
    ```

??? tip "Color Pickers"
    To chose a color for your material, you can checkout the [color picker](https://www.w3schools.com/colors/colors_picker.asp).
    After you select a color with the above tool, copy the 6 digits that follow the # under the color preview.

## Creating an Ingot

=== "JavaScript"
    ```js title="ingot.js"
    GTCEuStartupEvents.registry('gtceu:material', event => {
        event.create('andesite_alloy')
            .ingot()
            .components('1x andesite', '1x iron')
            .color(0x839689).iconSet(GTMaterialIconSet.DULL)
            .flags(GTMaterialFlags.GENERATE_PLATE, GTMaterialFlags.GENERATE_GEAR, GTMaterialFlags.GENERATE_SMALL_GEAR)
    })
    ```
=== "Java"
    ```java title="Ingot.java"
    public static Material ANDESITE_ALLOY;
    public static void register() {
        ANDESITE_ALLOY = new Material.Builder(
                your_mod_id.id("andesite_alloy"))
                .ingot()
                .components("1x andesite", "1x iron")
                .color(0xFF0000).secondaryColor(0x840707).iconSet(GTMaterialIconSet.DULL)
                .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_GEAR, MaterialFlags.GENERATE_SMALL_GEAR)
                .buildAndRegister();
        }
    ```

## Creating a Dust

=== "JavaScript"
    ```js title="dust.js"
    GTCEuStartupEvents.registry('gtceu:material', event => {
        event.create('mysterious_dust')
            .dust() // The harvest level and burn time can be specified in the brackets. Example: `.dust(2, 4000)`
            .color(0x7D2DDB)
    })
    ```

=== "Java"
    ```java title="Dust.java"
    public static Material MYSTERIOUS_DUST;
    public static void register() {
        MYSTERIOUS_DUST = new Material.Builder(
            your_mod_id.id("mysterious_dust"))
            .dust() // The harvest level and burn time can be specified in the brackets. Example: `.dust(2, 4000)`
            .color(0x7D2DDB)
            .buildAndRegister();
    }
    ```

## Creating a Gem

=== "JavaScript"
    ```js title="gem.js"
    GTCEuStartupEvents.registry('gtceu:material', event => {
        event.create('purple_coal')
            .gem(2, 4000) 
            .element(GTElements.C) 
            .ore(2, 3) 
            .color(0x7D2DDB).iconSet(GTMaterialIconSet.LIGNITE)

    })
    ```

=== "Java"
    ```java title="Gem.java"
    public static Material PURPLE_COAL;
    public static void register() {
        PURPLE_COAL = new Material.Builder(
            your_mod_id.id("purple_coal"))
            .gem(2, 4000)
            .element(GTElements.C)
            .ore(2, 3) 
            .color(0x7D2DDB).iconSet(GTMaterialIconSet.LIGNITE)
            .buildAndRegister();
        }
    ```

## Creating a Fluid

=== "JavaScript"
    ```js title="fluid.js"
    // const $FluidBuilder = Java.loadClass('com.gregtechceu.gtceu.api.fluids.FluidBuilder'); Uncomment if you want to use the Fluid Builder.
    GTCEuStartupEvents.registry('gtceu:material', event => {
        event.create('mysterious_ooze')
          .fluid() // Or .liquid(Int Temperature)
          .color(0x500bbf)
    })
    ```

=== "Java"
    ```java title="Fluid.java"
    public static Material MYSTERIOUS_OOZE;
    public static void register() {
        MYSTERIOUS_OOZE = new Material.Builder(
            your_mod_id.id("mysterious_ooze"))
            .fluid() // Or .liquid(Int Temperature)
            .color(0x500bbf)
            .buildAndRegister();
        }
    ```

!!! note
    - To create a placeable fluid, you need to call a new instance of the FluidBuilder class and call .block() inside of it. The syntax for this will be the same in java and kubejs but you will need to load the FluidBuilder class for kubejs.
        - For example: `.liquid(new $FluidBuilder().block().temperature(3100))`.


!!! tip "Further Material Information"
    For more information on more fine grained material control, check out the pages below!

For a full list of the flags, check out the [Material Flags page](./Material-Flags.md).

For a full list of material properties, check out the [Material Properties page](./Material-Properties.md).

For an explanation of tools, check out the [Tool Creation page](./Tool-Creation.md).

For an explanation of custom icon sets and a list of existing ones, check out the [Icon Set page](./Material-Icon-Sets.md).
