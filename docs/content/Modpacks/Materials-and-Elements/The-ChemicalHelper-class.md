---
title: ChemicalHelper
---


# The `ChemicalHelper` Class

It may behoove a packmaker working with GTCEu Modern to learn about the ChemicalHelper class.

This class, available for use in server scripts, contains a number of useful methods that can ease working with GTCEu
Materials in contexts where it might not be possible, or it might be unsafe, to work with item or block tags.


## Useful functions that ChemicalHelper offers

The following functions are available for use by packmakers:


### `.getMaterial()`

Can take almost any form of item reference (`Item`, `ItemStack`, `Ingredient` and so on) and will return the
`Material` entry associated with it. If there is no associated Material, the method returns `null`.
A `Fluid` may also be passed as input.


### `.getPrefix()`

Takes an item reference as input and will return the TagPrefix it is associated with it. If there is
non associated, the method will return `null`.


### `.getIngot()` / `.getDust()`

These two methods take two parameters each as input: a `Material`, and a number representing a material amount,
and will return an ItemStack representation of the respective Material's dust or ingot form, if it has one.

The material amount is usually very large; it is generally an integer multiple or fraction of the predefined value
`GTValues.M`, which is the commonly agreed-upon material amount of one (1) ingot or regular dust.

Depending on the amount passed, the functions will return different items:

- `.getIngot()`, for example, will return an ItemStack representation of a block or nugget of the associated Material if
  the passed amount is large or small enough.
- `.getDust()`, in similar fashion, will return regular, small or tiny dust ItemStack representations depending on the
  material amount passed.


### `.getTag()` / `.getBlockTag()` / `.getTags()` / `.getBlockTags()`

Takes a `TagPrefix` and a non-`null` `Material` as input and returns the first item or block tag
(or a Java array of all item or block tags if the plural functions are used) possessed by the item represented by
that `TagPrefix`-`Material` combination.


### `.get()`

Takes a `TagPrefix`, a `Material` and optionally an item count that otherwise defaults to 1, and returns an
ItemStack representing that `TagPrefix`-`Material` combination with the specified item count.


## Usage Examples

```js title="chemicalhelper_example_script.js"
var ironMaterial = ChemicalHelper.getMaterial(Item.of("gtceu:double_iron_plate").asItem()) // (1)
var rawOrePrefix = ChemicalHelper.getPrefix(Item.of("gtceu:raw_platinum").asItem()) // (2)
var cobaltIngotStack = ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Cobalt, 32) // (3)

var goldNugget = ChemicalHelper.getIngot(GTMaterials.Gold, GTValues.M / 9) // (4)
var steelBlock = ChemicalHelper.getIngot(GTMaterials.Steel, GTValues.M * 9)

var ashSmallDust = ChemicalHelper.getDust(GTMaterials.Ash, GTValues.M / 4) // (5)
```

1. `ironMaterial` is now a reference to `GTMaterials.Iron`.
2. `rawOrePrefix` is now a reference to `TagPrefix.rawOre`.
3. `cobaltIngotStack` is now an ItemStack representing half a stack of cobalt ingots.
4. `goldNugget` is now an ItemStack representing one gold nugget.
5. `ashSmallDust` is now an ItemStack representing a small ash pile.
