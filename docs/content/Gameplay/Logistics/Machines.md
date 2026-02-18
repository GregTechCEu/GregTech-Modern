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

### Distinct, Painted, and Filtered Inputs and Outputs
Under normal circumstances, **all** input Buses and Hatches and on a multiblock machine will be checked for recipe
inputs, and all output buses and hatches will be used to place recipe outputs. However, this can lead to unwanted behavior
where a user wants a single machine to do multiple recipes, but the ingredients to those recipes can conflict and be used
to run an unwanted third recipe. Additionally, when a machine is being used like this, the output bus/hatch to which
produced items/fluids are delivered is chosen somewhat arbitrarily, making it difficult or unwieldy to plan pipes to carry
specific output products away.

GTM offers three tools for this problem: Fluid Hatch Filter Locking, Distinct Buses and Painted Buses/Hatches.  

* Fluid Hatch Filter Locking is a simple system for resolving the problem of deciding what output hatches receive what
produced fluids. Using the same interface as a Super Tank, a Fluid Output Hatch can have its current contained fluid
Locked, meaning that only that fluid will ever be placed in it; or the Hatch can be pre-emptively locked to a fluid by
dragging that fluid from JEI/EMI into the Hatch's output slot.
* Filter Locking only works with standard *single* fluid hatches, and cannot be done to the higher-tier Quadruple or 
Nonuple Fluid Hatches. (However, those Hatches also cannot contain a single fluid in more than one slot, so they do
still allow for some degree of output separation when used with Quadruple or Nonuple Fluid Pipes.)
* Distinct Buses is a toggle used on Input Buses (not Hatches), which causes the machine to look at this bus as being
separate from all other Distinct Buses. (One distinct bus has no meaning, but two distinct buses on one machine will
cause the machine to search each distinct bus separately).
* Painted Buses/Hatches are hatches which have been Painted using a can of spray paint. Input Buses/Hatches which have been
Painted in the same color are looked at together by the machine searching for recipe inputs, but any items/fluids stored
in buses/hatches with a different color are not used for the search. The isolation works the same as for Distinct Buses,
but it allows for multiple buses/hatches to be searched together as a group.
* Prior to version 7.5.0, painting **Output** Buses/Hatches had no effect. Version 7.5.0 introduced machines pairing their 
Painted Outputs to their Painted Inputs, such that if a recipe pulls items from a Painted Input, it can only output the 
products of that recipe to a Painted Output of the same color (or an unpainted output).
* Buses and Hatches that are not painted, or not set to Distinct, are always fair game for the machine - Distinct and 
Painted Inputs can always pull from other non-distinct and non-painted inputs; and recipes that used ingredients from 
Painted Inputs can always send their products to non-painted outputs.

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