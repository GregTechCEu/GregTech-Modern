# ChangeLog

Version: 1.3.0-a

# **1.21 IS A *VERY* BREAKING UPDATE, DO NOT TRY TO UPDATE FROM 1.20.1 OR 1.19.2**

### ADDITIONS:
- Added the subtitle language values for machine/tool sounds
- Added various missing, commented out recipes back
- Added the ability to disable ore processing categories per material

### CHANGES:
- Updated mod compatibilities now that the dependencies exist
- Changed RedGranite material's id to actually be `red_granite`
- updated to latest KJS 1.21 changes
- Made pollution more bearable
- Made pollution shrink away slowly (speed is configurable)
- Made pollution spawn a fixed amount of particles instead of a massive amount
- Made pollution change the grass/leaf/water color of the area it's in
- Made singleblock generators produce a small amount of pollution
- Made air ducts more sensible to use
- Made all medical conditions be unconditionally removed on death
- Made normal naquadah not radioactive
- Made higher-tier air scrubbers have a higher radius
- Updated ja_jp & zh_cn language files

### FIXES:
- Fixed tools not taking durability damage when using
- Fixed fluid stack networking crash that sometimes could happen
- Fixed JEI crashing when loading a world
- Fixed EMI spamming the log when loading a world
- Fixed addons' material registries being stuck frozen
- Fixed infinite loop in fluid tank filling via automation
- Fixed EMI/JEI/REI not loading GT's pages
- Fixed missing null checks on create compatibility machines sometimes causing crashes
- Fixed fluid parallel logic
- Fixed GT recipe removal filtering
- Fixed pipe frame material not loading correctly when a world is loaded
- Fixed custom veins not generating when no biomes are set
- Fixed "%s B" and "%s mB" being inverted in the language values
- Fixed unsafe set operation in recipe logic that could cause crashes
- Fixed removal of vanilla GT ore veins not working correctly on singleplayer
- Fixed xaero's minimap spamming the log when it's rendering a material block
