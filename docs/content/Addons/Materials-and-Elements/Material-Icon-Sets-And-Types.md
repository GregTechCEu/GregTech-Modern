---
title: Icon Sets and Icon Types
---


# Icon Sets

The material system uses icon sets to determine the textures of generated blocks and items. Each icon set contains an icon type which is the texture applied for a specific item. For example, the denes plate for a custom material could have the `METALLIC` icon set, and since its TagPrefix is `plateDense`, it would have the `plateDense` icon set applied to it. The path for the texture applied would be `assets/gtceu/item/material_sets/metallic/plate_dense.png`

## Available Icon Sets

The following icon sets are available by default:

- `MaterialIconSet.DULL`
- `MaterialIconSet.METALLIC`
- `MaterialIconSet.MAGNETIC`
- `MaterialIconSet.SHINY`
- `MaterialIconSet.BRIGHT`
- `MaterialIconSet.DIAMOND`
- `MaterialIconSet.EMERALD`
- `MaterialIconSet.GAS`
- `MaterialIconSet.GEM_HORIZONTAL`
- `MaterialIconSet.GEM_VERTICAL`
- `MaterialIconSet.RUBY`
- `MaterialIconSet.OPAL`
- `MaterialIconSet.GLASS`
- `MaterialIconSet.NETHERSTAR`
- `MaterialIconSet.FINE`
- `MaterialIconSet.SAND`
- `MaterialIconSet.WOOD`
- `MaterialIconSet.ROUGH`
- `MaterialIconSet.FLINT`
- `MaterialIconSet.LIGNITE`
- `MaterialIconSet.QUARTZ`
- `MaterialIconSet.CERTUS`
- `MaterialIconSet.LAPIS`
- `MaterialIconSet.FLUID`
- `MaterialIconSet.RADIOACTIVE`

## Custom Icon Sets

Custom iconsets can be specified as well:

```java title="ModIconSets.java"
public static final MaterialIconType MY_ICON_SET = new MaterialIconType("my_icon_set");
```


# Icon Types

Icon types are the textures used for TagPrefix item such as dusts, plates, rods, etc. Each icon type belongs to an icon set.

## Available Icon Types

The following icon types are available by default:

- `MaterialIconType.dustSmall`
- `MaterialIconType.dust`
- `MaterialIconType.dustImpure`
- `MaterialIconType.dustPure`
- `MaterialIconType.rawOre`
- `MaterialIconType.rawOreBlock`
- `MaterialIconType.crushed`
- `MaterialIconType.crushedPurified`
- `MaterialIconType.crushedRefined`
- `MaterialIconType.gem`
- `MaterialIconType.gemChipped`
- `MaterialIconType.gemFlawed`
- `MaterialIconType.gemFlawless`
- `MaterialIconType.gemExquisite`
- `MaterialIconType.nugget`
- `MaterialIconType.ingot`
- `MaterialIconType.ingotHot`
- `MaterialIconType.ingotDouble`
- `MaterialIconType.ingotTriple`
- `MaterialIconType.ingotQuadruple`
- `MaterialIconType.ingotQuintuple`
- `MaterialIconType.plate`
- `MaterialIconType.plateDouble`
- `MaterialIconType.plateTriple`
- `MaterialIconType.plateQuadruple`
- `MaterialIconType.plateQuintuple`
- `MaterialIconType.plateDense`
- `MaterialIconType.stick`
- `MaterialIconType.lens`
- `MaterialIconType.round`
- `MaterialIconType.bolt`
- `MaterialIconType.screw`
- `MaterialIconType.ring`
- `MaterialIconType.wireFine`
- `MaterialIconType.gearSmall`
- `MaterialIconType.rotor`
- `MaterialIconType.stickLong`
- `MaterialIconType.springSmall`
- `MaterialIconType.spring`
- `MaterialIconType.gear`
- `MaterialIconType.foil`
- `MaterialIconType.toolHeadSword`
- `MaterialIconType.toolHeadPickaxe`
- `MaterialIconType.toolHeadShovel`
- `MaterialIconType.toolHeadAxe`
- `MaterialIconType.toolHeadHoe`
- `MaterialIconType.toolHeadHammer`
- `MaterialIconType.toolHeadFile`
- `MaterialIconType.toolHeadSaw`
- `MaterialIconType.toolHeadBuzzSaw`
- `MaterialIconType.toolHeadDrill`
- `MaterialIconType.toolHeadChainsaw`
- `MaterialIconType.toolHeadScythe`
- `MaterialIconType.toolHeadScrewdriver`
- `MaterialIconType.toolHeadWrench`
- `MaterialIconType.toolHeadWireCutter`
- `MaterialIconType.turbineBlade`
- `MaterialIconType.liquid`
- `MaterialIconType.gas`
- `MaterialIconType.plasma`
- `MaterialIconType.molten`
- `MaterialIconType.block`
- `MaterialIconType.ore`
- `MaterialIconType.oreSmall`
- `MaterialIconType.frameGt`
- `MaterialIconType.wire`


## Custom Icon Sets

Custom iconsets can be specified as well:

```java title="ModIconSets.java"
public static final MaterialIconSet MY_ICON_SET = new MaterialIconSet("my_icon_set", METALLIC);
```

`MaterialIconSet()` can have the following additional parameters:
- `MaterialIconSet parentIconset` -> specify an icon set to inherit missing textures from. Useful if you want to only add a couple of icon types to an icon set, and use preexisting icon types for the rest of the set. All of GregTech's material icon sets inherit another icon set, excluding `DULL`, `METALLIC`, `FINE`, and `FLUID`
- `boolean isRootIconset` -> specify whether this icon set is a root icon set ie it has no parent icon sets. Any missing textures will not inherit a texture from the parent icon set, and will be a null texture.