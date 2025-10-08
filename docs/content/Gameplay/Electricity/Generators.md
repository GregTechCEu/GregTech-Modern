# Generators
Generators are machines which consume Fuel to produce EU every tick. There are several different types of Generators, 
which each consume different fuels.

## Singleblock Generators
From LV to HV, EU is produced using singleblock generators, machines like the Basic Steam Turbine or Advanced Combustion
Generator. All singleblock generators:

* Consume Fuel. In base GregTech Modern, all electric generators consume either liquids or gasses as fuels, but no items.
EMI can display a list of all valid fuels a generator can consume. This fuel display lists:
    * An amount consumed per burn cycle. For most fuels, this is 1mb, however for certain low-efficiency fuels like Steam 
  it can be much higher.
    * A burn time.
    * The generation rate. Any generator which consumes this fuel must be of a power tier at or above this rate.
    * The total EU produced by one cycle. This is equal to the burn time x the generation rate.
* Produce EU every tick. All singleblock generators produce and output EU equal to 1A @ their tier voltage.
* Output EU every tick. All generators have one output side, marked with a colored dot, which provides 1A @ tier voltage
to either a Cable or an adjacent Machine.
    * Generators do not output "partial" amps or partial voltage. Generators will always output power in exact packets
    of 1A @ tier voltage.
* Cannot be placed inside **Cleanrooms**. Placing a Generator inside a Cleanroom will block the Cleanroom controller from
activating.
    * To transfer power into a Cleanroom, use a [Diode](./Cables-and-Transformers.md#diodes).

Singleblock generators come in several variants, each accepting different fuels.

* Steam Turbines produce EU from large volumes of Steam. The EMI display for Steam Turbines shows that they also
output Distilled Water; this is a feature of the multiblock Large Steam Turbine (discussed below) but not the basic
singleblock turbines
* Gas Turbines produce EU from combustible gasses such as Methane and Benzene.
* Combustion Engines produce EU from liquid Oils, Diesels, Gasolines, and Biofuels. Combustion Engines benefit very 
heavily from refining fuel before burning it.

Singleblock generators are simple to operate, but are relatively expensive, often more expensive than the machines they
give power to. While it is possible to directly feed machines with generators, it is often preferable to build a small
number of generators along with some form of [Energy Storage](./Energy-Storage.md) to power a large number of machines, 
given that most of those machines will not consume a full amp of power, or will not be running simultaneously.

MV and HV generators consume additional fuel to produce power at higher voltages. They do not do this by altering fuel 
burn time; they do this by consuming multiple cycles of fuel simultaneously (which is sometimes displayed as "running [X] 
recipes in parallel"), multiplying both their EU/t and fuel consumption rate.

## Multiblock Generators
At EV and beyond, energy generation shifts to multiblock machines. The size of these machines strongly encourages
centralizing power production, storage, and distribution; however because these generators are largely made of Casings, 
they are substantially cheaper than the large number of singleblock generators needed to match their output. 

All Multiblock Generators:

* Consume fuel. Lots of fuel. This fuel is provided to them using Input Hatches, as with other multiblock machines.
* Generate and output EU. Unlike with singleblock generators, this EU is outputted from a **Dynamo Hatch** placed on
one side of the generator. The Dynamo Hatch used determines the maximum output of the generator. Dynamo Hatches come in
several variants, with different Voltage *and Amperage* output values, allowing a single multiblock generator to produce
multiple Amps of power at their voltage. This helps compensate for their large size.
    * A Dynamo Hatch determines the Voltage and the Maximum Amperage output by the generator. This is **not** the same as
  the amount of EU the generator produces per tick. While using an undersized Dynamo Hatch will limit the maximum
  output of the generator, using an oversized Dynamo Hatch will result in a generator which awakens from idle and 
  emits a much higher amperage than its on-paper sustained power generation for a few seconds, until the Dynamo's internal
  energy buffer drains.
* Have efficiency boosts. Depending on their configuration, multiblock generators can produce significantly more EU per 
mB of fuel than singleblock generators. This further compensates for their size, allowing larger and more powerful 
machinery to be operated using a smaller fuel source.
* Require open air in front of them (either in front of their Air Intakes or their Rotor Holder)
* Allow Wallsharing to save on casings between multiple turbines.

There are two primary types of Multiblock Generators: the Large Combustion Engine, and Large Turbines.

### Large Combustion Engines
The Large Combustion Engine (EV) and Extreme Combustion Engine (IV) are the simpler of the large generator types. They 
consume Combustion Engine fuels to produce EU. They also passively consume Lubricant to operate.

The output and energy efficiency of an LCE/ECE can be significantly increased by feeding the generator with Oxygen (LCE)
or Liquid Oxygen (ECE). This boost doubles the fuel consumption of the generator, but triples (LCE) or quadruples (ECE)
the energy production.

If the Dynamo Hatch of an LCE/ECE fills with EU, the engine will pause and stop consuming fuel.

Large Combustion Engines are expensive structures to build but are fairly low-maintenance once operational; and higher
tier combustion fuels such as Gasoline are quite energy dense.

### Large Turbines
The Large Steam, Gas, and Plasma turbine are more complex generators which accept a wider range of fuels.

* Large Steam Turbines consume extremely large volumes of Steam to produce EU. LSTs also output Distilled Water, allowing
for setups that loop back their own feed water or providing free distilled water to use elsewhere.
* Large Gas Turbines consume large volumes of Gas Turbine fuels such as Benzene to produce EU.
* Large Plasma Turbines consume small volumes of Plasma, produced by Fusion Reactors, to produce large amounts of EU.
LPTs also output the liquid or gas form of the plasma that was consumed as fuel.

Large Turbines are significantly cheaper to construct than Large Combustion Engines. However, they require an additional
component: A **Rotor**. The Rotor is an expensive item with a finite durability (measured in seconds), that determines
the fuel consumption, energy production, and efficiency of the Turbine. Rotors are placed in Rotor Holders, and Rotor
Holders cannot be opened while the Turbine is active. (Trying will hurt.) Higher tier Rotor Holders increase the power
output of the Turbine (doubling the power output and fuel consumption per Rotor Holder tier) but also increase the
fuel efficiency of the turbine (reducing fuel consumption by 10% per tier above the Turbine's minimum requirement).
Unlike with smaller generators, this reduced fuel consumption is applied as *increased fuel burn duration*.

Additionally, Large Turbines require several minutes to spin up to their full output, and then spin down when inactive 
or their Dynamo Hatch is full of energy. Energy production scales exponentially with turbine RPM, meaning that for much 
of the spinup time the turbine's output will be quite low. When active, RPM increases by 1 per tick, and when inactive 
RPM decreases by 3 per tick. To compensate for this behavior, Large Turbines are best run either continuously, or in 
limited bursts to fill an energy storage, activated via Machine Controller Cover when the storage is low and deactivated
when the storage is nearly full.

The total EU/t output and fuel consumption of a Large Turbine is determined by:

* EU/t Output = [Turbine base EU/t] x [2 ^ Rotor Holder Tier - minimum tier] x [Turbine Power Mutiplier] x [Current RPM / Max RPM]^2
* Fuel Consumption = [EU/t output] / [Fuel base generation rate]
* Fuel Duration = [1 + 0.1 x [Rotor Holder Tier - minimum tier]] x [Rotor Efficiency Multiplier]

