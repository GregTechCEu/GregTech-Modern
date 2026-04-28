# Model Datagen

Like most JSON data, block and item models, along with their necessary blockstate files and [client items][citems], can be [datagenned][datagen]. This is all handled through the vanilla `ModelProvider`, with extensions provided by NeoForge via the `ExtendedModelTemplateBuilder`. Since the model JSON itself is similar between block and item models, the datagen code is relatively similar.

## Model Templates

Every model starts out as a `ModelTemplate`. For vanilla, the `ModelTemplate` acts as a parent to some pre-generated model file, defining the parent model, the required texture slots, and the file suffix to apply. For the NeoForge case, the `ExtendedModelTemplate` is constructed via an `ExtendedModelTemplateBuilder`, allowing the user to generate the model down to its base elements and faces, along with any NeoForge-added functionality.

A `ModelTemplate` is created by using one of the methods in `ModelTemplates` or calling the constructor. For the constructor, it takes in the optional `ResourceLocation` of the parent model relative to the `models` directory, an optional string to apply to the end of file path (e.g., for a pressed button, it is suffixed with `_pressed`), and a varargs of `TextureSlot`s that must be defined for the datagen not to crash. `TextureSlot`s are just a string that define the 'key' of a texture in the `textures` map. Each key can also have a parent `TextureSlot` that it will resolve to if no texture is specified for the specific slot. For example, `TextureSlot#PARTICLE` will first look for a defined `particle` texture, then check for a defined `texture` value, and finally checking `all`. If the slot or its parents are not defined, then a crash is thrown during data generation.

```java
// Assumes there is a texture referenced as '#base'
// Can be resolved by either specifying 'base' or 'all'
public static final TextureSlot BASE = TextureSlot.create("base", TextureSlot.ALL);

// Assume there exists some model 'examplemod:block/example_template'
public static final ModelTemplate EXAMPLE_TEMPLATE = new ModelTemplate(
    // The parent model location
    Optional.of(
        ModelLocationUtils.decorateBlockModelLocation("examplemod:example_template")
    ),
    // The suffix to apply to the end of any model that uses this template
    Optional.of("_example"),
    // All texture slots that must be defined
    // Should be as specific as possible based on what's undefined in the parent model
    TextureSlot.PARTICLE,
    BASE
);
```

The NeoForge-added `ExtendedModelTemplate` can be constructed via `ExtendedModelTemplateBuilder#builder` or `ModelTemplate#extend` for an existing vanilla template. The builder can then be resolved into the template using `#build`. The builder's methods provide full control over the construction of the model JSON:

| Method                                           | Effect                                                                                                                                                                                                                                                                                                                                                  |
|--------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `#parent(ResourceLocation parent)`                                         | Sets the parent model location relative to the `models` directory. |
| `#suffix(String suffix)`                                                   | Appends the string to the end of the model file path. |
| `#requiredTextureSlot(TextureSlot slot)`                                   | Adds a texture slot that must be defined within the `TextureMapping` for generation. |
| `#renderType(ResourceLocation renderType)`                                 | Sets the render type. Has an overload where the parameter is a `String`. For a list of valid values, see the `RenderType` class.                                                                                                                                                                                                                        |
| `transform(ItemDisplayContext type, Consumer<TransformVecBuilder> action)` | Adds a `TransformVecBuilder` that is configured via the consumer, used for setting the `display` on a model. |
| `#ambientOcclusion(boolean ambientOcclusion)`                              | Sets whether to use [ambient occlusion][ao] or not.                                                                                                                                                                                                                                                                                                     |
| `#guiLight(UnbakedModel.GuiLight light)`                                   | Sets the GUI light. May be `GuiLight.FRONT` or `GuiLight.SIDE`.                                                                                                                                                                                                                                                                                         |
| `#element(Consumer<ElementBuilder> action)`                                | Adds a new `ElementBuilder` (equivalent to adding a new [element][elements] to the model) that is configured via the consumer.                                                                                                                                                                                                 |                                                                                                                                                                                                                                                            |
| `#customLoader(Supplier customLoaderFactory, Consumer action)`            | Using the given factory, makes this model use a [custom loader][custommodelloader], and thus, a custom loader builder that is configured via the consumer. This changes the builder type, and as such may use different methods, depending on the loader's implementation. NeoForge provides a few custom loaders out of the box, see the linked article for more info (including datagen). |
| `rootTransforms(Consumer<RootTransformsBuilder> action)`                  | Configures the transforms of the model to apply before item display and block state transformations via the consumer. |

