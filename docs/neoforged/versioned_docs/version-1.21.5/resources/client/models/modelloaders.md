# Custom Model Loaders

A model is simply a shape. It can be a cube, a collection of cubes, a collection of triangles, or any other geometrical shape (or collection of geometrical shape). For most contexts, it is not relevant how a model is defined, as everything will end up baked into a `QuadCollection` anyway. As such, NeoForge adds the ability to register custom model loaders that can transform any model you want into the baked format for the game to use.

:::note
When implementing a custom loader, all models -- whether the JSON, block state definition, or item model -- should heavily cache. For block states, even though chunks are only rebuilt when a block in them changes, they are still called up to seven times per `RenderType` used by a given model * amount of `RenderType`s used by the respective model * 4096 blocks per chunk section, with [BERs][ber] or [entity renderers][entityrenderer] potentially rendering a model several times per frame. For item models, the number of times they are rendered multiple times per frame because of the `RenderType`.
:::

## Model Loaders

The entry point for a block model remains the model JSON file. However, you can specify a `loader` field in the root of the JSON that will swap out the default loader for your own loader. A custom model loader may ignore all fields the default loader requires.

Besides the default model loader, NeoForge offers several builtin loaders, each serving a different purpose.

### Composite Model

A composite model can be used to specify different model parts in the parent and only apply some of them in a child. This is best illustrated by an example. Consider the following parent model at `examplemod:example_composite_model`:

```json5
{
    "loader": "neoforge:composite",
    // Specify model parts.
    "children": {
        // These can either be references to another model or a model itself.
        "part_1": {
            "parent": "examplemod:some_model_1"
        },
        "part_2": {
            "parent": "examplemod:some_model_2"
        }
    },
    "visibility": {
        // Disable part 2 by default.
        "part_2": false
    }
}
```

Then, we can disable and enable individual parts in a child model of `examplemod:example_composite_model`:

```json5
{
    "parent": "examplemod:example_composite_model",
    // Override visibility. If a part is missing, it will use the parent model's visibility value.
    "visibility": {
        "part_1": false,
        "part_2": true
    }
}
```

To [datagen][modeldatagen] this model, use the custom loader class `CompositeModelBuilder`.

:::warning
The composite model loader should not be used for models used by [client items][citems]. Instead, they should use the [composite model][itemcomposite] provided in the definition itself.
:::

### Empty Model

An empty model just renders nothing at all.

```json5
{
    "loader": "neoforge:empty"
}
```

### OBJ Model

The OBJ model loader allows you to use Wavefront `.obj` 3D models in the game, allowing for arbitrary shapes (including triangles, circles, etc.) to be included in a model. The `.obj` model must be placed in the `models` folder (or a subfolder thereof), and a `.mtl` file with the same name must be provided (or set manually), so for example, an OBJ model at `models/block/example.obj` must have a corresponding MTL file at `models/block/example.mtl`.

```json5
{
    "loader": "neoforge:obj",
    // Required. Reference to the model file. Note that this is relative to the namespace root, not the model folder.
    "model": "examplemod:models/example.obj",
    // Normally, .mtl files must be put into the same location as the .obj file, with only the file ending differing.
    // This will cause the loader to automatically pick them up. However, you can also set the location
    // of the .mtl file manually if needed.
    "mtl_override": "examplemod:models/example_other_name.mtl",
    // These textures can be referenced in the .mtl file as #texture0, #particle, etc.
    // This usually requires manual editing of the .mtl file.
    "textures": {
        "texture0": "minecraft:block/cobblestone",
        "particle": "minecraft:block/stone"
    },
    // Enable or disable automatic culling of the model. Optional, defaults to true.
    "automatic_culling": false,
    // Whether to shade the model or not. Optional, defaults to true.
    "shade_quads": false,
    // Some modeling programs will assume V=0 to be bottom instead of the top. This property flips the Vs upside-down.
    // Optional, defaults to false.
    "flip_v": true,
    // Whether to enable emissivity or not. Optional, defaults to true.
    "emissive_ambient": false
}
```

To [datagen][modeldatagen] this model, use the custom loader class `ObjModelBuilder`.

### Creating Custom Model Loaders

To create your own model loader, you need four classes, plus an event handler:

