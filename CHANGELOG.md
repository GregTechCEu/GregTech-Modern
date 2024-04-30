# ChangeLog

Version: 1.2.1

### ADDITIONS:
- Added filtering to Output Hatches
- Added several utility functions to KJS
- Added more ore generator types (Classic & Cuboid)
- Added partial NBT support for recipe inputs
- Added Wood Cutter recipes

### CHANGES:
- Reworked Bedrock Ores
  - Note that it is now entirely up to pack developers to register Bedrock Ore Veins!
- Added certain fluid blocks to the minecraft:replaceable tag
- Updated Russian translation

### FIXES:
- Fixed some Multiblock builders not working in KJS
- Fixed Turbine Rotors not being damaged
- Fixed Shutter Covers not being registered
- Fixed missing Multiblock Tank recipes
- Fixed GCyM Multiblocks using Perfect Overclocking
- Fixed crash related to the Network Switch Multiblock
- Fixed Data Bank chaining
- Fixed HPCA energy consumption for energy that wasn't input recently
- Fixed Kinetic Output Boxes never stopping
- Fixed World Accelerators using too much energy and always accelerating Block Entities
- Fixed connectivity for Long Distance Pipes
- Fixed Item Collector (now works after reload, no longer requires KJS, fixed tooltip)
- Fixed Research items not rendering in JEI when playing on a server
- Fixed recipes randomly not working after server restarts
- Fixed recipes for ABS & Large Electrolyzer using the wrong wires
- Fixed sounds for Arc Furnace & Boilers not being registered properly
- Fixed Parallel Control Hatch behavior when the recipe voltage matches the machine voltage
- Fixed Wrench interactions in certain situations
- Fixed recipe for Treated Wood
- Fixed KJS Research recipes not registering
- Fixed machines and pipes causing excessive updates to neighboring blocks
- Fixed language entry for Iron III Chloride
- Fixed recycling recipe for Titanium Fluid Cells
- Fixed recipe for Engine Intake Casing
- Fixed any custom recipe capabilities being removed when KJS is installed