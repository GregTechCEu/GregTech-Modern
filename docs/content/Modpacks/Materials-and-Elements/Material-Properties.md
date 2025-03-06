---
title: Material Properties
---


# Material Properties (WIP)

## Blast Furnace Properties

### `BlastProperty.blastTemp()`
Sets the Blast Furnace Temperature of the material. If the temperature is below 1000K recipes will be generated in the Primitive Blast Furnace. If above 1750K recipes for the Hot Ingot will be created along with the Vacuum Freezer Recipe to cool the ingot. Example: `.blastTemp(2750)`

### `BlastProperty.gasTier()`
Sets the Gas Tier which determins what GAS EBF recipes will be generated. Example: `.gasTier(LOW)`

### `BlastProperty.durationOverride()`
Overrides the EBF's default behaviour for recipe durations.

### `BlastProperty.eutOverride()`
Overrides the EBF's default behaviour for EU/t.


## `DustProperty.dust()`
Used for creating a dust material. The haverst level and burn time can be specified in the brackets. Example: `.dust(2, 4000)`


## `FluidPipeProperty.fluidPipeProperties()`
Creates a fluid pipe out of the material it is added to. The possible values are: Max Fluid Temperature, Throughput, Gas Proof, Acid Proof, Cyro Proof, Plasma Proof,
Channels. Example: `.fluidPipeProperties(9620, 850, false, false, false, false, 1)`


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
!!! failure "Not yet documented"