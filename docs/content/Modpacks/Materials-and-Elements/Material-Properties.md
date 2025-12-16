---
title: Material Properties
---


# Material Properties

Properties can be applied to a material to decide how they behave. An example of this can be seen below:

=== "Javascript"
    ```js
    GTCEuStartupEvents.registry('gtceu:material', event => {
        event.create('my_material')
            // ...
            .blastTemp(3700, "mid", GTValues.VA[GTValues.EV], 1600)
    })
    ```
=== "Java"
    ```java
    public static Material MY_MATERIAL;
    public static void register() {
       MY_MATERIAL = new Material.Builder(
            your_mod_id.id('my_material'))
            // ...
            .blastTemp(3700, "mid", GTValues.VA[GTValues.EV], 1600)
            .buildAndRegister();
        }
    ```

## `Blast Furnace Properties`
- `.blastTemp()` is meant to be paired together with `.ingot()`. Will generate a EBF recipe (and an ABS recipe) based on the parameters you give it:
    1. `int temperature` -> dictates what coil tier it will require (check the coil tooltips for their max temperature).
        If the temperature is below 1000, it will also generate a PBF recipe.
        If temperature is above 1750, a hot ingot will be generated, this requiring a Vacuum Freezer.
    2. (optional) `string gasTier` -> can be `null` for none, `'low'` for nitrogen, `'mid'` for helium, `'high'` for argon, `'higher'` for neon or `'highest'` for krypton.
    3. (optional) `long EUPerTick` -> the recipe voltage
    4. (optional) `int durationInTicks` -> how long the recipe should take
!!! tip "ABS Recipe Generation"
    For an ABS recipe to actually generate from your material, you must set `.components()`. To disable the alloy blast smelter recipes from generating while `.components` and `.blastTemp` are set, check out [DISABLE_ALLOY_BLAST](./Material-Flags.md#dust-flags)
 
- `.durationOverride(int duration)`
    - `int duration` -> Overrides the EBF's default behaviour for recipe durations.

- `.eutOverride(int EU/t)`
    - `int EU/t` -> Overrides the EBF's default behaviour for EU/t.

## `Fluid Pipe Property`
- `.fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof)` -> this will create an fluid pipe from this material
      1. `int maxtemp` -> The maximum temperature of fluid that this pipe can handle before breaking the pipe and voiding fluids.
      2. `int throughput` -> The rate at which fluid can flow through this pipe.
      3. `boolean gasProof` -> Whether this pipe can hold gases. If not, some gas will be lost as it travels through the pipe.
      4. `boolean acidProof` -> Whether this pipe can hold Acids. If not, the pipe will break and void all fluids.
      5. `boolean cryoProof` -> Whether this pipe can hold cryogenic Fluids (below 120K). If not, the pipe will break and void all fluids.
      6. `boolean plasmaProof` -> Whether this pipe can hold plasmas. If not, the pipe will break and void all fluids. Plasma capable pipes do not care about temperature.

## `Item Pipe Property`
- `.itemPipeProperties(int priority, int stacksPerSecond)` -> this will create an item pipe from this material
      1. `int priority` -> Priority of this Item Pipe, used for the standard routing mode.
      2. `int stacksPerSecond` ->  How many stacks of items can be moved per second (20 ticks).

## `Rotor Property`
- `.rotorStats(int power, int efficiency, float damage, int durability)` -> this will create a turbine rotor from this material
    1. `int power` -> Power is the EU/t and fuel consumption multiplier the turbine gets when  equipped with this rotor.
     This output varies depending on speed of turbine and rotor holder.
    2. `int efficiency` ->  Efficiency is how well it handles fuel.
     A smaller number will make it consume more fuel while a bigger number means it uses less fuel.
     Actual efficiency: rotorEfficiency * holder Efficiency / 100
    3. `float damage` ->  Damage is the amount of damage that happens to the player when opening the ui of a running turbine's rotor holder.
    4. `int durability` ->  Durability is how much base durability it has.
- Here are some examples of base gt rotors:
    1. Titanium Rotor: .rotorStats(130, 115, 3.0, 1600)
    2. HSS-S Rotor .rotorStats(250, 180, 7.0, 3000)

## `Cable Property`
- .cableProperties(long voltage, int amperage, int lossPerBlock, boolean isSuperconductor)
    1. `long voltage` -> The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
    2. `int amperage` -> The amperage of this Cable. Should be greater than zero.
    3. `int lossPerBlock` -> The loss-per-block of this Cable. A value of zero here will still have loss as wires.
    4. `boolean isSuperconductor` -> Whether this Material is a Superconductor. If so, Cables will NOT be generated and the Wires will have zero cable loss, ignoring the loss parameter.

## Fluid Properties

### `Fluid Block Property`
- Add .block() into the builder of a liquid material to allow this material to be placeable.

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
  

