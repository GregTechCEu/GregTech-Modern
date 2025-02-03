# Filters

# Modes
Once a filter is placed on a machine or pipe end they gain 2 buttons inside of them, the filter insert/extract modes and Manual I/O modes.
## Insert/Extract
Filter extract mode applies the filter to contents exiting the side, e.g. putting it on the end of a pipe facing into a chest, with whitelist enabled, it will only output the filter contents to the chest.

Filter insert mode applies to contents entering a side, e.g. a pipe is placed on the side of a machine, the machine auto outputs to the pipe, and the pipe has a filter on the side of it which is touching the machine (filter is not on the machine), the filter will allow the machine to auto output into it if the filter contents match the machine auto output.

If not caring for directional filtering and just want to block something through passing a point at all, use filter Insert/extract to apply to both directions.

## Manual I/O modes
placeholder (I don't know how they work)
# Mechanics
## Item and Fluid filter
These basic filters can store 9 slots worth of items or fluids to act as a filter for pipes, 
Can be placed inside of logistics covers on machines or pipes, machine sides, or on pipes to filter.

When used in hand normally there are 9 slots and 2 buttons, you can drag contents to these slots from recipe viewers or click the content on them (item or bucket/container with fluid right click) to add them to the filter.

The list button changes the filter from whitelist to blacklist, specifying if you want to allow or deny contents passing through.

The NBT button specifies whether you want an item or fluid to match their unique data, like durability or enchants, for fluids an example is potion fluids which are all the same id but have data that makes them unique.
## Tag filters
Tag filters do not use slots for specific contents, instead they follow a regex-like system that matches tags of items or fluids, item tag filter does not match it's blocks tags, only the item tags.

You can get a fluids tags in the tags tab when checking uses with EMI, or using /kubejs hand on a container of the fluid, with their respective mods (being EMI and KubeJS).

Note that fluids contain liquids oxygens and gases, each having their own tag e.g. '#forge:gases', also note that molten fluids do not match liquids and are instead in '#forge:molten' tag

Tags come in the form 'namespace:tag/subtype'.
The 'forge:' namespace is assumed if one isn't provided.

Expression Symbols:

- `a & b` = AND
- `a | b` = OR
- `a ^ b` = XOR
- `!a` = NOT
- `(a)` for grouping
- `*` for wildcard
- `$` for untagged (Content has no tags that match fluid block or item tag,
this allows blocks with no item tag)

Examples:

item `*dusts/gold | (gtceu:circuits &!*lv)`

This matches all gold dusts or all circuits, except LV ones

item `* | $ & !(*refined* | *purified*)`

This matches all tagged items or all untagged items and disallows any tag including 'refined', or 'purified' (such as forge:refined_ores and forge:purified_ores).

fluid `forge:*neutron* | (*molten* & !*hss*)`

This matches any fluid with forge namespace that contains 'neutron'  (such as neutronium or it's molten alloy fluids), or matches any molten fluid that isn't a high speed steel.


## Smart filter

The smart filter filters items by machine recipes, you can click the machine to cycle between electrolyzer, centrifuge, and sifter.

Electrolyzer mode allows items that can be electrolyzed, like ruby dust

Centrifuge mode allows items that can be centrifuged, such as impure dusts, purified dusts, undoing mixer dust recipes, or redstone.

Sifter mode allows items that can be sifted, like any purified ore that has matching gems



