---
title: Material Properties
---


# Material Properties (WIP)

```js

BlastProperty.blastTemp() // (1)
BlastProperty.gasTier() // (2)
BlastProperty.durationOverride() // (3)
BlastProperty.eutOverride() // (4)
- DustProperty:
   - .dust() // (5)
- FluidPipeProperty:
   - .fluidPipeProperties() // (6)
- FluidProperty:
   - .fluid() // (7)
   - .isGas() // (8)
   - .hasBlock() 
- GemProperty:
   - .gem()
- IngotProperty:
   - .ingot() // (9)
      - .smeltInto()
      - .arcSmeltInto()
      - .magneticMaterial()
      - .macerateInto()
- OreProperty:
   - .ore() // (10)
```

1. Sets the Blast Furnace Temperature of the material. If the temperature is below 1000K recipes will be generated in the Primitive Blast Furnace. If above 1750K recipes for the Hot Ingot will be created along with the Vacuum Freezer Recipe to cool the ingot. Example: `.blastTemp(2750)`

2. Sets the Gas Tier which determins what GAS EBF recipes will be generated. Example: `.gasTier(LOW)`

3. Overrides the EBF's default behaviour for recipe durations.

4. Overrides the EBF's default behaviour for EU/t.

5. Used for creating a dust material. The haverst level and burn time can be specified in the brackets. Example: `.dust(2, 4000)`

6. Creates a fluid pipe out of the material it is added to. The possible values are: Max Fluid Temperature, Throughput, Gas Proof, Acid Proof, Cyro Proof, Plasma Proof,
 Channels. Example: `.fluidPipeProperties(9620, 850, false, false, false, false, 1)`
