# Baked Models

`BakedModel`s are the in-code representation of a shape with textures. They can originate from multiple sources, for example from a call to `UnbakedModel#bake` (default model loader) or `IUnbakedGeometry#bake` ([custom model loaders][modelloader]). Some [block entity renderers][ber] also make use of baked models. There is no limit to how complex a model may be.

Models are stored in the `ModelManager`, which can be accessed through `Minecraft.getInstance().modelManager`. Then, you can call `ModelManager#getModel` to get a certain model by its [`ResourceLocation`][rl] or [`ModelResourceLocation`][mrl]. Mods will basically always reuse a model that was previously automatically loaded and baked.

## Methods of `BakedModel`

### `getQuads`

The most important method of a baked model is `getQuads`. This method is responsible for returning a list of `BakedQuad`s, which can then be sent to the GPU. A quad compares to a triangle in a modeling program (and in most other games), however due to Minecraft's general focus on squares, the developers elected to use quads (4 vertices) instead of triangles (3 vertices) for rendering in Minecraft. `getQuads` has five parameters that can be used:

- A `BlockState`: The [blockstate] being rendered. May be null, indicating that an item is being rendered.
- A `Direction`: The direction of the face being culled against. May be null, which means quads that cannot be occluded should be returned.
- A `RandomSource`: A client-bound random source you can use for randomization.
- A `ModelData`: The extra model data to use. This may contain additional data from the block entity needed for rendering. Supplied by `BakedModel#getModelData`.
- A `RenderType`: The [render type][rendertype] to use for rendering the block. May be null, indicating that the quads for all render types used by this model should be returned. Otherwise, it is one of the render types returned by `BakedModel#getRenderTypes` (see below).

Models should heavily cache. This is because even though chunks are only rebuilt when a block in them changes, the computations done in this method still need to be as fast as possible and should ideally be cached heavily due to the amount of times this method will be called per chunk section (up to seven times per RenderType used by a given model * amount of RenderTypes used by the respective model * 4096 blocks per chunk section). In addition, [BERs][ber] or entity renderers may actually call this method several times per frame.

### `applyTransform` and `getTransforms`

`applyTransform` allows for applying custom logic when applying perspective transformations to the model, including returning a completely separate model. This method is added by NeoForge as a replacement for the vanilla `getTransforms()` method, which only allows you to customize the transforms themselves, but not the way they are applied. However, `applyTransform`'s default implementation defers to `getTransforms`, so if you only need custom transforms, you can also override `getTransforms` and be done with it. `applyTransforms` offers three parameters:

- An `ItemDisplayContext`: The [perspective] the model is being transformed to.
- A `PoseStack`: The pose stack used for rendering.
- A `boolean`: Whether to use modified values for left-hand rendering instead of the default right hand rendering; `true` if the rendered hand is the left hand (off hand, or main hand if left hand mode is enabled in the options)

:::note
`applyTransform` and `getTransforms` only apply to item models.
:::

### Others

Other methods in `BakedModel` that you may override and/or query include:

| Signature                                                                     | Effect                                                                                                                                                                                                                                                                                                                                                                                                      |
|-------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `TriState useAmbientOcclusion()`                                              | Whether to use [ambient occlusion][ao] or not. Accepts a `BlockState`, `RenderType` and `ModelData` parameter and returns a `TriState` which allows not only force-disabling AO but also force-enabling AO. Has two overloads that each return a `boolean` parameter and accept either only a `BlockState` or no parameters at all; both of these are deprecated for removal in favor of the first variant. |
| `boolean isGui3d()`                                                           | Whether this model renders as 3d or flat in GUI slots.                                                                                                                                                                                                                                                                                                                                                      |
| `boolean usesBlockLight()`                                                    | Whether to use 3D lighting (`true`) or flat lighting from the front (`false`) when lighting the model.                                                                                                                                                                                                                                                                                                      |
| `boolean isCustomRenderer()`                                                  | If true, skips normal rendering and calls an associated [`BlockEntityWithoutLevelRenderer`][bewlr]'s `renderByItem` method instead. If false, renders through the default renderer.                                                                                                                                                                                                                         |
| `ItemOverrides getOverrides()`                                                | Returns the [`ItemOverrides`][itemoverrides] associated with this model. This is only relevant on item models.                                                                                                                                                                                                                                                                                              |
| `ModelData getModelData(BlockAndTintGetter, BlockPos, BlockState, ModelData)` | Returns the model data to use for the model. This method is passed an existing `ModelData` that is either the result of `BlockEntity#getModelData()` if the block has an associated block entity, or `ModelData.EMPTY` if that is not the case. This method can be used for blocks that need model data, but do not have a block entity, for example for blocks with connected textures.                    |
| `TextureAtlasSprite getParticleIcon(ModelData)`                               | Returns the particle sprite to use for the model. May use the model data to use different particle sprites for different model data values. NeoForge-added, replacing the vanilla `getParticleIcon()` overload with no parameters.                                                                                                                                                                          |
| `ChunkRenderTypeSet getRenderTypes(BlockState, RandomSource, ModelData)`      | Returns a `ChunkRenderTypeSet` containing the render type(s) to use for rendering the block model. A `ChunkRenderTypeSet` is a set-backed ordered `Iterable<RenderType>`. By default falls back to [getting the render type from the model JSON][rendertype]. Only used for block models, item models use the overload below.                                                                               |
| `List<RenderType> getRenderTypes(ItemStack, boolean)`                         | Returns a `List<RenderType>` containing the render type(s) to use for rendering the item model. By default falls back to the normal model-bound render type lookup, which always yields a list with one element. Only used for item models, block models use the overload above.                                                                                                                            |