- An `UnbakedModelLoader` class
- An `UnbakedGeometry` class, usually an `ExtendedUnbakedGeometry` instance
- An `UnbakedModel` class, usually an `AbstractUnbakedModel` instance
- A `QuadCollection` class to hold the baked quads, usually the class itself
- A [client-side][sides] [event handler][event] for `ModelEvent.RegisterLoaders` that registers the unbaked model loader
- Optional: A [client-side][sides] [event handler][event] for `RegisterClientReloadListenersEvent` for model loaders that cache data about what is being loaded

To illustrate how these classes are connected, we will follow a model being loaded:

- During model loading, a model JSON with the `loader` property set to your loader is passed to your unbaked model loader. The loader then reads the model JSON and returns an `UnbakedModel` object using the model JSON's properties and an `UnbakedGeometry` with the model's unbaked quads.
- During model baking, `UnbakedGeometry#bake` is called, returning a `QuadCollection`.
- During model rendering, the `QuadCollection`, along with any other information required by the [client item][citems] or [block state definition][blockstatedefinition] is used in rendering.

:::note
If you are creating a custom model loader for a model used by an item or block state, depending on the use case, it might be better to create a new `ItemModel` or `BlockStateModel` instead. For example, a model that uses or generates `QuadCollection`s would make more sense as an `ItemModel` or `BlockStateModel`, while a model that parses a different data format (like `.obj`) should use a new model loader.
:::

Let's illustrate this further through a basic class setup. The loader class is named `MyUnbakedModelLoader`, the unbaked class is named `MyUnbakedModel`, and the unbaked geometry is called `MyUnbakedGeometry`. We will also assume that the model loader requires some cache:

```java
// This is the class used to load the model into its unbaked format
public class MyUnbakedModelLoader implements UnbakedModelLoader<MyUnbakedModel>, ResourceManagerReloadListener {
    // It is highly recommended to use a singleton pattern for unbaked model loaders, as all models can be loaded through one loader.
    public static final MyUnbakedModelLoader INSTANCE = new MyUnbakedModelLoader();
    // The id we will use to register this loader. Also used in the loader datagen class.
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("examplemod", "my_custom_loader");

    // In accordance with the singleton pattern, make the constructor private.        
    private MyUnbakedModelLoader() {}

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        // Handle any cache clearing logic
    }

    @Override
    public MyUnbakedModel read(JsonObject obj, JsonDeserializationContext context) throws JsonParseException {
        // Use the given JsonObject and, if needed, the JsonDeserializationContext to get properties from the model JSON.
        // The MyUnbakedModel constructor may have constructor parameters (see below).

        // Read the data used to create the quads
        MyUnbakedGeometry geometry;

        // For the basic parameters provided by vanilla and NeoForge, you can use the StandardModelParameters
        StandardModelParameters params = StandardModelParameters.parse(obj, context);

        return new MyUnbakedModel(params, geometry);
    }
}

// Holds the unbaked quads to render
// Other information that is stored in the unbaked model should be passed to the context map
public class MyUnbakedGeometry implements ExtendedUnbakedGeometry {

    public MyUnbakedGeometry(...) {
        // Store the unbaked quads to bake
    }

    // Method responsible for model baking, returning the quad collection. Parameters in this method are:
    // - The map of texture names to their associated materials.
    // - The model baker. Can be used for getting sub-models to bake and getting sprites from the texture slots.
    // - The model state. This holds the transformations from the blockstate file, typically from rotations and the uvlock.
    // - The name of the model.
    // - A ContextMap of settings provided by NeoForge and your unbaked model. See the 'NeoForgeModelProperties' class for all available properties.
    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
        // The builder to create the collection
        var builder = new QuadCollection.Builder();
        // Build the quads for baking
        builder.addUnculledFace(...); // or addCulledFace(Direction, BakedQuad)
        // Create the quad collection
        return builder.build();
    }
}

// The unbaked model contains all the information read from the JSON.
// It provides the basic settings and geometry.
// Using AbstractUnbakedModel sets the Vanilla and NeoForge properties methods
public class MyUnbakedModel extends AbstractUnbakedModel {

    private final MyUnbakedGeometry geometry;

    public MyUnbakedModel(StandardModelParameters params, MyUnbakedGeometry geometry) {
        super(params);
        this.geometry = geometry;
    }

    @Override
    public UnbakedGeometry geometry() {
        // The geometry to used to construct the baked quads
        return this.geometry;
    }

    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
        super.fillAdditionalProperties(propertiesBuilder);
        // Add additional properties below by calling withParameter(ContextKey<T>, T)
        // They can then be accessed in the ContextMap provided in UnbakedGeometry#bake
    }
}
```

