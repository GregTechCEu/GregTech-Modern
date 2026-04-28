# Custom Model Loaders

A model is simply a shape. It can be a cube, a collection of cubes, a collection of triangles, or any other geometrical shape (or collection of geometrical shape). For most contexts, it is not relevant how a model is defined, as everything will end up as a `BakedModel` in memory anyway. As such, NeoForge adds the ability to register custom model loaders that can transform any model you want into a `BakedModel` for the game to use.

The entry point for a block model remains the model JSON file. However, you can specify a `loader` field in the root of the JSON that will swap out the default loader for your own loader. A custom model loader may ignore all fields the default loader requires.

## Builtin Model Loaders

Besides the default model loader, NeoForge offers several builtin loaders, each serving a different purpose.

### Composite Model

A composite model can be used to specify different model parts in the parent and only apply some of them in a child. This is best illustrated by an example. Consider the following parent model at `examplemod:example_composite_model`:

```json5
{
    "loader": "neoforge:composite",
    // Specify model parts.
    "children": {
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

## Creating Custom Model Loaders

To create your own model loader, you need three classes, plus an event handler:

- An `UnbakedModelLoader` class
- An `UnbakedModel` class
- A [baked model][bakedmodel] class, usually a `SimpleBakedModel` instance, or `IDynamicBakedModel` if the `ModelData` is required
- A [client-side][sides] [event handler][event] for `ModelEvent.RegisterLoaders` that registers the unbaked model loader
- Optional: A [client-side][sides] [event handler][event] for `RegisterClientReloadListenersEvent` for model loaders that cache data about what is being loaded

To illustrate how these classes are connected, we will follow a model being loaded:

- During model loading, a model JSON with the `loader` property set to your loader is passed to your unbaked model loader. The loader then reads the model JSON and returns an unbaked object using the model JSON's properties.
- During model baking, the object is baked, returning a baked model.
- During model rendering, the baked model is used for rendering.

:::note
If you are creating a custom model loader for a model used by an item, depending on the use case, it would be better to create a new `ItemModel` instead. For example, a model that uses or generates `BakedModel`s would make more sense as an `ItemModel` while a model that renders a different data format (like `.obj`) should create a new model loader.
:::

Let's illustrate this further through a basic class setup. The loader class is named `MyUnbakedModelLoader`, the unbaked class is named `MyUnbakedModel`, and we construct a `SimpleBakedModel` instance. We will also assume that the model loader requires some cache:

```java
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
    public MyUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        // Use the given JsonObject and, if needed, the JsonDeserializationContext to get properties from the model JSON.
        // The MyUnbakedModel constructor may have constructor parameters (see below).
        return new MyUnbakedModel();
    }
}

// AbstractUnbakedModel is used as the base unbaked model for custom models.
// Adds support for vanilla and NeoForge properties but leaves the bake method
// for modder implementation.
public class MyUnbakedModel extends AbstractUnbakedModel {
    // The constructor may have any parameters you need, and store them in fields for further usage below.
    // If the constructor has parameters, the constructor call in MyUnbakedModelLoader#read must match them.
    public MyUnbakedModel() {}

    // Method responsible for model baking, returning our baked model. Parameters in this method are:
    // - The map of texture names to their associated materials.
    // - The model baker. Can be used for baking sub-models and getting sprites from the texture slots.
    // - The model state. This holds the properties from the blockstate file, e.g. rotations and the uvlock boolean.
    // - A boolean of whether to use ambient occlusion when rendering the model.
    // - A boolean of whether to use the block light when rendering a model.
    // - The item transforms associated with how this model should be displayed in a given ItemDisplayContext.
    // - A ContextMap of settings provided by NeoForge. See the 'NeoForgeModelProperties' class for all available properties.
    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        // The true boolean represents if the model is in 3D within the GUI
        var builder = new SimpleBakedModel.Builder(useAmbientOcclusion, usesBlockLight, true, itemTransforms);
        // Sets the particle texture
        builder.particle(baker.findSprite(textures, TextureSlot.PARTICLE.getId()));
        // Add the baked quads (call as many times as necessary)
        builder.addUnculledFace(...) // or addCulledFace(Direction, BakedQuad)
        // Create the baked model
        return builder.build(additionalProperties.getOrDefault(NeoForgeModelProperties.RENDER_TYPE, RenderTypeGroup.EMPTY));
    }

    // Method responsible for correctly resolving parent properties. Required if this model loads any nested models or reuses the vanilla loader on itself (see below).
    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        // ResolvableModel.Resolver#resolve
    }

    // Add properties to the context map used for baking
    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
        super.fillAdditionalProperties(propertiesBuilder);
        // Add additional properties below by calling withParameter(ContextKey<T>, T)
    }
}
```

When all is done, don't forget to actually register your loader, otherwise all the work will have been for nothing:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerGeometryLoaders(ModelEvent.RegisterLoaders event) {
    event.register(MyUnbakedModelLoader.ID, MyUnbakedModelLoader.INSTANCE);
}

// If you are caching data in the model loader:
@SubscribeEvent // on the mod event bus only on the physical client
public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(MyUnbakedModelLoader.INSTANCE);
}
```

### Datagen

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
    )
}
```

#### Visibility

The default implementation of `CustomLoaderBuilder` holds methods for applying visibility. You may choose to use or ignore the `visibility` property in your model loader. Currently, only the [composite model loader][composite] and [OBJ loader][obj] make use of this property.

### Reusing the Default Model Loader

In some contexts, it makes sense to reuse the vanilla model loader and just building your model logic on top of that instead of outright replacing it. We can do so using a neat trick: in the model loader,  we simply remove the `loader` property and send it back to the model deserializer, tricking it into thinking that it is a regular unbaked model now. Then, we bake the model during the baking process to pass along to the baked model, where we can the use the model's quads in whatever way we want.

:::note
The following example should only be used if the file only contains a single model JSON, whether on the top-level or nested within some object. If multiple models need to be loaded, then the JSON should either contain references to the other JSON files, or the children objects should be deserialized into `UnbakedModel`s and baked via `UnbakedModel#bakeWithTopModelValues`. Using references is the recommended method.
:::

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

    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        BakedModel base = super.bake(textures, baker, modelState, useAmbientOcclusion, usesBlockLight, itemTransforms, additionalProperties);
        return new MyBakedModel(base, /* other parameters here */);
    }
}

// We extend the delegate class as that stores the wrapped model
public class MyDynamicModel extends DelegateBakedModel {

    // other fields here

    public MyDynamicModel(BakedModel base, /* other parameters here */) {
        super(base);
        // set other fields here
    }

    // other override methods here

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();
        // Add the base model's quads. Can also do something different with the quads here, depending on what you need.
        quads.addAll(this.parent.getQuads(state, side, rand, extraData, renderType));
        // add other elements to the quads list as needed here
        return quads;
    }
}
```

[bakedmodel]: bakedmodel.md
[citems]: items.md
[composite]: #composite-model
[datagen]: ../../index.md#data-generation
[event]: ../../../concepts/events.md#registering-an-event-handler
[itemcomposite]: items.md#composite-models
[modeldatagen]: datagen.md
[obj]: #obj-model
[sides]: ../../../concepts/sides.md
