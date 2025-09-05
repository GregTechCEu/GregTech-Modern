# Pipes
GregTech Modern offers both Item and Fluid pipes, made out of a wide range of materials in a wide range of capacities.

## Pipe Placement
Pipes and Cables use a shared, unique system for placement in world.

A pipe or cable which is placed in world will, by default, not connect to any other adjacent blocks. However, if the
pipe was shift-right-click placed against a machine or another pipe, the pipe will be placed connected to that machine.

Once one pipe has been placed, looking at that pipe with another pipe will display the same sided overlay as when
looking at a machine with a wrench or other tool. Right-clicking with a pipe within one of the sided regions will place
a new pipe *on that side of the existing pipe*, connected to the pipe. This allows for placing lines of pipe without
needing to stand in the path being placed along.

### Frame Boxes
Pipes can be placed inside of Frame Boxes, and Frame Boxes can be placed over Pipes. This is primarily an aesthetic
choice, however it also forces the pipe's collision box to take up a full block of space, and thus also provides some 
protection from the hazards of touching a fluid pipe carrying very hot or cold fluids (see below).

## Pipe Connections
Pipes can be right-clicked with a wrench to Connect or Disconnect the pipe from that side. This can be used to
preemptively set pipe connections in preparation for a machine to be placed there, or to connect or disconnect pipe
segments that need to branch off of others.

Additionally, pipes can be shift-right-clicked in order to "Shutter" a given side of them. Shuttered pipe sides become
**output-only**, allowing pipes to be made one-way. Shuttered pipe sides are visible via a small black arrow drawn in the
allowed direction of transfer.

Pipes have default logic for how they distribute their contents across their connections, however these behaviors can be
modified by using [Covers](./Covers.md)

## Pipe Materials and Sizes
Pipes come in four sizes: Small, Normal, Large, and Huge. Fluid pipes also come in a Tiny size. Each larger size is more
expensive but has greater throughput. The throughput of a pipe is determined by its Material (and in general, materials
that require higher voltage tiers to produce have higher throughput) and multiplied by its size.

## Item Pipes
Item Pipes effectively act as teleportation tunnels between their sources and destinations: any items which are pushed
into a pipe, are instantly pushed out the other end(s). Item pipes can be 1-to-1, 1-to-many, many-to-1, or many-to-many.
Item pipes have a limit on how many items can be pushed through them every second. The limit of a full pipe is the limit
of the smallest segment through (as even though the items are teleported rather than moving from pipe block to pipe block,
they do still follow a path through the pipes and check every pipe along the path). There is no limit to the amount of
different items that can move through a pipe at once, only the total count per second.

When items are pushed into a pipe, by default they are pushed to the "closest" inventory to the source. However, the
"closest" inventory is not directly determined by the distance in blocks between source and destination. Instead, it is
determined by the "Priority" value of the pipe. Larger pipes have lower priorities, smaller pipes have higher priorities,
and items will take the path which has the smallest total priority value.

### Restrictive Item Pipes
Item Pipes have a special variant called Restrictive Item Pipes. Restrictive pipes have 100x the priority value of
equivalent non-restrictive pipes, allowing a pipe path to be forcibly made into the "longest" path and thus the final 
path items can choose to go down.

## Fluid Pipes
Fluid Pipes function completely differently from item pipes. Each fluid pipe block is a small fluid tank, with a size
equal to 20x the pipe's listed Transfer Rate.

Every 5 ticks (4 times per second), fluid pipes will check for their adjacent connections, and try to export up to
Half of their maximum storage capacity, distributed among all adjacent connections that the pipe did *not* receive input
from since it last exported, based on the fullness percentage of those connections.

As a result, while fluid pipes list their throughput as mB/t, they do not transfer on every tick; they transfer a large
amount of fluid several times per second.

As a further result however, a line or network of fluid pipes which are not completely filled can experience the fluid
in the pipes sloshing around, becoming more full in some places and more empty in others. This can be mitigated by
Shuttering pipes to prevent backflow.

## Multi Pipes
While Item Pipes can transfer an unlimited number of item types at once, Fluid Pipes cannot and trying to move multiple
fluids through one pipe is liable to get a fluid stuck somewhere. To combat this, 4 Small Pipes or 9 Tiny Pipes can be
crafted together into a Quadruple or Nonuple Fluid Pipe. These pipes act as 4 or 9 separate piped all in a single block. 
Each of these pipes calculates its I/O independently, but **cannot contain duplicate fluids**, allowing 4 or 9 fluids 
to be transferred through a single block with no risk of jams.

## Hazards
Fluid Pipes have an additional set of properties on them: Max Temperature, and Fluid Containment.

All fluids in GregTech Modern have a Temperature, and some of them also have further properties:
* Acids
* Gases
* Cryogenics
* Plasmas

If a Pipe's max temperature is less than the temperature of the fluid traveling through it, the pipe will intermittently
void some of its contents, severely injuring nearby players and spreading fires, and quickly be destroyed.
The exception to this is that if the fluid a pipe is carrying is a Plasma, and the pipe is marked as being able to contain
Plasmas, the temperature limit is ignored.

For other properties, if improperly contained, the effects are similarly hazardous. Acids and Cryogenics will harm their
surroundings, then explode. Gases within pipes that cannot carry them will not destroy the pipe, but the gasses will
quickly escape and cause small explosions.

Finally, pipes carrying very hot or cold fluids (above 320K or below 260K) will injure entities that touch them, twice
per second, damage scaling with how extreme the heat or cold is.