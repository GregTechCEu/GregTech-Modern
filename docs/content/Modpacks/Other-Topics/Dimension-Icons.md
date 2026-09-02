---
title: Dimension Icons
---

Dimension icons are the icons shown when the `dimension` recipe condition, as well as in recipe viewers to show which dimensions an ore vein can be generated in.

New ones can be made and existing ones can be modified both in `Java` and `KubeJS`.

To make or modify a dimension icon in `Java` you will need to add a listener to the mod's constructor.

`modEventBus.addGenericListener(DimensionMarker.class, this::registerDimensionMarkers);`

Below is an example of making new and modifying existing dimension icons in a Java addon.

```java
@SubscribeEvent
public void registerDimensionMarkers(RegisterEvent event) {
    if (event.getRegistryKey() != GTRegistries.Keys.DIMENSION_MARKER) return;

    // Making a new icon.
    DimensionMarker sceneMarker = new DimensionMarker(
            3, // Tier
            () -> Items.DIAMOND_BLOCK, // Supplier for the actual Icon ItemStack.
            "mymod.dimension.example_dimension.name" // Lang key can also be a normal text block.
    );
    event.register(GTRegistries.Keys.DIMENSION_MARKER, ResourceLocation.fromNamespaceAndPath(Phantasia.MOD_ID, "scene"), () -> sceneMarker);

    // Editing an existing icon.

    DimensionMarker marker = GTRegistries.DIMENSION_MARKERS.get(ResourceLocation.withDefaultNamespace("the_nether"));
    marker.setTier(5);
    marker.setOverrideName("text.mymod.super_nether");
    marker.setIcon(() -> Items.NETHERITE_BLOCK);
} 
```

`Dimension Icons` also can be made/edited using `KubeJS` scripts using similar syntax.

```js
GTCEuStartupEvents.registry("gtceu:dimension_marker", event => {
    // Edit existing dimension icon,
    let marker = DimensionMarker.get(ResourceLocation.withDefaultNamespace("the_nether"));
    marker.setTier(5);
    marker.setOverrideName("text.mymod.super_nether");
    marker.setIcon(() => Items.NETHERITE_BLOCK);
    
    // New icon
    event.create("ad_astra:glacio")
        .iconSupplier(() => Item.of("ad_astra:glacio_globe").getItem())
        .tier(0)
        .overrideName("Glacio")
})
```
