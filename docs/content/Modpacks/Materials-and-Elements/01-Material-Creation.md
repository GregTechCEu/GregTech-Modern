---
title: Material Creation
---


Materials are in-game items or fluids. They can be dusts, ingots, gems, fluids and all their derivatives.
To make a new material, write an `event.create()` call in the registering function, like in the examples.
Write inside the parentheses the name of the material inside `''` or `""`.
(**NOTE**: to add a material that is present on the periodic table, but doesn't have any in-game items/fluids, look below for how to do it)

You can change the properties of the material by adding any combination of the following calls:

- `.ingot()` will make the material have both an ingot and dust form.
- `.dust()` will make the material have a dust form. Don't use this together with `.ingot()`.
- `.gem()` will make the material have both a gem form and a dust form. Don't use those together with `.dust()` or `.ingot()`
- `.liquid()` will make the material have a liquid (fluid) form with liquid properties.
- `.gas()` will make the material have a gas (fluid) form with gas properties.
- `.plasma()` will make the material have a plasma (fluid) form with plasma properties.
- `.polymer()` will make the material have a dust form with polymer properties.
- `.burnTime(int burnTime)` will turn the material into a furnace fuel.
- `.fluidBurnTime(int burnTime)` defines how long the fluid of the material will burn.
- `.components(component1, component2, ...)` describes the composition. The components are a list of elements of the following form: `'Kx material_name'`, where `K` is a positive integer.
- `.iconSet(set)` gives the material an icon set.
- `.color(int colorCode)` gives the material a color. The color must be provided as a hex value in the following form: `0xNNNNNN`, where `N` are digits.
- `.secondaryColor(int colorCode)` gives the material a secondary color. If this is not being called, the secondary value will default to white(0xffffff).
- `.flags(flag1, flag2, ...)` can be used to select certain properties of the material, like generating gears, or disabling decomposition.
- `.element(element)` -> similar to `.components()`, but is used when the material represents an element.
- `.rotorStats(speed, damage, durability)` -> this will create a turbine rotor from this material.
- `.blastTemp()` is meant to be paired together with `.ingot()`. Will generate a EBF recipe (and an ABS recipe) based on the parameters you give it:
    1. temperature -> dictates what coil tier it will require (check the coil tooltips for their max temperature).
        If the temperature is below 1000, it will also generate a PBF recipe.
        If temperature is above 1750, a hot ingot will be generated, this requiring a Vacuum Freezer.
    2. (optional) gas tier -> can be `null` for none, `'low'` for nitrogen, `'mid'` for helium, `'high'` for argon, `'higher'` for neon or `'highest'` for krypton.
    3. (optional) EU per tick -> the recipe voltage
    4. (optional) duration in ticks -> how long the recipe should take
- `.ore()` will create an ore from the material.
  - Optionally you can add any of these sets of parameters: 
    1. is emissive -> `true` for emissive textures
    2. ore multiplier and byproduct multiplier -> how many crushed ores will be given from one raw ore and how many byproducts dusts will be given throughout the ore processing 
    3. ore multiplier, byproduct multiplier, is emissive
- `.washedIn()`
- `.separatedIn()`
- `.separatedInto()`
- `.oreSmeltInto()`
- `.polarizesInto()`
- `.arcSmeltInto()`
- `.maceratesInto()`
- `.ingotSmeltInto()`
- `.addOreByproducts()`
- `.cableProperties()` generates wires and cables(if material is not a superconductor). The following parameter sets can be given:
    1. Voltage, amperage, loss per block
    2. Voltage, amperage, loss per block, is superconductor -> for a super conductor, set loss as 0 and is super conductor as true
    3. Voltage, amperage, loss per block, is super conductor, critical temperature
- `.toolProperties()`
- `.fluidPipeProperties()`
- `.itemPipeProperties()`
- `.addDefaultEnchant()`

!!! tip "Harvest Level & Burn Time"
    For `.ingot()`, `.dust()` and `.gem()`, optionally you can put inside the parentheses any of these sets of parameters:

    1. harvest level (e.g. `.ingot(2)` will make the material have the harvest level of iron tools) 
    2. harvest level, burn time (e.g. `ingot(2, 2000)` will make the material have the harvest level of iron tools and will burn in furnaces as fuel for 2000 ticks or 100 seconds).

!!! tip "Disabling Decomposition"
    Depending on the composition, GT will autogenerate an electrolyzer or centrifuge recipe to decompose the material. You can block that by adding the disable decomposition flag. `DISABLE_DECOMPOSITION`

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
    To chose a color for your material, you can checkout https://www.w3schools.com/colors/colors_picker.asp
    After you select a color with the above tool, copy the 6 digits that follow the # under the color preview.
## Creating an Ingot

```js title="ingot.js"
GTCEuStartupEvents.registry('gtceu:material', event => {
    event.create('andesite_alloy')
        .ingot()
        .components('1x andesite', '1x iron')
        .color(0x839689).iconSet(GTMaterialIconSet.DULL)
        .flags(GTMaterialFlags.GENERATE_PLATE, GTMaterialFlags.GENERATE_GEAR, GTMaterialFlags.GENERATE_SMALL_GEAR)
})
```

## Creating a Gem

```js title="gem.js"
GTCEuStartupEvents.registry('gtceu:material', event => {
    event.create('purple_coal')
        .gem(2, 4000) 
        .element(GTElements.C) 
        .ore(2, 3) 
        .color(0x7D2DDB).iconSet(GTMaterialIconSet.LIGNITE)
})
```

## Creating a Dust

```js title="dust.js"
GTCEuStartupEvents.registry('gtceu:material', event => {
    event.create('mysterious_dust')
        .dust()
        .cableProperties(GTValues.V[GTValues.LV], 69, 0, true) // (1)
})
```

1. Voltage, Amperage, EU loss, Is Superconductor.

## Creating a Fluid

```js title="fluid.js"
GTCEuStartupEvents.registry('gtceu:material', event => {
    event.create('mysterious_ooze')
        .fluid()
        .color(0x500bbf)
        .fluidTemp(69420) 
})
```