:::tip
While elaborate and complex models can be created through datagen, it is recommended to instead use modeling software such as [Blockbench][blockbench] to create more complex models and then have the exported models be used, either directly or as parents for other models.
:::

### Creating the Model Instance

Now that we have a `ModelTemplate`, we can generate the model itself by calling one of the `ModelTemplate#create*` methods. Although each create method takes in different parameters, at their core, they all take in the `ResourceLocation` representing the name of the file, a `TextureMapping` which maps a `TextureSlot` to some `ResourceLocation` relative to the `textures` directory, and the model output as a `BiConsumer<ResourceLocation, ModelInstance>`. Then, the method essentially creates the `JsonObject` used to generate the model, throwing an error if any duplicates are provided.

:::note
Calling the base `create` method does not apply the stored suffix. Only `create*` methods that takes in the block or item do so.
:::

```java
// Given some BiConsumer<ResourceLocation, ModelInstance> modelOutput
// Assume there is a DeferredBlock<Block> EXAMPLE_BLOCK
EXAMPLE_TEMPLATE.create(
    // Creates the model at 'assets/minecraft/models/block/example_block_example.json'
    EXAMPLE_BLOCK.get(),
    // Define textures in slots
    new TextureMapping()
        // "particle": "examplemod:item/example_block"
        .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(EXAMPLE_BLOCK.get()))
        // "base": "examplemod:item/example_block_base"
        .put(TextureSlot.BASE, TextureMapping.getBlockTexture(EXAMPLE_BLOCK.get(), "_base")),
    // The consumer of the generated model json
    modelOutput
);
```

Sometimes, generated models use similar model templates and naming patterns for their textures (e.g., the texture for a regular block is just the name of the block). In these cases, a `TextureModel.Provider` can be created to help remove any redundancy. The provider is effectively a functional interface that takes in some `Block` and returns a `TexturedModel` (a `ModelTemplate`/`TextureMapping` pair) to generate the model. The interface is constructed via `TexturedModel#createDefault`, which takes a function to map a `Block` to its `TextureMapping` along with the `ModelTemplate` to use. Then the model can be generated by calling `TexturedModel.Provider#create` with the `Block` to generate for.

```java
public static final TexturedModel.Provider EXAMPLE_TEMPLATE_PROVIDER = TexturedModel.createDefault(
    // Block to texture mapping
    block -> new TextureMapping()
        .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block))
        .put(TextureSlot.BASE, TextureMapping.getBlockTexture(block, "_base")),
    // The template to generate from
    EXAMPLE_TEMPLATE
);

// Given some BiConsumer<ResourceLocation, ModelInstance> modelOutput
// Assume there is a DeferredBlock<Block> EXAMPLE_BLOCK
EXAMPLE_TEMPLATE_PROVIDER.create(
    // Creates the model at 'assets/minecraft/models/block/example_block_example.json'
    EXAMPLE_BLOCK.get(),
    // The consumer of the generated model json
    modelOutput
)
```

## `ModelProvider`

Both block and item model datagen utilize generators provided by `registerModels`, named `BlockModelGenerators` and `ItemModelGenerators`, respectively. Each generator generates both the model JSON along with any additional required files (blockstate, client items). Each generator contains various helper methods which batches the construction of all the files into a single, easy-to-use method, such as `ItemModelGenerators#generateFlatItem` to create a basic `item/generated` model or `BlockModelGenerators#createTrivialCube` for a basic `block/cube_all` model.

```java
public class ExampleModelProvider extends ModelProvider {

    public ExampleModelProvider(PackOutput output) {
        // Replace "examplemod" with your own mod id.
        super(output, "examplemod");
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Generate models and associated files here
    }
}
```

