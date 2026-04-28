# Model Datagen

Like most JSON data, block and item models can be [datagenned][datagen]. Since some things are common between item and block models, so is some of the datagen code.

## Model Datagen Classes

### `ModelBuilder`

Every model starts out as a `ModelBuilder` of some sort - usually a `BlockModelBuilder` or an `ItemModelBuilder`, depending on what you are generating. It contains all the properties of the model: its parent, its textures, its elements, its transforms, its loader, etc. Each of the properties can be set by a method:

| Method                                           | Effect                                                                                                                                                                                                                                                                                                                                                  |
|--------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `#texture(String key, ResourceLocation texture)` | Adds a texture variable with the given key and the given texture location. Has an overload where the second parameter is a `String`.                                                                                                                                                                                                                    |
| `#renderType(ResourceLocation renderType)`       | Sets the render type. Has an overload where the parameter is a `String`. For a list of valid values, see the `RenderType` class.                                                                                                                                                                                                                        |
| `#ao(boolean ao)`                                | Sets whether to use [ambient occlusion][ao] or not.                                                                                                                                                                                                                                                                                                     |
| `#guiLight(GuiLight light)`                      | Sets the GUI light. May be `GuiLight.FRONT` or `GuiLight.SIDE`.                                                                                                                                                                                                                                                                                         |
| `#element()`                                     | Adds a new `ElementBuilder` (equivalent to adding a new [element][elements] to the model). Returns said `ElementBuilder` for further modification.                                                                                                                                                                                                      |
| `#transforms()`                                  | Returns the builder's `TransformVecBuilder`, used for setting the `display` on a model.                                                                                                                                                                                                                                                                 |
| `#customLoader(BiFunction customLoaderFactory)`  | Using the given factory, makes this model use a [custom loader][custommodelloader], and thus, a custom loader builder. This changes the builder type, and as such may use different methods, depending on the loader's implementation. NeoForge provides a few custom loaders out of the box, see the linked article for more info (including datagen). |

:::tip
While elaborate and complex models can be created through datagen, it is recommended to instead use modeling software such as [Blockbench][blockbench] to create more complex models and then have the exported models be used, either directly or as parents for other models.
:::

### `ModelProvider`

Both block and item model datagen utilize subclasses of `ModelProvider`, named `BlockModelProvider` and `ItemModelProvider`, respectively. While item model datagen directly extends `ItemModelProvider`, block model datagen uses the `BlockStateProvider` base class, which has an internal `BlockModelProvider` that can be accessed via `BlockStateProvider#models()`. Additionally, `BlockStateProvider` also has its own internal `ItemModelProvider`, accessible via `BlockStateProvider#itemModels()`. The most important part of `ModelProvider` is the `getBuilder(String path)` method, which returns a `BlockModelBuilder` (or `ItemModelBuilder`) at the given location.

However, `ModelProvider` also contains various helper methods. The most important helper method is probably `withExistingParent(String name, ResourceLocation parent)`, which returns a new builder (via `getBuilder(name)`) and sets the given `ResourceLocation` as model parent. Two other very common helpers are `mcLoc(String name)`, which returns a `ResourceLocation` with the namespace `minecraft` and the given name as path, and `modLoc(String name)`, which does the same but with the provider's mod id (so usually your mod id) instead of `minecraft`. Furthermore, it provides various helper methods that are shortcuts for `#withExistingParent` for common things such as slabs, stairs, fences, doors, etc.

### `ModelFile`

Finally, the last important class is `ModelFile`. A `ModelFile` is an in-code representation of a model JSON on disk. `ModelFile` is an abstract class and has two inner subclasses `ExistingModelFile` and `UncheckedModelFile`. An `ExistingModelFile`'s existence is verified using an `ExistingFileHelper`, while an `UncheckedModelFile` is assumed to be existent without further checking. In addition, a `ModelBuilder` is considered to be a `ModelFile` as well.

## Block Model Datagen

Now, to actually generate blockstate and block model files, extend `BlockStateProvider` and override the `registerStatesAndModels()` method. Note that block models will always be placed in the `models/block` subfolder, but references are relative to `models` (i.e. they must always be prefixed with `block/`). In most cases, it makes sense to choose from one of the many predefined helper methods:

