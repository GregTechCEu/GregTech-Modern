# ChangeLog

Version: 1.3.0

# **1.21 IS A *VERY* BREAKING UPDATE, DO NOT TRY TO UPDATE FROM 1.20.1 OR 1.19.2**

### ADDITIONS:
- Added pollution
  - If you don't want to deal with it, pipe it elsewhere with duct pipes (or disable it.)
- Added a generic medical condition system used for pollution, hazardous materials and radioactive areas for when fission is implemented.
- Made Active Transformers explode if broken whilst active (configurable) via `harmlessActiveTransformers`
- Added a config option to disable ore processing diagrams for packs that change ore processing a lot
- Pipes moving hot fluids now damage you if you stand on them as they should
- Added the nano saber, an electric sword
- Added a config toggle for Compass, as it's unfinished.
- Added extended multiblock rotation for making your machines face sideways, or be mirrored
- Made it possible to place frame boxes on pipes for negating damage
- Made frame boxes climbable

### CHANGES:
- Made the hazard system (as well as its API) better to play with and use.
- Added early game ways to reduce/remove the effects of hazards via face masks and rubber gloves
- Made veined ore veins like copper and iron thicker and denser
- updated chinese, russian & japanese translations
- Made the ores always drop a consistent amount of raw ore and moved the multiplier to ore the processing step
- Empty tanks in a fluid pipe are now hidden in Jade
- Changed all mentions of Liters to millibuckets for consistency.
- Made multiblock autobuild able to use containers in your inventory like shulkerboxes and correctly programmed backpacks

### FIXES:
- Fixed mortar bonemeal recipe not giving as much bonemeal as was intended
- Fixed some configs not being registered properly 
- Fixed EMI AE2 autofill compat
- Fixed removed fluid/bedrock ore veins crashing the server and clearing the entire vein map
- Fixed the total computation value of recipes not showing up in the recipe viewers
- Fixed custom ore veins not showing up in recipe viewers on servers
- Fixed non-burnable logs having a coke oven recipe for charcoal
- Fixed stripped logs and wood blocks not having plank recipes as they should
- Fixed LV screwdriver and buzzsaw being marked as IV tools
- Fixed placing cables/pipes being incredibly laggy
- Fixed create compatiblity sometimes crashing servers
- Fixed parallel hatch not working sometimes
