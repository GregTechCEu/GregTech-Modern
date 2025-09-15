---
title: Material Properties
---


# Material Properties

## Blast Furnace Properties

### `BlastProperty.blastTemp()`
- `.blastTemp()` is meant to be paired together with `.ingot()`. Will generate a EBF recipe (and an ABS recipe) based on the parameters you give it:
    1. temperature -> dictates what coil tier it will require (check the coil tooltips for their max temperature).
        If the temperature is below 1000, it will also generate a PBF recipe.
        If temperature is above 1750, a hot ingot will be generated, this requiring a Vacuum Freezer.
    2. (optional) gas tier -> can be `null` for none, `'low'` for nitrogen, `'mid'` for helium, `'high'` for argon, `'higher'` for neon or `'highest'` for krypton.
    3. (optional) EU per tick -> the recipe voltage
    4. (optional) duration in ticks -> how long the recipe should take
!!! tip "ABS Recipe Generation"
    For an ABS recipe to actually generate from your material, you must set .componets().
 
### `BlastProperty.durationOverride(int duration)`
Overrides the EBF's default behaviour for recipe durations.

### `BlastProperty.eutOverride(int EU/t)`
Overrides the EBF's default behaviour for EU/t.

## `Fluid Pipe Property`
- `.fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof)` -> this will create an fluid pipe from this material
  1. The maximum temperature of fluid that this pipe can handle before causing damage to the pipe.
  2. The rate at which fluid can flow through this pipe.
  4. Whether this pipe can hold gases. If not, some gas will be lost as it travels through the pipe.
  5. Whether this pipe can hold Acids. If not, the Pipe may lose fluid or cause damage
  5. Whether this pipe can hold cryogenic Fluids (below 120K). If not, the pipe may lose fluid or cause damage.
  6. Whether this pipe can hold plasmas. If not, the pipe may lose fluid or cause damage.

## `Item Pipe Property`
- `.itemPipeProperties(int priority, stacksPerSecond)` -> this will create an item pipe from this material
  1. Priority of this Item Pipe, used for the standard routing mode.
  2. How many stacks of items can be moved per second (20 ticks).

## `RotorProperty`
- `.rotorStats(int power, int efficiency, float damage, int durability)` -> this will create a turbine rotor from this material
  1. Power is the EU/t and fuel consumption multiplier the turbine gets when  equipped with this rotor.
     This output varies depending on speed of turbine and rotor holder.
  2. Efficiency is how well it handles fuel.
     A smaller number will make it consume more fuel while a bigger number means it uses less fuel.
     Actual efficiency: rotorEfficiency * holder Efficiency / 100
  3. Damage is the amount of damage that happens to the player when opening the ui of a running turbine's rotor holder.
  4. Durability is how much base durability it has.
- Here are some examples of base gt rotors:
    1. Titanium Rotor: .rotorStats(130, 115, 3.0, 1600)
    2. HSS-S Rotor .rotorStats(250, 180, 7.0, 3000)

  ## `Cable Property`
- `.cableProperties(long voltage, int amperage, int LossPerBlock, boolean IsSuperconductor)`
    1. Voltage: The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
    2. Amperage: The amperage of this Cable. Should be greater than zero.
    3. Loss Per Block: The loss-per-block of this Cable. A value of zero here will still have loss as wires.
    4. Is Superconductor: Whether this Material is a Superconductor. If so, Cables will NOT be generated and the Wires will have zero cable loss, ignoring the loss parameter.

## Fluid Properties
- Add .block() into the builder of a liquid material to allow this material to be placeable.

### `Fluid Block Property`
!!! failure "Not yet implemented"
    Eventually you can place fluids in world! (Not yet though.)

## `Ingot Property`
- `.washedIn("water")`
  1. washedIn is what fluid it uses for if it has the ore prop and is making crushed->refined. For example, the sodium persulfate and mercury ore washing recipes.
- `.separatedInto("iron")`
  1. separatedInto is the list of materials that are obtained when processing purified dusts in the centrifuge.
- `.oreSmeltInto()`
  1. oreSmeltInto is what is obtained through directly smelting the ore.
- `.polarizesInto()`
  1. polarizesInto is what is obtained through polarizing the material.
- `.arcSmeltInto()`
  1. arcSmeltInto is what is obtained through arc smelting the material.
- `.maceratesInto()`
  1. maceratesInto is what is obtained through macerating the material.
- `.ingotSmeltInto()`
  1. ingotSmeltInto is what is obtained when smelting a material's ingot.

## `Ore Property`
- `.addOreByproducts()` is a "open" list of extra byproduct materials.
  1. Is the material when going crushed -> impure in macerator, or impure dust to dust in centrifuge
  2. Is the material when going crushed->refined in thermal centrifuge, or crushed to dust in macerator, or pure dust to dust in centrifuge
  3. Is the material when going  refined to dust in macerator,
  4. Is the material when going from crushed ore to purified or in chem bath(works only if you have a getWashedIn material)
  