```java
public class MyBlockStateProvider extends BlockStateProvider {
    // Parameter values are provided by GatherDataEvent.
    public MyBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        // Replace "examplemod" with your own mod id.
        super(output, "examplemod", existingFileHelper);
    }
    
    @Override
    protected void registerStatesAndModels() {
        // Placeholders, their usages should be replaced with real values. See above for how to use the model builder,
        // and below for the helpers the model builder offers.
        ModelFile exampleModel = models().withExistingParent("example_model", this.mcLoc("block/cobblestone"));
        Block block = MyBlocksClass.EXAMPLE_BLOCK.get();
        ResourceLocation exampleTexture = modLoc("block/example_texture");
        ResourceLocation bottomTexture = modLoc("block/example_texture_bottom");
        ResourceLocation topTexture = modLoc("block/example_texture_top");
        ResourceLocation sideTexture = modLoc("block/example_texture_front");
        ResourceLocation frontTexture = modLoc("block/example_texture_front");

        // Create a simple block model with the same texture on each side.
        // The texture must be located at assets/<namespace>/textures/block/<path>.png, where
        // <namespace> and <path> are the block's registry name's namespace and path, respectively.
        // Used by the majority of (full) blocks, such as planks, cobblestone or bricks.
        simpleBlock(block);
        // Overload that accepts a model file to use.
        simpleBlock(block, exampleModel);
        // Overload that accepts one or multiple (vararg) ConfiguredModel objects.
        // See below for more info about ConfiguredModel.
        simpleBlock(block, ConfiguredModel.builder().build());
        // Adds an item model file with the block's name, parenting the given model file, for a block item to pick up.
        simpleBlockItem(block, exampleModel);
        // Shorthand for calling #simpleBlock() (model file overload) and #simpleBlockItem.
        simpleBlockWithItem(block, exampleModel);
        
        // Adds a log block model. Requires two textures at assets/<namespace>/textures/block/<path>.png and
        // assets/<namespace>/textures/block/<path>_top.png, referencing the side and top texture, respectively.
        // Note that the block input here is limited to RotatedPillarBlock, which is the class vanilla logs use.
        logBlock(block);
        // Like #logBlock, but the textures are named <path>_side.png and <path>_end.png instead of
        // <path>.png and <path>_top.png, respectively. Used by quartz pillars and similar blocks.
        // Has an overload that allow you to specify a different texture base name, that is then suffixed
        // with _side and _end as needed, an overload that allows you to specify two resource locations
        // for the side and end textures, and an overload that allows specifying side and end model files.
        axisBlock(block);
        // Variants of #logBlock and #axisBlock that additionally allow for render types to be specified.
        // Comes in string and resource location variants for the render type,
        // in all combinations with all variants of #logBlock and #axisBlock.
        logBlockWithRenderType(block, "minecraft:cutout");
        axisBlockWithRenderType(block, mcLoc("cutout_mipped"));
        
        // Specifies a horizontally-rotatable block model with a side texture, a front texture, and a top texture.
        // The bottom will use the side texture as well. If you don't need the front or top texture,
        // just pass in the side texture twice. Used by e.g. furnaces and similar blocks.
        horizontalBlock(block, sideTexture, frontTexture, topTexture);
        // Specifies a horizontally-rotatable block model with a model file that will be rotated as needed.
        // Has an overload that instead of a model file accepts a Function<BlockState, ModelFile>,
        // allowing for different rotations to use different models. Used e.g. by the stonecutter.
        horizontalBlock(block, exampleModel);
        // Specifies a horizontally-rotatable block model that is attached to a face, e.g. for buttons or levers.
        // Accounts for placing the block on the ground and on the ceiling, and rotates them accordingly.
        // Like #horizontalBlock, has an overload that accepts a Function<BlockState, ModelFile> instead.
        horizontalFaceBlock(block, exampleModel);
        // Similar to #horizontalBlock, but for blocks that are rotatable in all directions, including up and down.
        // Again, has an overload that accepts a Function<BlockState, ModelFile> instead.
        directionalBlock(block, exampleModel);
    }
}
```

Additionally, helpers for the following common block models exist in `BlockStateProvider`:

- Stairs
- Slabs
- Buttons
- Pressure Plates
- Signs
- Fences
- Fence Gates
- Walls
- Panes
- Doors
- Trapdoors

