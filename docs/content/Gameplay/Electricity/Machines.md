# Electric Machines
The vast majority of functional machines in GregTech Modern are powered by EU. And as with Generators, there are a 
number of shared rules and patterns between them.

## Singleblock Machines
Available at all electrical tiers from LV to UV, singleblock machines consume EU to run recipes. All singleblock electric
machines:

* Perform recipes to produce or convert items or fluids
* Consume EU every tick to operate
    * A machine that runs out of power mid-recipe will "Powerstall", stopping processing and resetting its recipe progress
  to 0. It will then sit idle for several seconds, attempting to refill its internal energy buffer before starting again.
    * A powerstalling machine does not delete its input items, it simply cannot complete its work until it is given enough
  energy to run.
    * A powerstalling machine can also be fully set to standby by right-clicking it with a Soft Mallet. This will cause
  it to stop attempting to run the recipe until turned on again, but will not delete the input items. 
    * In previous versions of GregTech, machines could be paused and unpaused mid-recipe, and a powerstalling recipe would
  have its progress tick backwards rather than fully resetting. These behaviors were changed in 7.0.0, as they allowed
  exploits in running machines without feeding them sufficient power.
* Have a Voltage Tier. This voltage tier determines:
    * The voltage which can be safely input to the machine. A machine which receives an amp of power from a voltage above
    its tier will **explode**.
    * The tier of recipes the machine can run. Many recipes have a minimum required voltage tier.
    * The tier of **Overclock** the machine runs at. Higher-voltage machines running lower-voltage recipes will Overclock
  the recipes. And Overclocked recipe consumes power as if it were a tier up (4x EU/t), and be completed in 1/2 the time.
  This does mean that overclocked machines are less energy efficient (4x voltage, 1/2 time, 2x total energy usage), 
  however such is the cost of technology, speed, and industrialization.
* Accept EU emitted by a connected Generator or Cable. Cables and generators can connect to any side of a machine.
    * Every singleblock machine contains a small buffer of energy, equal to (Voltage x 64) EU
    * Every singleblock machine accepts 1 Amp of power at its voltage tier to fill its buffer.
    * When running a recipe, the machine accepts amps equal to the amperage of the recipe.
    * When below 50% energy buffer and running a recipe, the machine accepts 1 additional Amp.

This last aspect means that singleblock machines will, in general, accept either 1 or 2 Amps of power from any
connected generators. While it is possible to create recipes consuming multiple amps of power, there are currently
no recipes in GregTech Modern that do this.

