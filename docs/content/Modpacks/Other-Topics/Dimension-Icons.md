---
title: Dimension Icons
---

Dimension icons are the icons shown when the `dimension` recipe condition, as well as in recipe viewers to show which dimensions an ore vein can be generated in.

New ones can be made and existing ones can be modified both in `Java` and `KubeJS`.

To make or modify a dimension icon in `Java` you will need to add a listener to the mod's constructor.

`modEventBus.addListener(this::registerDimensionMarkers);`

Below is an example of making new and modifying existing dimension icons in a Java addon.

```java
private void registerDimensionMarkers(RegisterEvent event) {
    if (event.getRegistryKey() != GTRegistries.Keys.DIMENSION_MARKER) return;
    // Making a new icon.
    ResourceLocation sceneDimKey = ResourceLocation.fromNamespaceAndPath(Phantasia.MOD_ID, "scene");
    DimensionMarker sceneMarker = new DimensionMarker(
            3, // Tier
            () -> Items.DIAMOND_BLOCK, // Supplier for the actual Icon ItemStack.
            Component.translatable("mymod.dimension.example_dimension.name") // Can also be a Component.literal.
    );
    event.register(GTRegistries.Keys.DIMENSION_MARKER, sceneDimKey, () -> sceneMarker);

    DimensionMarker netherMarker = GTRegistries.DIMENSION_MARKERS.get(ResourceLocation.withDefaultNamespace("the_nether"));
    netherMarker.setTier(5);
    netherMarker.setIcon(() -> Items.NETHERITE_BLOCK);
    netherMarker.setOverrideName(Component.translatable("text.mymod.super_nether"));
}
```

`Dimension Icons` also can be made/edited using `KubeJS` scripts using similar syntax.

```js
GTCEuStartupEvents.registry("gtceu:dimension_marker", event => {
    // Edit existing dimension icon,
    const DimensionMarker = Java.loadClass('com.gregtechceu.gtceu.api.data.DimensionMarker')
    
    let netherMarker = GTRegistries.DIMENSION_MARKERS.get(ID.mc("the_nether"));
    netherMarker.setTier(5);
    netherMarker.setIcon(() => Items.NETHERITE_BLOCK);
    netherMarker.setOverrideName(Text.translate("text.mymod.super_nether"));
    
    // New icon
    event.create("ad_astra:glacio")
        .iconSupplier(() => Item.of("ad_astra:glacio_globe").getItem())
        .tier(0)
        .overrideName("Glacio")
})
```
