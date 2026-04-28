
# Understanding the Model System

Models within Minecraft are simply a list of quads with attached textures. Each part of the modeling process has their own separate implementation, with the underlying model JSON deserialized into an `UnbakedModel`. In the end, each part of the pipelines takes in some `List<BakedQuad>` and properties necessary for their own pipelines. Some [block entity renderers][ber] also make use of these models. There is no limit to how complex a model may be.

Models are stored in the `ModelManager`, which can be accessed through `Minecraft.getInstance().getModelManager()`. For the item pipeline, you can get the associated [`ItemModel`][itemmodels] via `ModelManager#getItemModel` by passing in a [`ResourceLocation`][rl]. For the block state pipeline, you can get the associated `BlockStateModel` via `ModelManager.getBlockModelShaper().getBlockModel()` by passing in a `BlockState`. Mods will basically always reuse a model that was previously automatically loaded and baked.

## Common Models and Geometry

The basic model JSON (in `assets/<namespace>/models`) are deserialized into an `UnbakedModel`. The `UnbakedModel` is generally one step short of its baked output, containing some general form of the general properties. The most important thing it contains is the `UnbakedGeometry` via `UnbakedModel#geometry`, which represents the data to become `BakedQuad`s. These quads are inlined into the item and block state model by (eventually) calling `UnbakedGeometry#bake`. This commonly constructs a `QuadCollection`, which contains that list of `BakedQuad`s which can be rendered at anytime or only if a given direction is not culled. Now, a quad compares to a triangle in a modeling program (and in most other games), however due to Minecraft's general focus on squares, the developers elected to use quads (4 vertices) instead of triangles (3 vertices) for rendering in Minecraft.

The `UnbakedModel` contains information that is either used by the [block state definition][bsd], [item models][itemmodelsection], or both. For example, `useAmbientOcclusion` is used exclusively by the block state definition, `guiLight` and `transforms` are used exclusively by the item model, and `textureSlots` and `parent` are used by both.


During the baking process, every `UnbakedModel` is wrapped in a `ResolvedModel` that are obtained by the `ModelBaker` for an item or block state. As the name implies, a `ResolvedModel` is an `UnbakedModel` with all lingering references resolved. The associated data can then be obtained from the `getTop*` methods, which compute the properties and geometry from the current model and its parents. Baking the `ResolvedModel` to its `QuadCollection` is typically done here by calling `ResolvedModel#bakeTopGeometry`.

## Block State Definitions

The block state definition JSON (in `assets/<namespace>/blockstates`) is compiled and baked into a `BlockStateModel` for every `BlockState`. The process of creating the `BlockStateModel` goes like so:

- During the loading process:
    - The block state definition JSON is loaded into a `BlockStateModel.UnbakedRoot`. The root is a general shared cache system used to link a `BlockState` to some set of `BlockStateModel`s.
    - The `BlockStateModel.UnbakedRoot` loads in the `BlockStateModel.Unbaked` and gets ready to link them to their appropriate `BlockState`.
    - The `BlockStateModel.Unbaked` loads in its `BlockModelPart.Unbaked`, which is used to get the common `UnbakedModel` (or more specifically the `ResolvedModel`).
- During the baking process:
    - `BlockStateModel.UnbakedRoot#bake` is called for every `BlockState`.
    - `BlockStateModel.Unbaked#bake` is called for a given `BlockState`, creating a `BlockStateModel`.
    - `BlockModelPart.Unbaked#bake` is called for the model parts within a `BlockStateModel`, inlining the `ResolvedModel` to a `QuadCollection`, along with getting the ambient occlusion settings, the particle icon, and the render type by default.

The most important method within `BlockStateModel` is `collectParts`, which is responsible for returning a list of `BlockModelPart`s to render. Remember that every `BlockModelPart` contains its list of `BakedQuad`s, via `BlockModelPart#getQuads`, which is then uploaded to the vertex consumer and rendered. `collectParts` has four parameters:

- A `BlockAndTintGetter`: A representation of the level the `BlockState` is rendered within.
- A `BlockPos`: The position that the block is rendered at.
- A `BlockState`: The [blockstate] being rendered. May be null, indicating that an item is being rendered.
- A `RandomSource`: A client-bound random source you can use for randomization.

### Model Data