## Perspectives

Minecraft's render engine recognizes a total of 8 perspective types (9 if you include the in-code fallback) for item rendering. These are used in a model JSON's `display` block, and represented in code through the `ItemDisplayContext` enum.

| Enum value                | JSON key                  | Usage                                                                                                            |
|---------------------------|---------------------------|------------------------------------------------------------------------------------------------------------------|
| `THIRD_PERSON_RIGHT_HAND` | `"thirdperson_righthand"` | Right hand in third person (F5 view, or on other players)                                                        |
| `THIRD_PERSON_LEFT_HAND`  | `"thirdperson_lefthand"`  | Left hand in third person (F5 view, or on other players)                                                         |
| `FIRST_PERSON_RIGHT_HAND` | `"firstperson_righthand"` | Right hand in first person                                                                                       |
| `FIRST_PERSON_LEFT_HAND`  | `"firstperson_lefthand"`  | Left hand in first person                                                                                        |
| `HEAD`                    | `"head"`                  | When in a player's head armor slot (often only achievable via commands)                                          |
| `GUI`                     | `"gui"`                   | Inventories, player hotbar                                                                                       |
| `GROUND`                  | `"ground"`                | Dropped items; note that the rotation of the dropped item is handled by the dropped item renderer, not the model |
| `FIXED`                   | `"fixed"`                 | Item frames                                                                                                      |
| `NONE`                    | `"none"`                  | Fallback purposes in code, should not be used in JSON                                                            |

## `ItemOverrides`

`ItemOverrides` is a class that provides a way for baked models to process the state of an [`ItemStack`][itemstack] and return a new baked model through the `#resolve` method. `#resolve` has five parameters:

- A `BakedModel`: The original model.
- An `ItemStack`: The item stack being rendered.
- A `ClientLevel`: The level the model is being rendered in. This should only be used for querying the level, not mutating it in any way. May be null.
- A `LivingEntity`: The entity the model is rendered on. May be null, e.g. when rendering from a [block entity renderer][ber].
- An `int`: A seed for randomizing.

`ItemOverrides` also hold the model's override options as `BakedOverride`s. An object of `BakedOverride` is an in-code representation of a model's [`overrides`][overrides] block. It can be used by baked models to return different models depending on its contents. A list of all `BakedOverride`s of an `ItemOverrides` instance can be retrieved through `ItemOverrides#getOverrides()`.

## `BakedModelWrapper`

A `BakedModelWrapper` can be used to modify an already existing `BakedModel`. `BakedModelWrapper` is a subclass of `BakedModel` that accepts another `BakedModel` (the "original" model) in the constructor and by default redirects all methods to the original model. Your implementation can then override only select methods, like so:

```java
// The generic parameter may optionally be a more specific subclass of BakedModel.
// If it is, the constructor parameter must match that type.
public class MyBakedModelWrapper extends BakedModelWrapper<BakedModel> {
    // Pass the original model to super.
    public MyBakedModelWrapper(BakedModel originalModel) {
        super(originalModel);
    }
    
    // Override whatever methods you want here. You may also access originalModel if needed.
}
```

After writing your model wrapper class, you must apply the wrappers to the models it should affect. Do so in a [client-side][sides] [event handler][event] for `ModelEvent.ModifyBakingResult`:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
    // For block models
    event.getModels().computeIfPresent(
        // The model resource location of the model to modify. Get it from
        // BlockModelShaper#stateToModelLocation with the blockstate to be affected as a parameter.
        BlockModelShaper.stateToModelLocation(MyBlocksClass.EXAMPLE_BLOCK.defaultBlockState()),
        // A BiFunction with the location and the original models as parameters, returning the new model.
        (location, model) -> new MyBakedModelWrapper(model);
    );
    // For item models
    event.getModels().computeIfPresent(
        // The model resource location of the model to modify.
        new ModelResourceLocation(
            ResourceLocation.fromNamespaceAndPath("examplemod", "example_item"),
            "inventory"
        ),
        // A BiFunction with the location and the original models as parameters, returning the new model.
        (location, model) -> new MyBakedModelWrapper(model);
    );
}
```

:::warning
It is generally encouraged to use a [custom model loader][modelloader] over wrapping baked models in `ModelEvent.ModifyBakingResult` when possible. Custom model loaders can also use `BakedModelWrapper`s if needed.
:::

[ao]: https://en.wikipedia.org/wiki/Ambient_occlusion
[ber]: ../../../blockentities/ber.md
[bewlr]: ../../../blockentities/ber.md#blockentitywithoutlevelrenderer
[blockstate]: ../../../blocks/states.md
[event]: ../../../concepts/events.md
[itemoverrides]: #itemoverrides
[itemstack]: ../../../items/index.md#itemstacks
[modelloader]: modelloaders.md
[mrl]: ../../../misc/resourcelocation.md#modelresourcelocations
[overrides]: index.md#overrides
[perspective]: #perspectives
[rendertype]: index.md#render-types
[rl]: ../../../misc/resourcelocation.md
[sides]: ../../../concepts/sides.md