* Contain a Battery Slot, marked with a lightning bolt. Batteries will be discussed more in [Energy Storage](./Energy-Storage.md#batteries), 
however Batteries placed inside Machines will:
    * Charge themselves when the machine's buffer is more than 2/3s full
    * Discharge themselves to power the machine when the machine's buffer is less than 1/3 full

These behaviors mean that a Battery can be used to stabilize the power feed to a machine that may otherwise not
have sufficient power to run continuously, counteract Powerstalling, and allow the machine to buffer enough power to 
run for short bursts and finish important recipes.

In the ancient days of IndustrialCraft 2, Redstone Dust could be placed in a machine's battery slot to provide 1000 EU
to the machine. This is *not* a feature in GregTech Modern.

## Multiblock Machines
Starting from the Electric Blast Furnace as the gateway to MV, and expanding greatly in HV and IV, Multiblock Machines 
are the answer to needing more processing speed and volume, or doing processes which are too large or power intensive to
run in singleblock machines. All multiblock electric machines:

* Perform recipes to produce or convert items or fluids.
    * Most Multiblock machines have an additional operating mode called Batch Mode. Batch Mode is enabled via a toggle button
  in the multiblock's Controller. Batch Mode has no effect on recipes that take longer than 2.5 seconds (after overclocks).
  However, for any recipes shorter than 2.5 seconds, the machine will attempt to combine together multiple recipe runs
  into a single large batch, completing as many recipes as possible within 5 second periods, combining their inputs and
  duration. This reduces how frequently the machine needs to search for new recipes, and improves server performance for
  large, late-game bases with extremely fast machines.
* Are a Structure built around a Controller.
    * The Controller defines the multiblock, is used to examine the machine's current activity and status, and is used
  to toggle the machine's recipe processing on or off. However, the Controller does **not** handle any item, fluid, or
  energy input or output.
    * Much of a multiblock's structure will be comprised of Casings of some variant. Casings are fully inert blocks 
  which make up much of the cost of a multiblock structure, but are very simple and cheap to make next to functional 
  machine blocks.
    * Certain multiblock machines, notably the Electric Blast Furnace and Alloy Blast Smelter, also contain Heating Coil
  blocks. These Heating Coils come in multiple tiers and determine some of the machine's recipe capabilities and parameters,
  either unlocking new recipes or making existing recipes more efficient. For machines which contain Heating Coils, all 
  Coils in the structure must match.
* Contain Hatches and Busses. Hatches and Busses are blocks which replace Casings in a structure, and are the places where
Items, Fluids, Energy, and other interactions are done with the multiblock structure. 
    * Input hatches and busses will automatically pull items or fluids into them through their input face; Output hatches
  and busses will automatically push items or fluids into inventories or tanks connected to their output face. This 
  automation behavior can be disabled by toggling the hatch Off using its power button or a soft mallet.
    * All multiblock structures have a "Minimum Required Casings", which acts as an upper limit on how many Casings
  can be replaced by busses and hatches.
    * Add-on mods may also define other types of Hatches to input or output other special types of recipe ingredients.
* Consume EU from Energy Hatches. Unlike with singleblock machines, multiblock machines do not have an inherent voltage
tier. Instead, multiblock machines operate at a voltage tier equal to the combined input of all of their energy hatches.
    * Standard Energy Hatches accept 2 Amps of power on-tier, and provide this power to their associated Controller.
  An Energy Hatch which receives an amp of power of a voltage above its tier will **explode**.
    * Most multiblock machines can accept multiple energy hatches, allowing them to run recipes at higher tiers
  using lower-tier components. This is immediately used on the Electric Blast Furnace, which requires MV power to run
  most recipes but can only be initially built using LV Energy Hatches. As such, a player's first EBF must be constructed
  using two LV Energy Hatches, and fed by four LV Generators (or a [4x Battery Buffer](./Energy-Storage.md#battery-buffers)).
    * The power tier of a multiblock machine is also used to determine Overclocks. Almost all multiblock machines use
  the same Overclocking rules as normal machines (4x EU/t, 1/2 recipe time), with three notable exceptions:
        * The **Large Chemical Reactor** uses "Perfect Overclocks": recipes run at 4x EU/t but **1/4th** recipe time,
      meaning that the LCR does not lose energy efficiency when overclocked. This also makes the LCR extremely fast at
      completing recipes.
        * The **Fusion Reactor** uses "Perfect Half Overclocks": recipes run at 2x EU/t and 1/2 recipe time.
        * If a multiblock machine is overclocked to the point that its recipes would take less than 1 tick to complete,
      it begins performing Subtick Overclocks. Subtick Overclocks work similar to Batch Mode, in that the machine will
      attempt to perform multiple copies of the recipe (as many instances as it could finish in 1 tick) to try to keep 
      pace with the machine's newfound extreme speed.
* Most (but not all) require Maintenance. Maintenance Issues occur every few hours while a multiblock machine is running
and must be repaired by using a Tool on a Maintenance Hatch. The maintenance issue that occurs dictates the tool needed.
Most machines require a Maintenance Hatch; and when a Maintenance Hatch is first placed it appears with all maintenance
issues present and thus needs maintenance before the machine can come online.
    * A machine with maintenance issues will safely shut itself down after finishing its current recipe, and once the
  maintenance issues are resolved it will turn on again.
* Can **Wallshare** components. The inert casings, frames, coils, and *most Hatches and Busses* in the structure of a multiblock
machine can be shared between multiple controllers. Unlike in most other mods that contain multiblock machines, GregTech
Multiblock Machines do **not** form a single connected entity out of the full machine; the only interactable Block Entities
are the Controller and the Hatches. The result of this is that, while the first multiblock machine of a given type must
be built at full cost, additional machines of the same type can be built sharing walls with the first, significantly
reducing their full build cost.
    * Most Hatches and Busses can also be shared between machines, allowing wallshared machines to take from the same 
  feed of input materials to run the same recipe side by side, or run different recipes that share a common ingredient.
    * Energy Hatches can be shared between machines, however this is generally discouraged as it can lead to the machines
  running out of power.

### GCYM Multiblock Machines
Constructing the Alloy Blast Smelter unlocks production of a number of complex metal alloys. These alloys are used to build
a line of IV-tier machines sometimes called the Gregicality Multiblocks. GCYM was an independent GregTech add-on mod that
was fully integrated into GregTech Modern, and adds a collection of multiblock replacements for singleblock machines.
The GCYM Multiblocks have all the above properties of normal Multiblock machines, with a few additional features:

* Are constructed from alloys made in an Alloy Blast Smelter.
* Perform recipes otherwise available in singleblock machines.
    * Certain GCYM Multis can also perform multiple different recipe sets. For example, the Large Centrifuge can perform
  either Centrifuge or Thermal Centrifuge recipes. The operating mode of a machine can be set in its Controller.
* Accept a **Parallel Hatch**. Parallel Hatches cause a machine to attempt to run multiple copies of a recipe simultaneously,
combining their inputs, outputs, and EU/t cost. If a machine does not have enough energy input to run its Hatch-allowed 
parallels at full overclocked speed, it will reduce its overclock tier to compensate, improving energy efficiency but 
(because of the parallel runs) not sacrificing recipe throughput volume.
* Can use High-Amp Energy Hatches.
    * Normal energy hatches accept 2 Amps of power. GCYM machines can accept 4A or 16A Energy Hatches, allowing for
  running higher parallel counts at higher overclock tiers.