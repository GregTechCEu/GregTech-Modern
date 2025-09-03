# Covers
**All** GregTech Modern blocks which contain a BlockEntity can have Covers attached to them to add additional functionality
or alter their normal functions. Some of these covers affect item and fluid transfer.

Covers can be attached to machines by right-clicking on the machine while holding the cover, or through the machine side
configuration UI. Covers can be configured by right-clicking their side with a Screwdriver, and removed by right-clicking
their side with a Crowbar.

Logistics are available at all voltage tiers starting at LV, and the maximum rate at which they transfer items and fluids 
is based on their voltage tier. (There is an add-on mod which also adds ULV covers however those are not included in base 
GregTech Modern.)

## Conveyor Modules and Electric Pumps
Conveyor Modules and Electric Pumps are the standard set of logistics covers. They can be placed on blocks with inventories
or tanks, or on item or fluid pipes (respectively). On placement, they default to Export mode, extracting items or fluids
from their attached block and pushing them into the block they face. By using a Screwdriver, they can be toggled to 
Import mode, and their transfer rate per-second or per-tick can also be configured.

When transporting items into an Item Pipe (either by being placed adjacent to a pipe and set to export, or placed on a
pipe and set to import), Conveyor Modules have an additional feature: Distribution Mode. There are three options for this:

* Priority: Items are sent to the closest available inventory first
* Round Robin: Items are distributed approximately equally across all available inventories
* Round Robin With Priority: Items are distributed across all available inventories, but the distribution ratio is based
on priority. (e.g. a destination with half the priority of the others receives twice as many items)

## Robot Arms and Fluid Regulators
Robot Arms and Fluid Regulators are more advanced versions of the Conveyor and Pump. They have the same features, but 
with an additional Transfer Mode toggle:

* Transfer Any: always transfer as many items/fluid as are available
* Transfer Exact: Only transfer items/fluid if an **exact** amount is available for transport (e.g. only transport 
exactly 13 items at once, or exactly 144mB of a fluid). The cover will not transfer any if it has less than the target, 
and will only transfer the exact amount even if more is available. This is useful for preventing machines being jammed
from a full inventory but each stack has only half the items needed to run a recipe.
* Keep Exact: Check the contents of the target inventory. Only transfer items/fluids if the amount of each available 
item/fluid in the target is less than the set amount, and only transfer enough to raise the target's inventory to the
set amount. This is useful to prevent a machine from getting completely filled with one item and having no space for a
second required item.

## Filters
Logistics covers can have Filters installed in them, to specifically allow (whitelist) or disallow (blacklist) chosen 
items or fluids to pass through those covers. Filters can be configured by right-clicking them against the air, and
items and fluids can be added to the filter either by placing them in from inventory (not consumed), or by dragging them
in from NEI/JEI/EMI.

There are five types of filters:

* Item Filter, Fluid Filter - filters by specific items/fluids. Can be set to filter by, or ignore, item NBT data.
* Item Tag Filter, Fluid Tag Filter - Filters using a Regular Expression string, searching by one or more Item Tags. 
Multiple tags can be included or excluded from a single filter using logical operators.
* Smart Item Filter - Filters items by searching recipe logic for valid ingredients. Supports the Centrifuge, Electrolyzer,
and Sifter recipe lists.

Filters can also be attached as covers to Machine faces set to auto-export or receiving inputs, to apply their filter 
to the items or fluids traveling through that face.

Fluid Filter Covers can also be attached to Fluid Pipes to isolate individual pipe directions and ensure only a single 
fluid can travel down that pipe (if, for example, there was a Quadruple Fluid Pipe carrying four fluids, and one fluid
needed to be separated out).

## Ender Links
Independent from all these other devices, there are also another set of covers: the Ender Link Covers. These come in 
three types: Item, Fluid, and Redstone. These covers must be configured with a screwdriver on placement, but once configured
they become part of a Network of wirelessly connected Ender Links.

Ender Links are assigned a network using a Channel, using an 8-digit hexadecimal color code (RBGA format) and optionally
a Description (as a text string). Every Channel has two forms: a Public form (accessible to everyone on the server), and
a Private form (accessible only to the player who placed the Link cover). All Links of the same type on the same channel
then become linked, allowing items, fluids, or redstone signals to be imported or exported between them.

After being assigned a channel, Ender Links have the same input/output and filter controls as a Conveyor or Pump. A single
Ender Link cover is only one-directional though, only allowing its attached machine to Export To the ender network, or