When all is done, don't forget to actually register your loader:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerLoaders(ModelEvent.RegisterLoaders event) {
    event.register(MyUnbakedModelLoader.ID, MyUnbakedModelLoader.INSTANCE);
}

// If you are caching data in the model loader:
@SubscribeEvent // on the mod event bus only on the physical client
public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(MyUnbakedModelLoader.INSTANCE);
}
```

#### Model Loader Datagen

Of course, we can also [datagen] our models. To do so, we need a class that extends `CustomLoaderBuilder`:

```java
public class MyLoaderBuilder extends CustomLoaderBuilder {
    public MyLoaderBuilder() {
        super(
            // Your model loader's id.
            MyUnbakedModelLoader.ID,
            // Whether the loader allows inline vanilla elements as a fallback if the loader is absent.
            false
        );
    }
    
    // Add fields and setters for the fields here. The fields can then be used below.

    @Override
    protected CustomLoaderBuilder copyInternal() {
        // Create a new instance of your loader builder and copy the properties from this builder
        // to the new instance.
        MyLoaderBuilder builder = new MyLoaderBuilder();
        // builder.<field> = this.<field>;
        return builder;
    }
    
    // Serialize the model to JSON.
    @Override
    public JsonObject toJson(JsonObject json) {
        // Add your fields to the given JsonObject.
        // Then call super, which adds the loader property and some other things.
        return super.toJson(json);
    }
}
```

To use this loader builder, do the following during block (or item) [model datagen][modeldatagen]:

```java
// This assumes an extension of ModelProvider and a DeferredBlock<Block> EXAMPLE_BLOCK.
// The parameter for customLoader() is a Supplier to construct the builder and a Consumer to set to associated properties.
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    blockModels.createTrivialBlock(
        // The block to generate the model for
        EXAMPLE_BLOCK.get(),
        TexturedModel.createDefault(
            // A mapping used to get the textures
            block -> new TextureMapping().put(
                TextureSlot.ALL, TextureMapping.getBlockTexture(block)
            ),
            // The model template builder used to create the JSON
            ExtendedModelTemplateBuilder.builder()
                // Say we are using a custom model loader
                .customLoader(MyLoaderBuilder::new, loader -> {
                    // Set any required fields here
                })
                // Textures required by the model
                .requiredTextureSlot(TextureSlot.ALL)
                // Call build once complete
                .build()
        )
    );
}
```

#### Visibility

The default implementation of `CustomLoaderBuilder` holds methods for applying visibility. You may choose to use or ignore the `visibility` property in your model loader. Currently, only the [composite model loader][composite] and [OBJ loader][obj] make use of this property.

## Block State Model Loaders

As block state models are considered separate from the model JSON file, there are also custom NeoForge loaders, handled by specifying a `type` in a variant or multipart. A custom block state model loader may ignore all fields the loader requires.

### Composite Block State Model

A composite block state model can be used to render multiple `BlockStateModel`s together.

```json5
{
    "variants": {
        "": {
            "type": "neoforge:composite",
            // Specify model parts.
            "models": [
                // These must be inlined block state models
                {
                    "variants": {
                        // ...
                    }
                },
                {
                    "multipart": [
                        // ...
                    ]
                }
                // ...
            ]
        }
    }
}
```

To [datagen][modeldatagen] this block state model, use the custom loader class `CompositeBlockStateModelBuilder`.

### Reusing the Default Model Loader

In some contexts, it makes sense to reuse the vanilla model loader and just building your model logic on top of that instead of outright replacing it. We can do so using a neat trick: in the model loader, we simply remove the `loader` property and send it back to the model deserializer, tricking it into thinking that it is a regular unbaked model now. Then, we can modify the model or its geometry before the baking process, where we can do whatever way we want.

```java
public class MyUnbakedModelLoader implements UnbakedModelLoader<MyUnbakedModel> {
    public static final MyUnbakedModelLoader INSTANCE = new MyUnbakedModelLoader();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("examplemod", "my_custom_loader");
    
    private MyUnbakedModelLoader() {}

    @Override
    public MyUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        // Trick the deserializer into thinking this is a normal model by removing the loader field
        // Then, pass it to the deserializer.
        jsonObject.remove("loader");
        UnbakedModel model = context.deserialize(jsonObject, UnbakedModel.class);
        return new MyUnbakedModel(model, /* other parameters here */);
    }
}

