---
title: Dimension Icons
---

Dimension icons are the icons shown when the `dimension` recipe condition, as well as in recipe viewers to show which dimensions an ore vein can be generated in.

New ones can be made and existing ones can be modified both in `Java` and `KubeJS`.

To make or modify a dimension icon in `Java` you will need to add a listener to the mod's constructor.

`modEventBus.addGenericListener(DimensionMarker.class, this::registerDimensionMarkers);`

Below is an example of making new and modifying existing dimension icons in a Java addon.

```java
private void registerDimensionMarkers(GTCEuAPI.RegisterEvent<ResourceLocation, DimensionMarker> event) {
    // Making a new icon.
    ResourceLocation sceneDimKey = new ResourceLocation(Phantasia.MOD_ID, "scene");
    DimensionMarker sceneMarker = new DimensionMarker(
            3, // Tier
            () -> Items.DIAMOND_BLOCK, // Supplier for the actual Icon ItemStack.
            "mymod.dimension.example_dimension.name" // Lang key can also be a normal text block.
    );
    event.register(sceneDimKey, sceneMarker);
    
    // Editing an existing icon.
    ResourceLocation netherKey = new ResourceLocation("minecraft", "the_nether");
    DimensionMarker upgradedNetherMarker = new DimensionMarker(
            5, 
            () -> Items.NETHERITE_BLOCK,
            "text.mymod.super_nether"
    );
    GTRegistries.DIMENSION_MARKERS.registerOrOverride(netherKey, upgradedNetherMarker);
}
```

`Dimension Icons` also can be made/edited using `KubeJS` scripts using similar syntax.

```js
GTCEuStartupEvents.registry("gtceu:dimension_marker", event => {
    // Edit existing dimension icon,
    const DimensionMarker = Java.loadClass('com.gregtechceu.gtceu.api.data.DimensionMarker')
    
    let netherKey = new ResourceLocation("minecraft", "the_nether")

    let upgradedNetherMarker = new DimensionMarker(
        5, 
        Items.NETHERITE_BLOCK, 
        "text.mymod.super_nether" 
    )

    GTRegistries.DIMENSION_MARKERS.registerOrOverride(netherKey, upgradedNetherMarker)
    
    // New icon
    event.create("ad_astra:glacio")
        .iconSupplier(() => Item.of("ad_astra:glacio_globe").getItem())
        .tier(0)
        .overrideName("Glacio")
})
```
