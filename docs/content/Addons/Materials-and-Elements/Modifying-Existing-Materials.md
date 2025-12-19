---
title: Modifying Existing Materials
---


# Modifying Existing Materials
The properties for existing Materials can be modified

```java title="MaterialModificationExamples.java"
        // Ingot
        GTMaterials.Zirconium.setProperty(PropertyKey.INGOT, new IngotProperty());
        GTMaterials.Obsidian.setProperty(PropertyKey.INGOT, new IngotProperty());

        // Dust
        GTMaterials.Selenium.setProperty(PropertyKey.DUST, new DustProperty());

        // Blast Property
        GTMaterials.Zirconium.setProperty(PropertyKey.BLAST, new BlastProperty(8000, GasTier.HIGHER, GTValues.VA(GTValues.MV), 8000));
```

Adding fluids to existing materials requires a bit of work with the new FluidStorage system

```java title="MaterialModificationExamples.java"
    Coke.setProperty(PropertyKey.FLUID, new FluidProperty(FluidStorageKeys.GAS /* (1) */, new FluidBuilder().block())); // (2)
``` 

1. Can be `LIQUID`, `GAS`, `PLASMA` or `MOLTEN`
2. Adds a fluid for Coke that is a gas, and can be placed in world with a bucket

You can even add an ore to existing materials:

```js title="ore_property.js"
    GTCEuStartupEvents.registry('gtceu:material', event => {

    const $OreProperty = Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty');

        // Zinc Ore
        GTMaterials.Zinc.setProperty(PropertyKey.ORE, new $OreProperty());
        
    });
```

You can also add flags to existing materials:

```js title="flags.js"
    GTCEuStartupEvents.registry('gtceu:material', event => {

        GTMaterials.Lead.addFlags(GTMaterialFlags.GENERATE_GEAR); // This is for materials already in GTCEU
        GTMaterials.get('custom_material_name').addFlags(GTMaterialFlags.GENERATE_FOIL); // This only works for materials added by GTCEU addons
        
    });
```

Editing the color of an existing material:


```js title="material_modification.js"
    GTCEuStartupEvents.materialModification(event => {
        GTMaterials.BismuthBronze.setMaterialARGB(0x82AD92) //(1)
    })
```

1. Most methods in the [``Material`` class](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/api/data/chemical/material/Material.java) can be used in the ``materialModification`` event