# Cables and Transformers
EU needs to move from generators to storage to machines. EU travels through Wires and Cables.

Every wire or cable has several properties:

* Toggled connections. A cable is placed in world as a block and can be connected separately in all six directions. Cables
are not directional in how they allow power to flow. Cable connections can be added or removed using Wire Cutters, and 
Wire Cutters are also the tool used to break and retrieve cable blocks.
* Maximum Voltage. Every cable has a maximum voltage of energy it can carry. Sending EU of too high a voltage down a 
cable will cause the cable to catch fire and be destroyed.
    * If a cable attempts to carry an amp of too high a voltage, the cable will *reduce the carried voltage to its own
  safe limit* before being destroyed. This means that, if a high-voltage generator is inadvertently connected to a low-
  voltage cable, the cable will act as a sacrificial fuse and be destroyed, but the lower-voltage machines further down 
  the line will be protected and not explode. As such, it is generally much less safe to directly power machines using
  cables that carry a higher voltage tier than the machine.
* Maximum Amperage. Similar to Voltage, every cable has a maximum Amperage it can safely carry. However, sending too many
Amps down a cable will not cause an instant failure. Instead, over-amping a cable will cause it to very briefly heat up.
If the cable heats up too much, its Insulation layer will burn off, and if it continues heating, it will eventually
catch fire and be destroyed.
    * Wires can be combined into 2x, 4x, 8x, or 16x Wires. Combined wires combine their maximum amperage, allowing more
  Amps to be carried through a single block space.
    * To ensure amperage safety, it is generally recommended to never run cables that can carry fewer Amps than the 
  [Generators](./Generators.md) or [Battery Buffers](./Energy-Storage.md#battery-buffers) connected to them them can supply. 
  (1A per singleblock generator, [Dynamo Hatch] Amps per Multiblock generator, [Battery slots] amps per Battery Buffer.)
  This is important to note as machines will often *accept* more than 1 Amp of power, and thus it's generally simpler to 
  control for energy supply than energy demand.
* Voltage Loss per Block. Power transfer is not free. For every cable block that an Amp of electricity travels down, one
or more Volts are lost from it. This effect is much more pronounced at low voltages or when using inferior cable materials,
and especially when using uninsulated wires. For an example of this, see [below](./Cables-and-Transformers.md#an-example-of-voltage-loss-and-transformer-usage).
    * Voltage Loss can be compensated for by using [Battery Buffers](./Energy-Storage.md), Diodes, or Transformers.
  While these do not eliminate voltage loss, they can mitigate its effects on machines.
* Insulation. Wires should never be used without first being covered in insulation and converted to Cables. Cables have
significantly reduced Voltage Drop than uninsulated wires. Furthermore, touching an uninsulated wire that has carried 
energy during the last tick will inflict a significant amount of damage, easily lethal at high voltages.
    * Higher-thickness cables are made by applying insulation to higher-thickness wires.
    * Cable insulation is made out of Rubber. LV cables can be insulated by crafting them together with Rubber Sheets,
  however higher tier cables require an Assembler and Liquid Rubber. 
      * Liquid Rubber can eventually be replaced with Silicone Rubber or Styrene-Butadiene Rubber (and high tier
    components require this). 
      * EV cables and above also require Thin Polyvinyl Chloride Sheets, and higher tier cables require further sheets.
    * Certain wires are marked as Superconductors. These wires do not require insulation, are safe to touch, and have
  0 voltage drop per block. However, they are all made out of alloys that are much more complex than the simple wires
  and cables that are generally used for power transmission.
    * Insulation can be removed from wires using the Packer. This can be used to retire older cables and reclaim the
  wires used.

## Diodes
Diodes are blocks used to help manage power transmission.

* Energy can enter a Diode from any of five sides, but can only exit the Diode from its output side.
* Diodes limit the energy flowing through them. By default, a Diode will emit 1 Amp of its voltage, but right-clicking
with a Soft Mallet will cycle its output through 2, 4, 8, and 16 Amps. Diodes will accept Amps equal to their output.
* Diodes do not store large amounts of power. They have the same 64-tick buffer as any other machines of their tier.
* Diodes can be placed in the walls of **Cleanrooms**. This is the means by which EU can be sent into a Cleanroom to power
the machines inside.

Diodes are much cheaper than Battery Buffers, and while they do not act as bulk energy storage, they can be used to merge 
several small cables into one large cable, or to tap small cables off of a large cable, and ensure that the resulting
cables are never sent more Amps than they can carry. Also, because they are blocks which absorb and emit power rather than
simply transmitting it, every Amp that is emitted by a Diode *will* be on-Voltage, thus allowing Diodes to compensate for
voltage loss, by (as necessary) merging together multiple reduced-voltage amps into a few full-voltage amps.

## Transformers
Transformers allow for shifting voltages up and down. A Transformer can convert 1 Amp of a voltage one tier above its own
into 4 Amps of its own voltage tier, or vice-versa. (For example, an LV Transformer will accept 1A MV and emit 4A LV, or
accept 4A LV to emit 1A MV.) A Soft Mallet will switch the Transformer's mode from Down to Up.

Transformers come in three further variants: 2x High-Amp (converting 2A into 8A), 4x High-Amp (converting 4A into 16A), 
and Power Transformer (converting 16A into 64A).

Transformers are useful for powering a large array of low-voltage machines using a small number of high-voltage generators.
As generators are quite expensive to build, powering many parallel machines with only a few generators can save significantly
on resources and space.

Transformers are also useful to help mitigate Voltage Loss from cables. Higher voltage cables do not always have lower
voltage loss per block, however voltage loss per block per Amp is *subtractive*, not *multiplicative*. As such, transmitting
a small number of high-voltage Amps a long distance results in far less energy loss than transporting a large number of
low voltage Amps that distance. (And furthermore, carrying more Amps requires thicker and more expensive Cables.)

### An Example of Voltage Loss and Transformer Usage
For an example of this last point, take following simple case: 16A LV, traveling 20 blocks down a 16x Tin Cable. 
A 16x Tin Cable can carry 16 Amps at LV, and suffers 1V loss per block per amp. By the end of the 20 block travel, the 
32V x 16A that entered the cable has been reduced to 12V x 16A, losing 68.75% of its power and greatly reducing its 
ability to reliably power machines (as at that point any machine that requires more than 12EU/t to run will *need* to 
consume an entire second Amp), likely limiting the cable to powering only 8 machines.

For contrast, take if that 16A LV was first Transformed up to MV and sent down the same 20 blocks of 4x Annealed Copper Cable.
A 4x Annealed Copper Cable can carry 4A at MV, and suffers 1V loss per block per amp. By the end of the 20 block travel,
the 128V x 4A that entered the cable has been reduced to 108V x 4A, losing only 15.6% of its power, and the resulting 
4x Hi-Amp LV Transformer on the destination end is able to emit that as 13.5A LV (Amps must be whole numbers so it will
alternate between 13A and 14A), and thus easily power at least 13 machines running at full speed.

## Active Transformers 
The Active Transformer is an LuV-tier multiblock machine. An Active Transformer can accept up to **13** Energy, Dynamo,
or Laser Hatches, of any voltage tiers, and it will transform all of its input feeds to power all of its output feeds.

Active Transformers are commonly paired with [Power Substations](./Energy-Storage.md#power-substation) to take the 
Substation's very high output amperages, and appropriately deliver them to machine arrays.

### Laser Hatches and Laser Pipes
Power Substations and Active Transformers can use Laser Source Hatches and Laser Target Hatches as an extremely high-Amp
alternative to traditional power delivery. Laser Hatches become available starting at IV, and can emit or receive from 
256A to 4096A power.

Laser Pipes carry energy from Source Hatches to Target Hatches. They are quite cheap for their power but have several
important characteristics:

* Laser Pipes have no Max Voltage, Max Amperage, Voltage Loss, or Insulation. They carry energy at whatever Voltage and
Amperage they are fed from their Source hatch.
* Laser Pipes must be connected in a **straight line**. Laser Pipes are not allowed to have any bends in them, or they 
will not transfer power.
    * As a consequence of this, Laser Pipes can only act as 1-to-1 connections between a single Source and Target Hatch. 