Sometimes, a `BlockStateModel` may rely on the `BlockEntity` to determine what `BlockModelPart`s to choose in `collectParts`. NeoForge provides the `ModelData` system to sync and pass data from the `BlockEntity`. To do so, a `BlockEntity` must implement `getModelData` and return the data it wants to sync. The data can then be sent to the client by calling `BlockEntity#requestModelDataUpdate`. Then, within `collectParts`, `getModelData` can be called on the `BlockAndTintGetter` with the `BlockPos` to get the data.

## Item Models

The [client item][clientitem] JSON (in `assets/<namespace>/items`) is compiled and baked into an `ItemModel` for a given `Item` to be used by the `ItemStack`. The process of creating the `ItemModel` goes like so:

- During the loading process:
    - The client item JSON is loaded into a `ClientItem`. This holds the item model and some general properties for how it should be rendered.
    - The `ClientItem` loads in the `ItemModel.Unbaked`.
- During the baking process:
    - `ItemModel.Unbaked#bake` is called for every `Item`, inlining the `ResolvedModel` to a `List<BakedQuad>`, along with some general `ModelRenderProperties` and the render type if the `Item` is a `BlockItem`.

Information about item rendering can be found in the [Manually Rendering an Item][itemmodels] section.

### Perspectives

Minecraft's render engine recognizes a total of 8 perspective types (9 if you include the in-code fallback) for item rendering. These are used in a model JSON's `display` block, and represented in code through the `ItemDisplayContext` enum. These are normally passed from the `UnbakedModel` to a `ModelRenderProperties` in the `ItemModel`, which is then applied to the `ItemStackRenderState` via `ModelRenderProperties#applyToLayer`.

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

## Modifying a Baking Result

Modifying an existing block state model or item stack model in-code can typically be done by wrapping the model in some sort of delegate. Block state models have `DelegateBlockStateModel`, while item stack models do not have an existing implementation. Your implementation can then override only select methods, like so:

```java
// For block states
public class MyDelegateBlockStateModel extends DelegateBlockStateModel {
    // Pass the original model to super.
    public MyDelegateBlockStateModel(BlockStateModel originalModel) {
        super(originalModel);
    }
    
    // Override whatever methods you want here. You may also access originalModel if needed.
}

// For item models
public class MyDelegateItemModel implements ItemModel {

    private final ItemModel originalModel;

    public MyDelegateItemModel(ItemModel originalModel) {
        this.originalModel = originalModel;
    }

    // Override whatever methods you want here. You may also access originalModel if needed.
    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed
    ) {
        this.originalModel.update(renderState, stack, resolver, displayContext, level, entity, seed);
    }
}
```

After writing your model wrapper class, you must apply the wrappers to the models it should affect. Do so in a [client-side][sides] [event handler][event] for `ModelEvent.ModifyBakingResult` on the [**mod event bus**][modbus]:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
    // For block state models
    event.getBakingResult().blockStateModels().computeIfPresent(
        // The block state of the model to modify.
        MyBlocksClass.EXAMPLE_BLOCK.get().defaultBlockState(),
        // A BiFunction with the location and the original models as parameters, returning the new model.
        (location, model) -> new MyDelegateBakedModel(model);
    );

    // For item models
    event.getBakingResult().itemStackModels().computeIfPresent(
        // The resource location the model to modify.
        // Typically the item registry name; however, can be anything due to the ITEM_MODEL component
        MyItemsClass.EXAMPLE_ITEM.getKey().location(),
        // A BiFunction with the location and the original models as parameters, returning the new model.
        (location, model) -> new MyDelegateItemModel(model);
    );
}
```

:::warning
It is generally encouraged to use a [custom model loader][modelloader] over wrapping baked models in `ModelEvent.ModifyBakingResult` when possible. Custom model loaders can also use delegate models if needed.
:::

[ao]: https://en.wikipedia.org/wiki/Ambient_occlusion
[ber]: ../../../blockentities/ber.md
[blockstate]: ../../../blocks/states.md
[bsd]: #block-state-definitions
[clientitem]: items.md
[event]: ../../../concepts/events.md
[itemmodels]: items.md#manually-rendering-an-item
[itemmodelsection]: #item-models
[livingentity]: ../../../entities/livingentity.md
[modbus]: ../../../concepts/events.md#event-buses
[modelloader]: modelloaders.md
[rl]: ../../../misc/resourcelocation.md
[perspective]: #perspectives
[rendertype]: index.md#render-types
[sides]: ../../../concepts/sides.md