// We extend the delegate class as that stores the wrapped model
public class MyUnbakedModel extends DelegateUnbakedModel {

    // Store the model for use below
    public MyUnbakedModel(UnbakedModel model, /* other parameters here */) {
       super(model);
    }
}
```

### Creating Custom Block State Model Loaders

To create your own block state model loader, you need five classes, plus an event handler:

- A `CustomUnbakedBlockStateModel` class to load the block state model
- A `BlockStateModel` class to bake the model, usually a `DynamicBlockStateModel` instance
- A `BlockModelPart.Unbaked` to load the model JSON
- A `ModelState` to apply any transformations to a given face or model 
- A `BlockModelPart` to hold the quads, ambient occlusion, and particle texture, commonly a `SimpleModelWrapper`
- A [client-side][sides] [event handler][event] for `RegisterBlockStateModels` that registers the codec for the unbaked block state model loader

To illustrate how these classes are connected, we will follow a block state model being loaded:

- During definition loading, a block state model within a variant, multipart, or [custom definition][customdefinition] with the `type` property set to your loader is decoded to your `CustomUnbakedBlockStateModel`.
- During model baking, `CustomUnbakedBlockStateModel#bake` is called, returning a `BlockStateModel`, which contains some list of `BlockModelPart`s.
- During model rendering, `BlockStateModel#collectParts` collects the list of `BlockModelPart`s to render.

Let's illustrate this further through a basic class setup. The baked model is named `MyBlockStateModel`, the unbaked class is an inner record `MyBlockStateModel.Unbaked`, model parts is called `MyBlockModelPart`, the unbaked part class is an inner record `MyBlockModelPart.Unbaked`, and the `ModelState` is named `MyModelState`:

```java
// The model state used to apply the necessary transformations
// If you are using an intermediate object to hold the model state, it must be transformable to a ModelState
public class MyModelState implements ModelState {

    // Used for the unbaked block model part
    public static final Codec<MyModelState> CODEC = Codec.unit(new MyModelState());

    public MyModelState() {}

    @Override
    public Transformation transformation() {
        // Returns the model rotation to apply to the baking vertices
        return Transformation.identity();
    }

    @Override
    public Matrix4fc faceTransformation(Direction direction) {
        // Returns the matrix that is applied to a given face on the model after the transformation
        // This is currently unused in Vanilla
        return NO_TRANSFORM;
    }

    @Override
    public Matrix4fc inverseFaceTransformation(Direction direction) {
        // Returns the inverse of faceTransformation that is applied to a given face on the model
        // This is passed to the FaceBakery
        return NO_TRANSFORM;
    }
}

// The model part representing a baked model
// useAmbientOcclusion and particleIcon are implemented as part of the record
public record MyBlockModelPart(QuadCollection quads, boolean useAmbientOcclusion, TextureAtlasSprite particleIcon) implements BlockModelPart {

    // Get the baked quads to render
    @Override
    List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.getQuads(direction);
    }

    // The unbaked model that is read from the block state json
    public record Unbaked(ResourceLocation modelLocation, MyModelState modelState) implements BlockModelPart.Unbaked {

        // Used for the unbaked block state model
        public static final MapCodec<MyBlockModelPart.Unbaked> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(MyBlockModelPart.Unbaked::modelLocation),
                MyModelState.CODEC.fieldOf("state").forGetter(MyBlockModelPart.Unbaked::modelState)
            ).apply(instance, MyBlockModelPart.Unbaked::new)
        );

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            // Mark any models used by the model part
            resolver.markDependency(this.modelLocation);
        }

        @Override
        public BlockModelPart bake(ModelBaker baker) {
            // Get the model to bake
            ResolvedModel resolvedModel = baker.getModel(this.modelLocation);

            // Get the necessary settings for the model part
            TextureSlots slots = resolvedModel.getTopTextureSlots();
            boolean ao = resolvedModel.getTopAmbientOcclusion();
            TextureAtlasSprite particle = resolvedModel.resolveParticleSprite(slots, baker);
            QuadCollection quads = resolvedModel.bakeTopGeometry(slots, baker, this.modelState);
            
            // Return the baked part
            return new MyBlockModelPart(quads, ao, particle);
        }
    }
}

// The state model representing the baked block state
public record MyBlockStateModel(MyBlockModelPart model) implements DynamicBlockStateModel {

    // Sets the particle icon
    // While it needs to be implemented, any actual logic should be delegated to the level-aware version
    @Override
    public TextureAtlasSprite particleIcon() {
        return this.model.particleIcon();
    }

    // This effectively acts as a key to reuse geometry previous produced. This should generally be as deterministic as possible.
    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return this;
    }

    // Method responsible for collecting the parts to be rendered. Parameters in this method are:
    // - The getter for the blocks and tints, usually the level.
    // - The position of the block to render.
    // - The state of the block.
    // - A random instance.
    // - This list of model parts to be rendered. Add your model parts here.
    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        // If you want the block rendered to be dependent on the block entity (e.g., your block entity implements `BlockEntity#getModelData`)
        // You can call `BlockAndTintGetter#getModelData` with the block position
        // You can read the property using `get` with the `ModelProperty` key
        // Remember that your block entity should call `BlockEntity#requestModelDataUpdate` to sync the model data to the client
        ModelData data = level.getModelData(pos);

        // Add the model to be rendered
        parts.add(this.model);
    }

    @Override
    public TextureAtlasSprite particleIcon(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        // Override this if you want to use the level to determine what particle to render
        return self().particleIcon();
    }

    // The unbaked model that is read from the block state json
    public record Unbaked(MyBlockModelPart.Unbaked model) implements CustomUnbakedBlockStateModel {

        // The codec to register
        public static final MapCodec<MyBlockStateModel.Unbaked> CODEC = MyBlockModelPart.Unbaked.CODEC.xmap(
            MyBlockStateModel.Unbaked::new, MyBlockStateModel.Unbaked::model
        );
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("examplemod", "my_custom_model_loader");

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            // Mark any models used by the state model
            this.model.resolveDependencies(resolver);
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            // Bake the model parts and pass into the block state model
            return new MyBlockStateModel(this.model.bake(baker));
        }
    }
}
```


When all is done, don't forget to actually register your loader:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerDefinitions(RegisterBlockStateModels event) {
    event.registerModel(MyBlockStateModel.Unbaked.ID, MyBlockStateModel.Unbaked.CODEC);
}
```

