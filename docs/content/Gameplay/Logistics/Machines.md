# Machine Logistics
## Steam Machines
Steam Machines do not have any form of built-in logistics capability, with three exceptions:

* The Primitive Water Pump is a Multiblock Machine and contains an Output Hatch. This hatch will automatically push water
  out into any connected pipe or tank.
* The Coke Oven is also a Multiblock machine, but unlike other multiblocks its controller does contain its inventory.
  To aid in Coke Oven automation, a unique block called the Coke Oven Hatch can be placed in its structure, which will
  accept item input and automatically push items and fluids out.
* All Boilers which contain Steam will attempt to push Steam out into any fluid pipes or machines adjacent to them, on
  all sides _except below_. This means that water can be safely input from below with no risk of steam entering your water
  pipes.

Additionally, Steam machines have a unique design challenge feature: all Steam machines have an Exhaust face, which must
be facing open air, and will emit a blast of Steam every time the machine finishes a recipe. If the Exhaust face is
blocked, the machine cannot complete its recipes; and the blast of Steam released will heavily injure any player
standing in it.

## Electric Machines
All Electric machines have the ability to automatically output their produced items, fluids, or both, into any adjacent
machine, inventory, tank, or pipe. This can be done with a Wrench, or via the Side Configuration tab in the machine's UI.

* To change auto output from outside a machine, shift-right-click on air with the wrench to choose whether to configure
  for Items, Fluids, or Both, then right click with a Wrench on the side of the machine to rotate its output face.
  * To then enable automated output, right-click the machine face with a Screwdriver.
* To change auto output from inside a machine, open the Side Configuration tab, and click the side you want to set as
  output.
  * One click will select the side, a second left click will set the side to item output, and a third left click will
    toggle auto output. Right clicks will set and toggle fluid output.
  * The main machine UI also contains two toggle buttons to enable or disable auto output for items or fluids, without
    needing to open the configuration panel.

Additionally, all electric machines will also **block** automated item or fluid input from their output sides. To
override this and allow input from output side, there is an additional toggle button in the Side Configuration tab, or
shift-right-click on the machine with a Screwdriver.

## Multiblock Machines
Multiblock machines do not contain items within their controller blocks, and instead all I/O is handled by Buses and
Hatches. Buses and Hatches will, by default, **auto-import** from any inventories, tanks, or pipes they face, and
**auto-export** to any inventories, tanks, or pipes. This behavior can be toggled by Disabling the hatch, either through
the power button in its UI or by right-clicking it with a Soft Mallet.

Buses and Hatches can accept automated import or export from other sides, so long as something else is causing it.

## Passthrough Hatches and the Cleanroom
The Cleanroom is a unique multiblock with unique restrictions. Because the Cleanroom must have solid walls, pipes, cables,
and inventories outside cannot directly connect to machines inside. For this purpose, Passthrough Hatches exist.
Passthrough Hatches (by default, only the HV Passthrough Hatches can be crafted) are solid blocks which can be placed in
the walls or floor of a cleanroom. These hatches act as Input and Output at the same time, and will auto-Import from
their green face, and auto-Export to their red face, in the same tick. This allows items and fluids to be pulled through
the cleanroom walls, in or out.

Furthermore, as Generators cannot be placed inside the Cleanroom, to transfer power in, Diodes can be used. Diodes can
also be placed in the Walls or Floor, and if right-clicked with a Soft Mallet will limit how many Amps will travel through,
cycling through 1/2/4/8/16A.

Finally, Machine Hulls have a unique feature. On one hand, they can be used as a 1A Diode and transfer a single amp
of power. On the other hand, they are also considered valid cables for an Applied Energistics ME Network, and thus
ME Cables will connect to Hulls and allow an ME Network to extend into a Cleanroom.