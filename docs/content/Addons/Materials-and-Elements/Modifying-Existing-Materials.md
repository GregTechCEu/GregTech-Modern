---
title: Modifying Existing Materials
---


# Modifying Existing Materials
GregTech CEu Modern adds a lot of methods available to developers to modify materials. Materials can have their properties, flags, and other attributes modified or added to them.

Note that all material modification must be done during `PostMaterialEvent`.

```java title="MaterialModificationExamples.java"
@SubscribeEvent
public static void modifyMaterials(PostMaterialEvent event) {
    // Ingot
    GTMaterials.Zirconium.setProperty(PropertyKey.INGOT, new IngotProperty());
    GTMaterials.Obsidian.setProperty(PropertyKey.INGOT, new IngotProperty());

    // Dust
    GTMaterials.Selenium.setProperty(PropertyKey.DUST, new DustProperty());

    // Blast Property
    GTMaterials.Zirconium.setProperty(PropertyKey.BLAST,new BlastProperty(8000, GasTier.HIGHER, GTValues.VA(GTValues.MV), 8000));
}
```

Adding fluids to existing materials requires a bit of work with the new `FluidStorage` system

```java title="MaterialModificationExamples.java"
Coke.setProperty(PropertyKey.FLUID, new FluidProperty(FluidStorageKeys.GAS, // (1)
    new FluidBuilder().block())); // (2)
``` 

1. Can be `LIQUID`, `GAS`, `PLASMA` or `MOLTEN`
2. Adds a fluid for Coke that is a gas, and can be placed in world with a bucket

You can even add an ore to existing materials:

```java title="MaterialModificationExamples.java"
// Zinc Ore
GTMaterials.Zinc.setProperty(PropertyKey.ORE, new OreProperty());
```

Flags can also be added to existing materials:

```java title="MaterialModificationExamples.java"
GTMaterials.Lead.addFlags(GTMaterialFlags.GENERATE_GEAR); // This is for materials already in GTCEu
GTMaterials.get('custom_material_name').addFlags(GTMaterialFlags.GENERATE_FOIL); // This only works for materials added by GTCEu addons
```

Colors of pre-existing materials can also be modified:


```java title="MaterialModificationExamples.java"
GTMaterials.BismuthBronze.setMaterialARGB(0x82AD92)
```