#### State Model Loader Datagen

Of course, we can also [datagen] our models. To do so, we need a class that extends `CustomBlockStateModelBuilder`:

```java
// The builder used to construct the block state JSON
public class MyBlockStateModelBuilder extends CustomBlockStateModelBuilder {

    private MyBlockModelPart.Unbaked model;

    public MyBlockStateModelBuilder() {}
    
    // Add fields and setters for the fields here. The fields can then be used below.

    @Override
    public MyBlockStateModelBuilder with(VariantMutator variantMutator) {
        // If you want to apply any mutators that assumes your unbaked model part is a `Variant`
        // If not, this should do nothing
        return this;
    }

    // This is for generalized unbaked blockstate models
    @Override
    public MyBlockStateModelBuilder with(UnbakedMutator unbakedMutator) {
        var result = new MyBlockStateModelBuilder();

        if (this.model != null) {
            result.model = unbakedMutator.apply(this.model);
        }

        return result;
    }

    // Converts the builder to its unbaked variant to encode
    @Override
    public CustomUnbakedBlockStateModel toUnbaked() {
        return new MyBlockStateModel.Unbaked(this.model);
    }
}
```

To use this state definition loader builder, do the following during block (or item) [model datagen][modeldatagen]:

```java
// This assumes an extension of ModelProvider and a DeferredBlock<Block> EXAMPLE_BLOCK.
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    blockModels.blockStateOutput.accept(
        MultiVariantGenerator.dispatch(
            // The block to generate the model for
            EXAMPLE_BLOCK.get(),
            // Our custom block state builder
            MultiVariant.of(new CustomBlockStateModelBuilder().with(...))
        )
    );
}
```

This will generate a model like so:

```json5
{
  "variants": {
    "": {
        "type": "examplemod:my_custom_model_loader"
        // Other fields
    }
  }
}
```

## Block State Definition Loaders

While individual block state models handle the loading of a single block state, block state definition loaders handle the entire loading of a block state file, handled by specifying a `neoforge:definition_type`. A custom block state definition loader may ignore all fields the loader requires.

### Creating Custom Block State Definition Loaders

To create your own block state definition loader, you need two classes, plus an event handler:

