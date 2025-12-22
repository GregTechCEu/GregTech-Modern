---
title: TagPrefixes and The Power of .setIgnored()
---

## What is a TagPrefix?

TagPrefixes are GTCEu Modern's way of streamlining applying item and block tags to Materials, along with some other
functions. The `TagPrefix` class, available for use in startup and server scripts, contains a number of predefined
prefixes that associate potentially everything from drill heads to flawless gemstones with a material.

A common and easy-to-understand example is `TagPrefix.ingot`, which associates with all Materials that have an
IngotProperty and thus an associated ingot item, including custom ones defined by addons or KubeJS. 

TagPrefixes provide localization, item and block tagging, influence many crafting recipes, and are integral to the
functioning of GregTech's material definition system.

!!! tip "What TagPrefixes exist by default?"
    A list of all available TagPrefixes can be found in GTCEu Modern's GitHub, in the class [TagPrefix](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/api/data/tag/TagPrefix.java). You can also quickly navigate to GTM's classes in your External Libraries


## What is `.setIgnored()`?

While trawling through GTM's codebase, or simply by playing Minecraft, you may have noticed that some vanilla materials are treated differently. For example, iron ingots are added by vanilla, and GTM does not create a duplicate iron ingot, as its Material definition would suggest.

Instead, the Material entry for iron treats the vanilla iron ingot as the material's ingot, and thus produces no duplicates.

This functionality is governed by TagPrefixes, and can also be harnessed by addon developers for their own custom items, or
when writing compatibility between GregTech and other mods.


### Okay, but how do I use this?

`MaterialModificationEvent` occurs in Minecraft's boot sequence after Material registration is finalized, but before
the Material registry is closed; you won't be able to define any new Materials using it.

The following calls are available for each TagPrefix:

- `.setIgnored()` with one input parameter:  
  Takes a `Material` as input and prevents GTCEu from associating that specific TagPrefix with that Material.
- `.setIgnored()` with two input parameters:  
  Takes a `Material` and an `Item` or `Block` (or any class that that implements the `ItemLike` interface) as input;
  causes GTCEu to treat the passed `ItemLike` as whatever item the TagPrefix would have originally generated for the
  Material. An `ItemLike...` varargs in the form of a JS array may also be passed to perform the action on multiple
  blocks and/or items at once.
- `.removeIgnored()`:  
  Takes a `Material` as input and re-enables generation of the item associated with the TagPrefix for that material.

A more illustrative example, using some Applied Energistics 2 items:

```java title="SetIgnoredExamples.java"
TagPrefix.gemChipped.setIgnored(ChemicalHelper.getMaterial("fluix_crystal")); // (1)
TagPrefix.rock.setIgnored(ChemicalHelper.getMaterial("sky_stone"), AEBlocks.SKY_STONE_BLOCK); // (2)
TagPrefix.ingot.removeIgnored(GTMaterials.Iron); // (3)
TagPrefix.dust.setIgnored(GTMaterials.Blaze, Items.BLAZE_POWDER); // (4)
```

1. Prevents a chipped gem variant for `fluix_crystal` from being generated.
2. Sets AE2's Sky Stone block as the rock for the `sky_stone` material (similar to how vanilla stone block is associated with the `stone` material).
3. Removes vanilla iron ingots from the `iron` material causing it to generate a duplicate iron ingot.
4. Sets blaze powder as the dust for the `blaze` material

## Creating custom TagPrefixes

<!--TODO-->

Custom TagPrefixes can also be created by addon devs. Once created, custom TagPrefixes must be registered via `IGTAddon#registerTagPrefixes`. 

```java title="ModTagPrefixes.java"
public static VeryLongRod = new TagPrefix("very_long_rod")
    .idPattern("very_long_%s_rod")
    .defaultTagPath("very_long_rod")
    .materialIconType(ModMaterialIconTypes.very_long_rod)
    .unificationEnabled(true)
    .generateItem(true)
    .generationCondition(material -> material.hasProperty(PropertyKey.LONG_ROD));
```

All TagPrefixes added by GTM and their definitions can be viewed in the [TagPrefix class](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/api/data/tag/TagPrefix.java)