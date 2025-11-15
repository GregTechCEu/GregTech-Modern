# Electric Energy Storage
[Generators](./Generators.md) are expensive machines. While it is possible to build a generator to power every machine, 
it's much less expensive to build a small number of generators, and a means to store the generated EU, allowing a small 
number of generators to power a large array of machines. This becomes especially applicable in EV and beyond, where 
singleblock generators are no longer available.

GregTech Modern contains three primary forms of energy storage: Batteries, Battery Buffers, and the Power Substation.

## Batteries
Available at the beginnings of LV and present at all energy tiers, a Battery is an item which stores EU. Batteries are
generally made in the Canner, out of a Battery Hull and some amount of acceptable Dust (initially Lithium, Cadmium, or
Sodium). Higher-tier batteries (HV and beyond) also include the Energy Crystal, Lapotron Crystal, and the various
Lapotron Orb derivatives. These Crystal batteries are made using Autoclaves and Assemblers, and are more expensive than 
traditional batteries, but have much higher power capacities.

Batteries can be used in four ways:

* Batteries can be placed in singleblock [Electric Machines](./Machines.md#singleblock-machines). All singleblock machines
have a dedicated Battery slot, marked with a lightning bolt. A Battery placed in this slot will:
    * Charge from the machine's energy buffer, if the machine's buffer is above 2/3s full
    * Discharge to feed the machine's buffer, if the buffer is below 1/3 full
* Batteries carried in a player's inventory will use their held charge to recharge electric tools or Armor that the player
is holding or wearing, at a rate of 1 Amp per tick. This behavior can be (de)activated by shift-right-clicking while 
holding the battery.
* Batteries can be placed in Turbochargers to rapidly recharge them. A Turbocharger will accept up to 4 Amps of power
per electric item contained (Batteries, Tools, Armor), and distribute that power among their contained batteries.
* Batteries can be placed in Battery Buffers, which will be discussed next.

## Battery Buffers
A Battery Buffer is a block that contains 1, 4, 8, or 16 inventory slots. Each of those inventory slots can hold one
battery. Battery Buffers will accept 2 Amps of power per contained electric item, and will **output** 1 Amp of power per
contained battery. Battery Buffers will charge and discharge all contained batteries evenly, and are the primary means of
bulk energy storage and power supply stabilization from LV through mid EV.

In early EV, Battery Buffers also have an important further usage. A Battery Buffer can have an Energy Detector or
Advanced Energy Detector Cover attached, to read the total energy contained within their held batteries. This readout
is emitted as a Redstone Signal, which can be fed to a Machine Controller cover placed on a [Large Steam, Gas, or Plasma
Turbine](./Generators.md#large-turbines). This setup can then be used to automatically turn the Turbine On when the 
batteries run low, and Off when the batteries fill, greatly saving fuel and ensuring that full power is available at all
times, regardless of if the Turbines are spun up or not.

## Power Substation
The Power Substation is GregTech Modern's strongest answer to energy storage, centralization, and distribution. The PSS
is a Multiblock structure, available midway through EV, and constructed out of Palladium, Laminated Glass, and Capacitor
Blocks. The total energy storage of a Power Substation is based on the set of Capacitor Blocks used to build it.

Power Substations have several significant features to them:

* Extremely high energy storage capacity. The initial EV PSS holds up to 2.7 billion EU, roughly 18 Amp-Hours of energy.
* High input and output capacity at arbitrary Voltages. The PSS does not have a Voltage Tier, and can have multiple 
Energy Hatches and Dynamo Hatches placed in it, of any tier.
    * 64 Amp Hatches. The PSS has a unique set of Energy and Dynamo Hatches capable of accepting or emitting 64 Amps of
  power, from EV through MAX Voltage.
* Laser Hatches. Power Substations can utilize Laser Source Hatches and Laser Target Hatches, to transfer colossal Amperages
between Substations or Active Transformers
* Very slight power decay. Power Substations lose approximately 1% of their stored energy per 24 hours. At the power scale
that the PSS exists at, this is a relatively insignificant drain, but it does mean that you can't fully ignore power
generation.

The extremely high amperages allowed by the Power Substation pair well with the high outputs and large size of [Multiblock
Generators](./Generators.md#multiblock-generators), and encourage fully centralized power production and distribution. 
Designing a base to take advantage of this also acts as a strong encouragement to utilize [Transformers](./Cables-and-Transformers.md#transformers) 
to transmit very high voltage energy over long distances using thin cables, before downtransforming it at machinery lines
for use.
