---
title: Material Properties
---


# Material Properties
The full list of material properties and their related functions is too expansive to be fully detailed here! All properties can be found [here](https://github.com/GregTechCEu/GregTech-Modern/tree/1.20.1/src/main/java/com/gregtechceu/gtceu/api/data/chemical/material/properties) and are fairly well documented

## Blast Furnace Properties

### `BlastProperty#blastTemperature`
The temperature of the blast furnace recipe for the material. If the temperature is below 1000K, recipes will be generated in the Primitive Blast Furnace. If above 1750K, recipes for the Hot Ingot will be created along with the Vacuum Freezer Recipe to cool the ingot. 

??? example
    ```java title="MaterialPropertyExamples.java"
    // ...
    MyMaterial = new Material.Builder(ExampleMod.id("my_material"))
        .blast(1700 // (1), BlastProperty.GasTier.LOW)

    MyMaterial.getBlastTemperature(); // (2)
    ```

    1. Sets the blast temperature to 1700K
    2. Returns 1700K (as a long)

### `BlastProperty#gasTier`
The gas tier which determines what gas will be used for EBF recipes. 

For reference,

1. `GasTier.LOW` is 1000mb of Nitrogen
2. `GasTier.MID` is 100mb of Helium
3. `GasTier.HIGH` is 50mb of Argon
4. `GasTier.HIGHER` is 25mb of Neon
5. `GasTier.HIGHEST` is 10mb of Krypton

??? example
    ```java title="ModMaterials.java"
    // ...
    MyMaterial = new Material.Builder(ExampleMod.id("my_material"))
        .blast(1700, BlastProperty.GasTier.LOW // (1))
    ```

    1. Sets Nitrogen as the gas for EBF recipes

### `BlastProperty#durationOverride()`
Overrides the EBF's default behaviour for recipe durations. By default, this is -1 which makes it follow the equation `material.getAverageMass() * blastTemperature / 50`

### `BlastProperty#eutOverride`
Overrides the EBF's default behaviour for recipe EU/t. By default, this is -1 which makes it follow the default EU/t of 120EU/t

### `BlastProperty#vacuumDurationOverride`
Overrides the Vacuum Freezer's default behaviour for recipe durations. By default, this is -1 which makes it follow the equation `material.getMass() * 3`

### `BlastProperty#vacuumEUtOverride`
Overrides the Vacuum Freezer's default behaviour for recipe EU/t. By default, this is -1 which makes it follow the default EU/t of 120EU/t

## `DustProperty#dust`
Used for creating a dust material. The harvest level and burn time can be specified in the brackets. Example: `.dust(2, 4000)`

## `FluidPipeProperty#fluidPipeProperties`
Creates a fluid pipe out of the material it is added to. The possible values are: Max Fluid Temperature, Throughput, Gas Proof, Acid Proof, Cyro Proof, Plasma Proof, Channels. 

??? example
    ```java title="ModMaterials.java"
    // ...
    MyMaterial = new Material.Builder(ExampleMod.id("my_material"))
        .fluidPipeProperties(9620, 850, false, false, false, false, 1) // (1)
    ```

    1. Sets Nitrogen as the gas for EBF recipes


## Fluid Properties

### `FluidProperty.fluid()`
!!! failure "Not yet documented"

### `FluidProperty.isGas()`
!!! failure "Not yet documented"

### `FluidProperty.hasBlock()`
!!! failure "Not yet documented"


## `GemProperty.gem()`
!!! failure "Not yet documented"


## `IngotProperty.ingot()`
!!! failure "Not yet documented"

- `.smeltInto()`
- `.arcSmeltInto()`
- `.magneticMaterial()`
- `.macerateInto()`


## `OreProperty.ore()`
Used to create an ore for the material. The ore and byproduct multipliers can be specified in brackets. A currently unimplemented option to make the ore's texture emissive is also available