In some cases, the blockstates don't need special casing, but the models do. For this case, the `BlockModelProvider`, accessible via `BlockStateProvider#models()`, provides a few additional helpers, all of which accept a name as the first parameter and most of which are in some way related to full cubes. They will typically be used as model file parameters for e.g. `simpleBlock`. The helpers include supporting methods for the ones in `BlockStateProvider`, as well as:

- `withExistingParent`: Already mentioned before, this method returns a new model builder with the given parent. The parent must either already exist or be created before the model.
- `getExistingFile`: Performs a lookup in the model provider's `ExistingFileHelper`, returning the corresponding `ModelFile` if present and throwing an `IllegalStateException` otherwise.
- `singleTexture`: Accepts a parent and a single texture location, returning a model with the given parent, and with the texture variable `texture` set to the given texture location.
- `sideBottomTop`: Accepts a parent and three texture locations, returning a model with the given parent and the side, bottom and top textures set to the three texture locations.
- `cube`: Accepts six texture resource locations for the six sides, returning a full cube model with the six sides set to the six textures.
- `cubeAll`: Accepts a texture location, returning a full cube model with the given texture applied to all six sides. A mix between `singleTexture` and `cube`, if you will.
- `cubeTop`: Accepts two texture locations, returning a full cube model with the first texture applied to the sides and the bottom, and the second texture applied to the top.
- `cubeBottomTop`: Accepts three texture locations, returning a full cube model with the side, bottom and top textures set to the three texture locations. A mix between `cube` and `sideBottomTop`, if you will.
- `cubeColumn` and `cubeColumnHorizontal`: Accepts two texture locations, returning a "standing" or "laying" pillar cube model with the side and end textures set to the two texture locations. Used by `BlockStateProvider#logBlock`, `BlockStateProvider#axisBlock` and their variants.
- `orientable`: Accepts three texture locations, returning a cube with a "front" texture. The three texture locations are the side, front and top texture, respectively.
- `orientableVertical`: Variant of `orientable` that omits the top parameter, instead using the side parameter as well.
- `orientableWithBottom`: Variant of `orientable` that has a fourth parameter for a bottom texture between the front and top parameter.
- `crop`: Accepts a texture location, returning a crop-like model with the given texture, as used by the four vanilla crops.
- `cross`: Accepts a texture location, returning a cross model with the given texture, as used by flowers, saplings and many other foliage blocks.
- `torch`: Accepts a texture location, returning a torch model with the given texture.
- `wall_torch`: Accepts a texture location, returning a wall torch model with the given texture (wall torches are separate blocks from standing torches).
- `carpet`: Accepts a texture location, returning a carpet model with the given texture.

Finally, don't forget to register your block state provider to the event:

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput output = generator.getPackOutput();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

    // other providers here
    generator.addProvider(
            event.includeClient(),
            new MyBlockStateProvider(output, existingFileHelper)
    );
}
```

### `ConfiguredModel.Builder`

If the default helpers won't do it for you, you can also directly build model objects using a `ConfiguredModel.Builder` and then use them in a `VariantBlockStateBuilder` to build a `variants` blockstate file, or in a `MultiPartBlockStateBuilder` to build a `multipart` blockstate file:

```java
// Create a ConfiguredModel.Builder. Alternatively, you can use one of the ways demonstrated below
// (VariantBlockStateBuilder.PartialBlockstate#modelForState or MultiPartBlockStateBuilder#part) where applicable.
ConfiguredModel.Builder<?> builder = ConfiguredModel.builder()
// Use a model file. As mentioned previously, can either be an ExistingModelFile, an UncheckedModelFile,
// or some sort of ModelBuilder. See above for how to use ModelBuilder.
        .modelFile(models().withExistingParent("example_model", this.mcLoc("block/cobblestone")))
        // Set rotations around the x and y axes.
        .rotationX(90)
        .rotationY(180)
        // Set a uvlock.
        .uvlock(true)
        // Set a weight.
        .weight(5);
// Build the configured model. The return type is an array
// to account for multiple possible models in the same blockstate.
ConfiguredModel[] model = builder.build();

