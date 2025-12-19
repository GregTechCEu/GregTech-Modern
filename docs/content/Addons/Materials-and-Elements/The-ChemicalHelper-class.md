---
title: ChemicalHelper
---


# The `ChemicalHelper` Class

The ChemicalHelper class contains a number of useful methods that can get a Material, TagPrefix, ItemStack, and other objects from GTM's registries in contexts where it might be unsafe or not possible

## Available functions

The following functions are available for use by addon devs:

### `.getMaterialStack()`

Can take almost any form of item reference (`Item`, `ItemStack`, `Ingredient` and so on) and will return the
`MaterialStack` entry associated with it. If there is no associated `Material`, the method returns `null`.

### `.getMaterial()`

Returns a `Material` from its related `Fluid`. If the fluid has no associated Material, the method returns `null`.

### `.getPrefix()`

Takes an item reference as input and returns the associated `TagPrefix`. If there is no associated `TagPrefix`,
the method will return `null`.


### `.getIngot()` and `.getDust()`

These two methods take two parameters each as input: a `Material`, and a `long` number representing a material amount.
The method returns an `ItemStack` of the respective `Material`'s dust or ingot, if it has one.

The material amount is usually very large; it is generally an integer multiple or fraction of the predefined value
`GTValues.M`, which is the commonly agreed-upon material amount of one (1) ingot or regular dust.

Depending on the amount passed, the functions will return different items:

- `.getIngot()`, for example, will return an ItemStack of a block or nugget of the associated Material if
  the passed amount is large or small enough.
- `.getDust()`, in a similar fashion, will return an ItemStack of regular, small or tiny dust depending on the
  material amount passed.


### `.getTag()`, `.getBlockTag()`, `.getTags()`, and `.getBlockTags()`

Takes a `TagPrefix` and a non-null `Material` as input and returns the first item or block tag
(or a Java array of all item or block tags if the plural functions are used) possessed by the item represented by
that `TagPrefix`-`Material` combination.


### `.get()`

Takes a `TagPrefix`, a `Material` and an optional item count that otherwise defaults to 1, and returns an
ItemStack of the `TagPrefix`-`Material` combination with the specified item count.


## Usage Examples

```java title="ChemicalHelperExamples.java"
var ironMaterial = ChemicalHelper.getMaterial(doublePlate, Iron); // (1)
var rawOrePrefix = ChemicalHelper.getPrefix(rawOre, Platinum);// (2)
var cobaltIngotStack = ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Cobalt, 32); // (3)

var goldNugget = ChemicalHelper.getIngot(GTMaterials.Gold, GTValues.M / 9); // (4)
var steelBlock = ChemicalHelper.getIngot(GTMaterials.Steel, GTValues.M * 9);

var ashSmallDust = ChemicalHelper.getDust(GTMaterials.Ash, GTValues.M / 4); // (5)
```

1. `ironMaterial` is a reference to `GTMaterials.Iron`.
2. `rawOrePrefix` is a reference to `TagPrefix.rawOre`.
3. `cobaltIngotStack` is an `ItemStack` of 32 Cobalt Ingots.
4. `goldNugget` is an `ItemStack` representing one Gold Nugget.
5. `ashSmallDust` is an `ItemStack` representing a Small Pile of Ash.
