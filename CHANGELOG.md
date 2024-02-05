# ChangeLog

Version: 1.1.3

### ADDITIONS:
- in-world multiblock preview
- all machines now have an overclocking logic by default
- added a way to change a ToolProperty's types
- added a way for tools to have "subtypes", eg LV wrench -> wrench
- made KJS recipes able to have subrecipes, like DT -> distillery
- added `setMaterialSecondaryARGB` for changing a material's secondary color

### FIXES:
- fixed cover placement on pipes picking a random face
- fixed custom fluid textures not working
- fixed power being transfered through nonconnected pipes
- fixed the planks TagPrefix
- fixed rubber logs not being burnable
- fixed crash when registering fluids
- fixed steam miner venting every tick, and not consuming any steam
- fixed machine UI layout sometimes being offcenter
- fixed electric tools getting -1/-1 energy when changing the tool head

### CHANGES:
- deprecated the PA for removal in a future update.
- made recipe lookup be 500-900% faster!
- combined cover configuration UI and output configuration UI into one