// Get a variant block state builder.
VariantBlockStateBuilder variantBuilder = getVariantBuilder(MyBlocksClass.EXAMPLE_BLOCK.get());
// Create a partial state and set properties on it.
VariantBlockStateBuilder.PartialBlockstate partialState = variantBuilder.partialState();
// Add one or multiple models for a partial blockstate. The models are a vararg parameter.
variantBuilder.addModels(partialState,
        // Specify at least one ConfiguredModel.Builder, as seen above. Create through #modelForState().
        partialState.modelForState()
                .modelFile(models().withExistingParent("example_variant_model", this.mcLoc("block/cobblestone")))
                .uvlock(true)
);
// Alternatively, forAllStates(Function<BlockState, ConfiguredModel[]>) creates a model for every state.
// The passed function will be called once for each possible state.
variantBuilder.forAllStates(state -> {
    // Return a ConfiguredModel depending on the state's properties.
    // For example, the following code will rotate the model depending on the horizontal rotation of the block.
    return ConfiguredModel.builder()
            .modelFile(models().withExistingParent("example_variant_model", this.mcLoc("block/cobblestone")))
            .rotationY((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot())
            .build();
});

// Get a multipart block state builder.
MultiPartBlockStateBuilder multipartBuilder = getMultipartBuilder(MyBlocksClass.EXAMPLE_BLOCK.get());
// Add a new part. Starts with .part() and ends with .end().
multipartBuilder.part()
        // Step one: Build the model. multipartBuilder.part() returns a ConfiguredModel.Builder,
        // meaning that all methods seen above can be used here as well.
        .modelFile(models().withExistingParent("example_multipart_model", mcLoc("block/cobblestone")))
        // Call .addModel(). Now that the model is built, we can proceed to step two: add the part data.
        .addModel()
        // Add a condition for the part. Requires a property
        // and at least one property value; property values are a vararg.
        .condition(BlockStateProperties.FACING, Direction.NORTH, Direction.SOUTH)
        // Set the multipart conditions to be ORed instead of the default ANDing.
        .useOr()
        // Creates a nested condition group.
        .nestedGroup()
        // Adds a condition to the nested group.
        .condition(BlockStateProperties.FACING, Direction.NORTH)
        // Sets only this condition group to be ORed instead of ANDed.
        .useOr()
        // Creates yet another nested condition group. There is no limit on how many groups can be nested.
        .nestedGroup()
        // Ends the nested condition group, returning to the owning part builder or condition group level.
        // Called twice here since we currently have two nested groups.
        .endNestedGroup()
        .endNestedGroup()
        // End the part builder and add the resulting part to the multipart builder.
        .end();
```

## Item Model Datagen

Generating item models is considerably simpler, which is mainly due to the fact that we operate directly on an `ItemModelProvider` instead of using an intermediate class like `BlockStateProvider`, which is of course because item models don't have an equivalent to blockstate files and are instead used directly.

Similar to above, we create a class and have it extend the base provider, in this case `ItemModelProvider`. Since we are directly in a subclass of `ModelProvider`, all `models()` calls become `this` (or are omitted).

```java
public class MyItemModelProvider extends ItemModelProvider {
    public MyItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, "examplemod", existingFileHelper);
    }
    
    // Assume that EXAMPLE_BLOCK_ITEM and EXAMPLE_ITEM are both DeferredItems
    @Override
    protected void registerModels() {
        // Block items generally use their corresponding block models as parent.
        withExistingParent(MyItemsClass.EXAMPLE_BLOCK_ITEM.getId().toString(), modLoc("block/example_block"));
        // Items generally use a simple parent and one texture. The most common parents are item/generated and item/handheld.
        // In this example, the item texture would be located at assets/examplemod/textures/item/example_item.png.
        // If you want a more complex model, you can use getBuilder() and then work from that, like you would with block models.
        withExistingParent(MyItemsClass.EXAMPLE_ITEM.getId().toString(), mcLoc("item/generated")).texture("layer0", "item/example_item");
        // The above line is so common that there is a shortcut for it. Note that the item registry name and the
        // texture path, relative to textures/item, must match.
        basicItem(MyItemsClass.EXAMPLE_ITEM.get());
    }
}
```

And like all data providers, don't forget to register your provider to the event:

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput output = generator.getPackOutput();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

    // other providers here
    generator.addProvider(
            event.includeClient(),
            new MyItemModelProvider(output, existingFileHelper)
    );
}
```

[ao]: https://en.wikipedia.org/wiki/Ambient_occlusion
[blockbench]: https://www.blockbench.net
[custommodelloader]: modelloaders.md#datagen
[datagen]: ../../index.md#data-generation
[elements]: index.md#elements
