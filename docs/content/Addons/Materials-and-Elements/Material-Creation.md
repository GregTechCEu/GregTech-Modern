---
title: Material Creation
---

Materials are in-game items or fluids. They can be dusts, ingots, gems, fluids and all their derivatives. Materials must be defined using a builder, and later registered using `MaterialEvent`

## Material Definition
Materials are defined using `Material.Builder`. A list of most builder methods can be found below, and a full list in the [Material class](https://github.com/GregTechCEu/GregTech-Modern/blob/main/src/main/java/com/gregtechceu/gtceu/api/data/chemical/material/Material.java#L527)

An example of a Material definition is: 
```java
public static final Material MyCustomMaterial = new Material.Builder(ExampleMod.id("my_custom_material")) // (1)
    .color(0xFFFFF)
    .iconSet(ROUGH)
    .dust()
    .components(Diamond, 2, Iron, 2)
    .flags(DISABLE_DECOMPOSITION)
    .buildAndRegister();
```

1. ExampleMod.id() is simply a method that returns a ResourceLocation with your mod's namespace in the format `modid:path`

!!! note
    GregTech adds materials for all elements on the real-life periodic table by default. Some elements do not show up in game, as they don't have any items registered to them. These can be edited with addons

### Builder methods

You can change the properties of the material by adding any combination of the following builder options:

- `.ingot()` -> Makes the material have both an ingot and dust form.

- `.dust()` -> Makes the material have only a dust form, and no ingot. Don't use this together with `.ingot()`.

- `.gem()` -> Makes the material have both a gem form and a dust form, but no ingot. Don't use those together with `.dust()` or `.ingot()`

- `.liquid()` -> Makes the material have a liquid (fluid) form with liquid properties.

- `.gas()` -> Makes the material have a gas (fluid) form with gas properties.

- `.plasma()` -> Makes the material have a plasma (fluid) form with plasma properties.

- `.polymer()` -> Makes the material have a dust form with polymer properties. Additionally dusts of polymer materials are called pulps instead in-game

- `.burnTime(int burnTime)` ->  Turns the material into a furnace fuel.

- `.fluidBurnTime(int burnTime)` -> Defines how long the fluid of the material will burn in a generator

- `.components(Material component1, Material component2, ...)` -> Describes the composition. The components are a list of elements of the following form: `Material material, int amount`, where `K` is a positive integer.

- `.iconSet(MaterialIconSet iconSet)` -> Sets the MaterialIconSet. Read more about icon sets [here](Material-Icon-Sets-And-Types.md)

- `.color(int colorCode)` -> Gives the material a color. The color must be provided as a hex value in the following form: `0xNNNNNN`, where `N` are digits.

- `.secondaryColor(int colorCode)` -> Gives the material a secondary color. If this is not being called, the secondary value will default to white(0xffffff).

- `.flags(MaterialFlag flag1, MaterialFlag flag2, ...)` -> Can be used to select certain properties of the material, like generating gears, or disabling decomposition.

- `.element(Element element)` -> Similar to `.components()`, but is used when the material represents an element.

- `.rotorStats(int speed, int efficiency, float damage, int durability)` -> Creates a turbine rotor from this material.

- `.blastTemp(int temp)` -> Meant to be paired together with `.ingot()`. This will generate a EBF recipe (and an ABS recipe) based on the parameters you give it:
    1. `temp` -> dictates what coil tier it will require (check the coil tooltips for their max temperature).
        If the temperature is below 1000, it will also generate a PBF recipe.
        If temperature is above 1750, a hot ingot will be generated, this requiring a Vacuum Freezer.
    2. (optional) `BlastProperty.GasTier gasTier` -> can be `null` for none, `'LOW'` for nitrogen, `'MID'` for helium, `'HIGH'` for argon, `'HIGHER'` for neon or `'HIGHEST'` for krypton.
    3. (optional) `int euTOverride` -> override the recipe voltage and provide your own
    4. (optional) `int durationOverride` -> override the recipe duration and provide your own
- `.ore()` will create an ore from the material.
  - Optionally you can add any of these sets of parameters: 
    1. `boolean emissive` -> Whether the ore should glow
    2. `int oreMultiplier, int byproductMultiplier` -> how many crushed ores will be obtained from one raw ore, and how many byproduct dusts will be obtained from the ore processing. The two must be used together

<!--TODO-->

- `.washedIn()`

- `.separatedIn()`

- `.separatedInto()`

- `.oreSmeltInto()`

- `.polarizesInto()`

- `.arcSmeltInto()`

- `.maceratesInto()`

- `.ingotSmeltInto()`

- `.addOreByproducts()`

- `.cableProperties()` -> Generates wires and cables (if material is not a superconductor). See [cable property](Material-Properties.md#cable-property)

- `.toolProperties()`

- `.fluidPipeProperties()`

- `.itemPipeProperties()`

- `.addDefaultEnchant()`

!!! tip "Harvest Level & Burn Time"
    For `.ingot()`, `.dust()` and `.gem()`, optionally you can put inside the parentheses any of these sets of parameters:

    1. `int harvestLevel` (e.g. `.ingot(2)` will make the material have the harvest level of iron tools) 
    2. `int harvestLevel, int burnTime` (e.g. `ingot(2, 2000)` will make the material have the harvest level of iron tools and will burn in furnaces as fuel for 2000 ticks or 100 seconds).

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

    ```java
    GTValues.VA[GTValues.EV]
    ```

??? tip "Color Pickers"
    To chose a color for your material, you can checkout W3Schools' Color Picker [https://www.w3schools.com/colors/colors_picker.asp]
    After you select a color with the above tool, copy the 6 digits that follow the # under the color preview.
### Creating an Ingot

```java title="ModMaterials.java"
public static final Material AndesiteAlloy = new Material.Builder(ExampleMod.id("andesite_alloy"))
        .color(0x839689)
        .iconSet(DULL)
        .ingot()
        .components(Andesite, 1, Iron, 1)
        .flags(GENERATE_PLATE, GENERATE_PLATE, GENERATE_SMALL_GEAR)
        .buildAndRegister();
```

### Creating a Gem

```java title="ModMaterials.java"
public static final Material PurpleCoal = new Material.Builder(ExampleMod.id("purple_coal"))
        .color(0x7D2DDB)
        .iconSet(LIGNITE)
        .gem(2, 4000)
        .element(Carbon)
        .ore(2, 3)
        .buildAndRegister();
```

### Creating a Dust

```java title="ModMaterials.java"
public static final Material MysteriousDust = new Material.Builder(ExampleMod.id("mysterious_dust"))
        .dust()
        .cableProperties(GTValues.V[GTValues.LV], 69, 0, true) // (1)
        .buildAndRegister();
```

1. Voltage, Amperage, EU loss, Is Superconductor.

### Creating a Fluid

```java title="ModMaterials.java"
public static final Material MysteriousOoze = new Material.Builder(ExampleMod.id("mysterious_ooze"))
        .color(0x500bbf)
        .fluid()
        .fluidTemp(69420) 
        .buildAndRegister();
```

## Material Registration
In order for GTM to register your materials, a `MaterialRegistry` must be created during `MaterialRegistryEvent` like so:

```java title="CommonProxy.java"
@SubscribeEvent
public void registerMaterialRegistry(MaterialRegistryEvent event) {
    MATERIAL_REGISTRY = GTCEuAPI.materialManager.createRegistry(ExampleMod.MOD_ID);
}
```

Once this is done, material registration is done during `MaterialEvent`.

```java title="CommonProxy.java"
@SubscribeEvent
public void registerMaterials(MaterialEvent event) {
    ModMaterials.register();
}
```