And like all data providers, don't forget to register your provider to the event:

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createProvider(ExampleModelProvider::new);
}
```

### Block Model Datagen

Now, to actually generate blockstate and block model files, you can either call one of the many public methods in `BlockModelGenerators` within `ModelProvider#registerModels`, or pass in the generated files yourself to the `blockStateOutput` for blockstate files, `itemModelOutput` for non-trivial client items, and `modelOutput` for the model JSONs.

:::note
If you have an associated `BlockItem` registered for your block with no generated client item, the `ModelProvider` will automatically generate a client item using the default block model location `assets/<namespace>/models/block/<path>.json` as its model.
:::

```java
public class ExampleModelProvider extends ModelProvider {

    public ExampleModelProvider(PackOutput output) {
        // Replace "examplemod" with your own mod id.
        super(output, "examplemod");
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Placeholders, their usages should be replaced with real values. See above for how to use the model builder,
        // and below for the helpers the model builder offers.
        Block block = MyBlocksClass.EXAMPLE_BLOCK.get();

        // Create a simple block model with the same texture on each side.
        // The texture must be located at assets/<namespace>/textures/block/<path>.png, where
        // <namespace> and <path> are the block's registry name's namespace and path, respectively.
        // Used by the majority of (full) blocks, such as planks, cobblestone or bricks.
        blockModels.createTrivialCube(block);

        // Overload that accepts a `TexturedModel.Provider` to use.
        blockModels.createTrivialBlock(block, EXAMPLE_TEMPLATE_PROVIDER);

        // Block items have a model generated automatically
        // But let's assume you want to generate a different item, such as a flat item
        blockModels.registerSimpleFlatItemModel(block);

        // Adds a log block model. Requires two textures at assets/<namespace>/textures/block/<path>.png and
        // assets/<namespace>/textures/block/<path>_top.png, referencing the side and top texture, respectively.
        // Note that the block input here is limited to RotatedPillarBlock, which is the class vanilla logs use.
        blockModels.woodProvider(block).log(block);
        
        // Like WoodProvider#logWithHorizontal. Used by quartz pillars and similar blocks.
        blockModels.createRotatedPillarWithHorizontalVariant(block, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);

        // Using the `ExtendedModelTemplate` to specify the render type to use.
        blockModels.createRotatedPillarWithHorizontalVariant(block,
            TexturedModel.COLUMN_ALT.updateTemplate(template ->
                template.extend().renderType("minecraft:cutout").build()
            ),
            TexturedModel.COLUMN_HORIZONTAL_ALT.updateTemplate(template ->
                template.extend().renderType(this.mcLocation("cutout_mipped")).build()
            )
        );

        // Specifies a horizontally-rotatable block model with a side texture, a front texture, and a top texture.
        // The bottom will use the side texture as well. If you don't need the front or top texture,
        // just pass in the side texture twice. Used by e.g. furnaces and similar blocks.
        blockModels.createHorizontallyRotatedBlock(
            block,
            TexturedModel.Provider.ORIENTABLE_ONLY_TOP.updateTexture(mapping ->
                mapping.put(TextureSlot.SIDE, this.modLocation("block/example_texture_side"))
                .put(TextureSlot.FRONT, this.modLocation("block/example_texture_front"))
                .put(TextureSlot.TOP, this.modLocation("block/example_texture_top"))
            )
        );

        // Specifies a horizontally-rotatable block model that is attached to a face, e.g. for buttons.
        // Accounts for placing the block on the ground and on the ceiling, and rotates them accordingly.
        blockModels.familyWithExistingFullBlock(block).button(block);

        // Create a model to use for blockstatefiles
        ResourceLocation modelLoc = TexturedModel.CUBE.create(block, blockModels.modelOutput);

        // Basic single variant model
        blockModels.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(
                block,
                Variant.variant()
                    // Set and generate model
                    .with(VariantProperties.MODEL, modelLoc)
                    // Set rotations around the x and y axes
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    // Set a uvlock
                    .with(VariantProperties.UV_LOCK, true)
                    // Set a weight
                    .with(VariantProperties.WEIGHT, 5)
            )
        );

        // Add one or multiple models based on the block state properties
        blockModels.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(block)
                .with(
                    // Or properties for selecting on multiple properties
                    PropertyDispatch.property(BlockStateProperties.AXIS)
                    // Select the property and apply the models
                    .select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, modelLoc))
                    .select(
                        Direction.Axis.Z,
                        Variant.variant().with(VariantProperties.MODEL, modelLoc)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.Axis.X,
                        Variant.variant()
                            .with(VariantProperties.MODEL, modelLoc)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                )
        );

        // Modify simple model settings with property dispatch
        // Example rotates model depending on the horizontal rotation of the block
        blockModels.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(
                block,
                Variant.variant().with(VariantProperties.MODEL, modelLoc)
                    .with(BlockModelGenerators.createHorizontalFacingDispatch())
            )
        );

        // Generate a multipart
        blockModels.blockStateOutput.accept(
            MultiPartGenerator.multiPart(block)
                // Provide the base model
                .with(Variant.variant().with(VariantProperties.MODEL, modelLoc))
                // Add conditions for variant to appear
                .with(
                    // Add conditions to apply
                    Condition.or(
                        // Where at least one of the conditions are true
                        Condition.condition().term(BlockStateProperties.FACING, Direction.NORTH, Direction.SOUTH)
                        // Can nest as many conditions or groups as necessary
                        Condition.and(
                            Condition.condition().term(BlockStateProperties.FACING, Direction.NORTH)
                        )
                    ),
                    // Supply variant to generate
                    Variant.variant().with(VariantProperties.MODEL, modelLoc)
                )
        );
    }
}
```

