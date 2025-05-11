---
title: Electricity
---


# GregTech Electricity

GregTech uses an energy system different from Forge Energy (FE) called Energy Units (EU). 1 EU converts to 4 FE, but FE cannot be converted to EU without a converter.

!!! note
    The conversion rate between EU and FE can be changed in the configs.


## Amperage and Voltage

EU comes in a system of _Voltages_ and _Amperages_.

Energy is transferred in packages. Voltage describes the size of the package, while Amperage describes how many packages are sent in parallel.


### Voltage

GregTech tiers progression into voltage tiers. The voltage tier of a singleblock machine or energy hatch describes the maximum amount of EU that it can receive per amp. If that value is exceeded, the machine explodes.

Certain recipes might require certain voltage levels, thus requiring a higher tier machine.

Each tier quadruples the EU value of the previous tier.

| Short Name | Long Name               | EU value      |
| ---------- | ----------------------- | ------------- |
| ULV        | Ultra Low Voltage       | 8             |
| LV         | Low Voltage             | 32            |
| MV         | Medium Voltage          | 128           |
| HV         | High Voltage            | 512           |
| EV         | Extreme Voltage         | 2,048         |
| IV         | Insane Voltage          | 8,192         |
| LuV        | Ludicrous Voltage       | 32,768        |
| ZPM        | ZPM Voltage             | 131,072       |
| UV         | Ultimate Voltage        | 524,288       |
| UHV        | Ultra High Voltage      | 2,097,152     |
| UEV        | Ultra Excessive Voltage | 8,388,608     |
| UIV        | Ultra Immense Voltage   | 33,554,432    |
| UXV        | Ultra Extreme Voltage   | 134,217,728   |
| OpV        | Overpowered Voltage     | 536,870,912   |
| MAX        | Maximum Voltage         | 2,147,483,648 |


### Amperage

Machines and energy hatches draw amps to fill their EU buffers. When running a recipe, the machine will draw from its internal buffer.

Different machines draw different amounts of amps:

| Machine                | Notes                                                |
| ---------------------- | ---------------------------------------------------- |
| Singleblock Generators | Outputs 1A of its tier                               |
| Energy Hatches         | Draws 2A                                             |
| Transformers Step-Up   | Draws 4A lower voltage, outputs 1A higher voltage    |
| Transformers Step-Down | Draws 1A higher voltage, outputs 4A lower voltage    |
| Battery Buffers        | Draws 2A per Battery, outputs 1A per Battery.        |


## Machine Overclocking

Machines of a higher tier than that of the recipe's voltage level can overclock recipes to speed them up.
Overclocking doubles the recipe's speed, but quadruples EU/t usage. This is also called a _2/4 overclock_ or _regular overclock_.
Singleblock machines can only do regular overclocks.

A _perfect overclock_ quadruples both the recipe's speed and EU/t usage.

Some multibocks suck as the Large Chemical Reactor do perfect overclocking by default, while some have conditional perfect overclocking.

Multiblocks with 2 Energy Hatches of the same voltage tier will overclock to 1 Amp of the next voltage tier. 

For example, an Electric Blast Furnace with 2 LV Energy Hatches (4A of LV = 4 \* 32 = 128 EU/t) will overclock to MV (1A of MV = 1 \* 128 = 128 EU/t).


## Wires, Cables, Cable loss

In GregTech, energy is transferred through wires and cables. Cables are insulated wires, so most information about them applies to wires as well.

!!! danger
    Non-superconductor wires will electrocute you if touched while electricity is flowing through.

All cables have a max Voltage, max Amperage and a Loss/Meter/Ampere.

- Cables that receive more EU than their maximum Voltage will burn up.
- Cables that have more Amps traveling through them than their maximum Amperage will burn up.

Each energy packet (Amp) traveling through a cable will lose the specified amount of voltage per block traveled.<br>
For example: a 1x Tin Cable can transfer 32 EU/t with a Cable Loss of 1 EU/Meter. That means 1 Amp can "live" for 32 blocks before it's completely lost.

!!! note "Machine energy requirements"
    Most recipes require ¹⁵⁄₁₆ of the tier's EU value to function, so an LV Tin Cable can only supply enough energy for most LV recipes from at most 2 blocks away from the energy source.

Cables come in different density. A higher density can transfer a higher amount of Amps.

To account for cable loss, machines requiring large amounts of Amps (like the EBF), you should place these machines closer to your power use and use more dense cables. For example, an EBF that has 2 LV Energy Hatches requires 4A of LV, so using only 4A Tin Cables will barely be enough (it will be as most recipe Voltages account for some power loss) as even after traveling 1 block, each amp would already lose 1 EU.

!!! tip
    Cables have half the cable loss as wires do. Insulate your wires!

Superconductors are wires with 0 cable loss. They don't have a cable variant and won't electrocute you when touching them.


## Dynamo Hatches

Multiblock Generators use Dynamo Hatches to output electricity. Dynamo Hatches have 1A, 4A, 16A and 64A variants.

!!! note
    Multiblock Generators will always try to "fill" existing generated Amps before trying to generate more Amps.


## Transformers

Transformers can either _transform down_ 1 Amps of a higher Voltage to 4 Amps of a lower Voltage, or _transform up_ 4 Amps of a lower Voltage to 1 Amps of a higher Voltage. The Mode can be swapped by right-clicking with a Screwdriver.

Transformers come in 1-4, 2-8 and 4-16 versions.


### Active Transformer

The Active Transformer is a multiblock that can transform to and from any voltage, accepting energy with an Energy Hatch and outputting energy with a Dynamo Hatch.

## Diodes

Diode blocks can be used to limit the Amperage throughput of a network. A Diode has 5 input sides (green) and 1 output side (red). By default a Diode accepts and Amps and outputs 1 Amp. The output Amperage can be changed by right clicking with a Soft Mallet.

![Diode 16A to 4A](./assets/diode.png)
*An LV Diode taking 16A of LV power and outputting 4A of LV power to prevent the smaller cable from burning.*

## Power Substation (PSS)

The Power Substation is a multiblock battery. It uses Lapotronic Capacitors to store power and it can have many Energy Hatches and Dynamo (Energy Output) Hatches. It is gated behind LuV circuits, which can be made in EV tier with an EV Circuit Assembler.

![A power substation using 2 Energy Hatches and 2 Dynamo Hatches](./assets/pss.png)
*A power substation using 2 Energy Hatches and 2 Dynamo Hatches*