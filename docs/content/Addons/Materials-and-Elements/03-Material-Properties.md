---
title: Material Properties
---


# Material Properties
Properties can be applied to a material to decide how they behave. 

The full list of material properties and their related attributes is too expansive to be fully detailed here! A list of all properties can be found [here](https://github.com/GregTechCEu/GregTech-Modern/tree/1.20.1/src/main/java/com/gregtechceu/gtceu/api/data/chemical/material/properties)

## `BlastProperty`
- `.blastTemp()` is meant to be paired together with `.ingot()`. It will generate a EBF recipe (and an ABS recipe) based on the parameters you give it.
    1. `int temperature` -> Dictates the coil tier required (check the coil tooltips for their max temperature).
        If the temperature is below 1000, it will also generate a PBF recipe.
        If temperature is above 1750, a hot ingot will be generated, as well as a vacuum freezer cooling recipe.
    2. (optional) `String gasTier` -> The gas tier which determines what gas will be used for EBF recipes. For reference,

        1. `GasTier.LOW` is 1000mb of Nitrogen
        2. `GasTier.MID` is 100mb of Helium
        3. `GasTier.HIGH` is 50mb of Argon
        4. `GasTier.HIGHER` is 25mb of Neon
        5. `GasTier.HIGHEST` is 10mb of Krypton

    3. (optional) `int eutOverride` -> Overrides the EBF's default behaviour for recipe EU/t. By default, this is -1 which makes it follow the default EU/t of 120EU/t
    4. (optional) `int durationOverride` -> Overrides the EBF's default behaviour for recipe durations. By default, this is -1 which makes it follow the equation `material.getAverageMass() * blastTemperature / 50`
    
!!! tip "ABS Recipe Generation"
    For an ABS recipe to actually generate from your material, you must set `.components()`. To disable the alloy blast smelter recipes from generating while `.components` and `.blastTemp` are set, check out [DISABLE_ALLOY_BLAST](./Material-Flags.md#dust-flags)

- `.blast()` allows you to use a Builder instead for your BlastProperty. In comparison to `.blastTemp()`, it allows you to override vacuum freezer recipes as well. The builder has the following parameters:

    - `.temp()` -> Set the temperature of coil for the recipe and optionally, the gas tier for the recipe
        - `int temperature` -> Dictates the coil tier required (check the coil tooltips for their max temperature).
        If the temperature is below 1000, it will also generate a PBF recipe.
        If temperature is above 1750, a hot ingot will be generated, as well as a vacuum freezer cooling recipe.
        - (optional) `String gasTier` -> The gas tier which determines what gas will be used for EBF recipes. See above for the different gases used as GasTiers

    - `.blastStats()` -> Used to override the EBF recipes. Has the following parameters:
        - `int eutOverride` -> Overrides the EBF's default behaviour for recipe EU/t. By default, this is -1 which makes it follow the default EU/t of 120EU/t
        - (optional) `int durationOverride` -> Overrides the EBF's default behaviour for recipe durations. By default, this is -1 which makes it follow the equation `material.getAverageMass() * blastTemperature / 50`
    - `vacuumStats()` -> Used to override the vacuum freezer cooling recipe. Has the following parameters:
        - `int eutOverride` -> Overrides the Vacuum Freezer's default behaviour for recipe EU/t. By default, this is -1 which makes it follow the default EU/t of 120EU/t
        - (optional) `int durationOverride` -> Overrides the Vacuum Freezer's default behaviour for recipe durations. By default, this is -1 which makes it follow the equation `material.getMass() * 3`
    

??? example
    ```java title="MaterialPropertyExamples.java"
    // ...
    MyMaterial = new Material.Builder(ExampleMod.id("my_material"))
        .blast(b -> b.temp(1700, BlastProperty.GasTier.LOW) // (1)
            .blastStats(120, 1500) // (2)
            .vacuumStats(120, 1000)) // (3)

    MyMaterial.getBlastTemperature(); // (4)
    ```

    1. Sets the blast temperature to 1700K, and sets Nitrogen as the gas for EBF recipes
    2. Sets the EBF recipe to run at 120 EU/t for 75 seconds
    3. Sets the Vacuum Freezer recipe to run at 120 EU/t for 50 seconds
    4. Returns 1700K (as a long)

## `DustProperty#dust`
- `.dust(int harvestLevel, int burnTime)` -> Used for creating a material with a dust
      1. (optional) `int harvestLevel` -> Sets the harvest level for the material's ore and other related
      2. (optional) `int burnTime` -> Sets the material to a furnace fuel and its burn time as well

!!! warning
    Using `.dust()` throws an `IllegalArgumentException` if a `DustProperty` has already been added to this Material from another source

## `Fluid Pipe Property`
- `.fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof)` -> Creates a fluid pipe from this material
      1. `int maxTemp` -> The maximum temperature of fluid that this pipe can handle before breaking the pipe and voiding fluids.
      2. `int throughput` -> The rate at which fluid can flow through this pipe.
      3. `boolean gasProof` -> Whether this pipe can hold gases. If not, some gas will be lost as it travels through the pipe.
      4. (optional) `boolean acidProof` -> Allow this pipe to hold acids. If false, the pipe will break and void all fluids.
      5. (optional) `boolean cryoProof` -> Allow this pipe to hold cryogenic fluids (below 120K). If false, the pipe will break and void all fluids.
      6. (optional) `boolean plasmaProof` -> Allow this pipe can hold plasmas. If false, the pipe will break and void all fluids. Plasma-capable pipes do not care about temperature.

## `Item Pipe Property`
- `.itemPipeProperties(int priority, int stacksPerSecond)` -> this will create an item pipe from this material
      1. `int priority` -> Priority of this Item Pipe, used for the standard routing mode.
      2. `int stacksPerSecond` ->  How many stacks of items can be moved per second (20 ticks).

## `Rotor Property`
- `.rotorStats(int power, int efficiency, float damage, int durability)` -> this will create a turbine rotor from this material
    1. `int power` -> EU/t generated by the turbine when equipped with this rotor. This output varies depending on speed of turbine and rotor holder.
    2. `int efficiency` -> Rate of consumption of provided fuel. The more efficient the rotor, the less fuel consumed per cycle. Actual efficiency can be calculated using the formula `rotorEfficiency * holderEfficiency / 100`
    3. `float damage` ->  Damage dealt to the player when when the UI of a working turbine is opened
    4. `int durability` ->  Durability of the rotor itself

??? example Base GT rotors
    Titanium Rotor: `.rotorStats(130, 115, 3.0, 1600)` // (1)
    HSS-S Rotor: `.rotorStats(250, 180, 7.0, 3000)`

    1. 130 EU/t, 1.15x rotor efficiency, 3 damage, 1600 durability

## `Cable Property`
- .cableProperties(long voltage, int amperage, int lossPerBlock, boolean isSuperconductor)
    1. `long voltage` -> The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
    2. `int amperage` -> The amperage of this Cable. Should be greater than zero.
    3. `int loss` -> The loss-per-block of this Cable. A value of zero here will still have loss as wires.
    4. (optional) `boolean isSuperconductor` -> Whether this Material is a Superconductor. If so, cables will NOT be generated and wires will have zero cable loss.

## Fluid Properties

### `Fluid Block Property`
- Add `.block()` into the builder of a liquid material to allow this material to be placeable.

## `Ingot Property`
- `.polarizesInto(string newMaterial)`
    - `string newMaterial` -> Is what is obtained through polarizing the material.
- `.arcSmeltInto(string newMaterial)`
    - `string newMaterial` -> Is what is obtained through arc smelting the material.
- `.macerateInto(string newMaterial)`
    - `string newMaterial` -> Is what is obtained through macerating the material.
- `.ingotSmeltInto(string newMaterial)`
    - `string newMaterial` -> Is what is obtained when smelting a material's ingot.

## `Ore Property`
- `.addOreByproducts()` is an "open" list of extra byproduct materials.
      1. Is the material when going crushed -> impure in macerator, or impure dust to dust in centrifuge
      2. Is the material when going crushed->refined in thermal centrifuge, or crushed to dust in macerator, or pure dust to dust in centrifuge
      3. Is the material when going from refined to dust in macerator,
      4. Is the material when going from crushed ore to purified or in chem bath(works only if you have a getWashedIn material)
- `.washedIn(string fluid)`
    - `string fluid` Is what fluid it uses for if it has the ore prop and is making crushed->refined. For example, the sodium persulfate and mercury ore washing recipes.
- `.separatedInto(list material)`
    - `list material` Is the list of materials that are obtained when processing purified dusts in the centrifuge.
- `.oreSmeltInto(string material)`
    - `string material` Is what is obtained through directly smelting the ore.
  