- A `CustomBlockModelDefinition` class to load the block state definition
- A `BlockStateModel.UnbakedRoot` class to bake a block state to its `BlockStateModel`
- A [client-side][sides] [event handler][event] for `RegisterBlockStateModels` that registers the codec for the unbaked block state model loader

To illustrate how these classes are connected, we will follow a block state model being loaded:

- During definition loading, a block state definition with the `neoforge:definition_type` property set to your loader is decoded to a `CustomBlockModelDefinition`.
- Then, `CustomBlockModelDefinition#instantiate` is called to map all possible block states to their `BlockStateModel.UnbakedRoot`. For simple cases, this is constructed via `BlockStateModel.Unbaked#asRoot`. Complicated instances create their own `BlockStateModel.UnbakedRoot`.
- During model baking, `BlockStateModel.UnbakedRoot#bake` is called, returning a `BlockStateModel` for some `BlockState`.

Let's illustrate this further through a basic class setup. The block model definition is named `MyBlockModelDefinition` and we will reuse `BlockStateModel.Unbaked#asRoot` to construct the `BlockStateModel.UnbakedRoot`:

```java
public record MyBlockModelDefinition(MyBlockStateModel.Unbaked model) implements CustomBlockModelDefinition {

    // The codec to register
    public static final MapCodec<MyBlockModelDefinition> CODEC = MyBlockStateModel.Unbaked.CODEC.xmap(
        MyBlockModelDefinition::new, MyBlockModelDefinition::model
    );
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("examplemod", "my_custom_definition_loader");

    // This method maps all possible states to some unbaked root
    // As the root will generally share block states models, they are typically operated using a `ModelBaker.SharedOperationKey` to cache the loading model
    @Override
    public Map<BlockState, BlockStateModel.UnbakedRoot> instantiate(StateDefinition<Block, BlockState> states, Supplier<String> sourceSupplier) {
        Map<BlockState, BlockStateModel.UnbakedRoot> result = new HashMap<>();

        // Handle for all possible states
        var unbakedRoot = this.model.asRoot();
        states.getPossibleStates().forEach(state -> result.put(state, unbakedRoot));

        return result;
    }

    @Override
    public MapCodec<? extends CustomBlockModelDefinition> codec() {
        return CODEC;
    }
}
```

When all is done, don't forget to actually register your loader:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerDefinitions(RegisterBlockStateModels event) {
    event.registerDefinition(MyBlockModelDefinition.ID, MyBlockModelDefinition.CODEC);
}
```

#### State Definition Loader Datagen

Of course, we can also [datagen] our definitions. To do so, we need a class that extends `BlockModelDefinitionGenerator`:

```java
public class MyBlockModelDefinitionGenerator implements BlockModelDefinitionGenerator {

    private final Block block;
    private final MyBlockStateModelBuilder builder;

    private MyBlockModelDefinitionGenerator(Block block, MyBlockStateModelBuilder builder) {
        this.block = block;
        this.builder = builder;
    }

    public static MyBlockModelDefinitionGenerator dispatch(Block block, MyBlockStateModelBuilder builder) {
        return new MyBlockModelDefinitionGenerator(block, builder);
    }

    @Override
    public Block block() {
        // Returns the block you are generating the definition file for
        return this.block;
    }

    @Override
    public BlockModelDefinition create() {
        // Creates the block model definition used to encode and decode the file
        return new MyBlockModelDefinition(this.builder.toUnbaked());
    }
} 
```

To use this state definition loader builder, do the following during block (or item) [model datagen][modeldatagen]:

```java
// This assumes a DeferredBlock<Block> EXAMPLE_BLOCK.
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    blockModels.blockStateOutput.accept(
        MyBlockModelDefinitionGenerator.dispatch(
            // The block to generate the model for
            EXAMPLE_BLOCK.get(),
            new CustomBlockStateModelBuilder(...)
        )
    );
}
```

This will generate a model like so:

```json5
{
    "neoforge:definition_type": "examplemod:my_custom_definition_loader"
    // Other fields
}
```

[citems]: items.md
[composite]: #composite-model
[customdefinition]: #block-state-definition-loaders
[datagen]: ../../index.md#data-generation
[event]: ../../../concepts/events.md#registering-an-event-handler
[itemcomposite]: items.md#composite-models
[modeldatagen]: datagen.md
[obj]: #obj-model
[sides]: ../../../concepts/sides.md
