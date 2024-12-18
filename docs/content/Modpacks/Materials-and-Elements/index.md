---
title: "Materials & Elements"
---


# Materials & Elements

GregTech has its own material system based on chemical elements.

Materials are composed of chemical elements and/or other materials.  
Each material can have different items (and blocks), such as ingots, dusts, plates, wires, ores, etc.

## A note about registration
Order matters when you are registering a new material. If you reference a material by `.components()`, you must make sure the other material(s) have been created before the current one.
