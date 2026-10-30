---
title: Icon Sets
---


# Icon Sets

The material system uses icon sets to determine the textures of generated blocks and items.


## Available Icon Sets

The following icon sets are available by default:

- `MaterialIconSet.BRIGHT`
- `MaterialIconSet.CERTUS`
- `MaterialIconSet.DIAMOND`
- `MaterialIconSet.DULL`
- `MaterialIconSet.EMERALD`
- `MaterialIconSet.FINE`
- `MaterialIconSet.FLINT`
- `MaterialIconSet.FLUID`
- `MaterialIconSet.GAS`
- `MaterialIconSet.GEM_HORIZONTAL`
- `MaterialIconSet.GEM_VERTICAL`
- `MaterialIconSet.GLASS`
- `MaterialIconSet.LAPIS`
- `MaterialIconSet.LIGNITE`
- `MaterialIconSet.MAGNETIC`
- `MaterialIconSet.METALLIC`
- `MaterialIconSet.NETHERSTAR`
- `MaterialIconSet.OPAL`
- `MaterialIconSet.QUARTZ`
- `MaterialIconSet.ROUGH`
- `MaterialIconSet.RUBY`
- `MaterialIconSet.SAND`
- `MaterialIconSet.SHINY`
- `MaterialIconSet.WOOD`


## Custom Icon Sets

Custom iconsets can be specified as well, using the `gtceu:matieral_icon_set` event:
=== "Java"
    ```java title="AddonIconSets.java"
    public static final MaterialIconSet STARRY = new MaterialIconSet(ADDON_MOD.id("starry"), MaterialIconSet.SHINY);
    ```
=== "JavaScript"
    ```js title="custom_iconsets.js"
    GTCEuStartupEvents.registry('gtceu:material_icon_set', event => {
        event.create('starry')
            .parent('shiny')
    })
    ```