## Item Model Datagen

Generating item models is considerably simpler, which is mainly due to all of the helper methods for within `ItemModelGenerators` and `ItemModelUtils` for property information. Similar to above, you can either call one of the many public methods in `ItemModelGenerators` within `ModelProvider#registerModels`, or pass in the generated files yourself to the `itemModelOutput` for non-trivial client items and `modelOutput` for the model JSONs.

```java
public class ExampleModelProvider extends ModelProvider {

    public ExampleModelProvider(PackOutput output) {
        // Replace "examplemod" with your own mod id.
        super(output, "examplemod");
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // The most common item
        // item/generated with the layer0 texture as the item name
        itemModels.generateFlatItem(MyItemsClass.EXAMPLE_ITEM.get(), ModelTemplates.FLAT_ITEM);

        // A bow-like item
        ItemModel.Unbaked bow = ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(MyItemsClass.EXAMPLE_ITEM.get()));
        ItemModel.Unbaked pullingBow0 = ItemModelUtils.plainModel(this.createFlatItemModel(MyItemsClass.EXAMPLE_ITEM.get(), "_pulling_0", ModelTemplates.BOW));
        ItemModel.Unbaked pullingBow1 = ItemModelUtils.plainModel(this.createFlatItemModel(MyItemsClass.EXAMPLE_ITEM.get(), "_pulling_1", ModelTemplates.BOW));
        ItemModel.Unbaked pullingBow2 = ItemModelUtils.plainModel(this.createFlatItemModel(MyItemsClass.EXAMPLE_ITEM.get(), "_pulling_2", ModelTemplates.BOW));
        this.itemModelOutput.accept(
            MyItemsClass.EXAMPLE_ITEM.get(),
            // Conditional model for item
            ItemModelUtils.conditional(
                // Checks if item is being used
                ItemModelUtils.isUsingItem(),
                // When true, select model based on use duration
                ItemModelUtils.rangeSelect(
                    new UseDuration(false),
                    // Scalar to apply to the thresholds
                    0.05F,
                    pullingBow0,
                    // Threshold when 0.65
                    ItemModelUtils.override(pullingBow1, 0.65F),
                    // Threshold when 0.9
                    ItemModelUtils.override(pullingBow2, 0.9F)
                ),
                // When false, use the base bow model
                bow
            )
        );
    }
}
```

[ao]: https://en.wikipedia.org/wiki/Ambient_occlusion
[blockbench]: https://www.blockbench.net
[citems]: items.md
[custommodelloader]: modelloaders.md#datagen
[datagen]: ../../index.md#data-generation
[elements]: index.md#elements
