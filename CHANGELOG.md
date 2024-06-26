# ChangeLog

Version: 1.3.0

### ADDITIONS:
- Added the subtitle language values for machine/tool sounds
- Added various missing, commented out recipes back
- Added the ability to disable ore processing categories per material

### CHANGES:
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
- Fixed missing null checks on create compatibility machines sometimes causing crashes
- Fixed fluid parallel logic
- Fixed GT recipe removal filtering
- Fixed pipe frame material not loading correctly when a world is loaded
- Fixed custom veins not generating when no biomes are set
- Fixed "%s B" and "%s mB" being inverted in the language values
- Fixed unsafe set operation in recipe logic that could cause crashes
- Fixed removal of vanilla GT ore veins not working correctly on singleplayer
- Fixed xaero's minimap spamming the log when it's rendering a material block
