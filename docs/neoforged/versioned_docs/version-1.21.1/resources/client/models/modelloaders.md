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

### Dynamic Fluid Container Model

The dynamic fluid container model, also called dynamic bucket model after its most common use case, is used for items that represent a fluid container (such as a bucket or a tank) and want to show the fluid within the model. This only works if there is a fixed amount of fluids (e.g. only lava and powder snow) that can be used, use a [`BlockEntityWithoutLevelRenderer`][bewlr] instead if the fluid is arbitrary.

```json5
{
    "loader": "neoforge:fluid_container",
    // Required. Must be a valid fluid id.
    "fluid": "minecraft:water",
    // The loader generally expects two textures: base and fluid.
    "textures": {
        // The base container texture, i.e. the equivalent of an empty bucket.
        "base": "examplemod:item/custom_container",
        // The fluid texture, i.e. the equivalent of water in a bucket.
        "fluid": "examplemod:item/custom_container_fluid"
    },
    // Optional, defaults to false. Whether to flip the model upside down, for gaseous fluids.
    "flip_gas": true,
    // Optional, defaults to true. Whether to have the cover act as the mask.
    "cover_is_mask": false,
    // Optional, defaults to true. Whether to apply the fluid's luminosity to the item model.
    "apply_fluid_luminosity": false,
}
```

Very often, dynamic fluid container models will directly use the bucket model. This is done by specifying the `neoforge:item_bucket` parent model, like so:

```json5
{
    "loader": "neoforge:fluid_container",
    "parent": "neoforge:item/bucket",
    // Replace with your own fluid.
    "fluid": "minecraft:water"
    // Optional properties here. Note that the textures are handled by the parent.
}
```

To [datagen][modeldatagen] this model, use the custom loader class `DynamicFluidContainerModelBuilder`. Be aware that for legacy support reasons, this class also provides a method to set the `apply_tint` property, which is no longer used.

### Elements Model

An elements model consists of block model [elements][elements] and an optional [root transform][transform]. Intended mainly for usage outside regular model rendering, for example within a [BER][ber].

```json5
{
    "loader": "neoforge:elements",
    "elements": [...],
    "transform": {...}
}
```

### Empty Model

An empty model just renders nothing at all.

```json5
{
    "loader": "neoforge:empty"
}
```

### Item Layer Model

Item layer models are a variant of the standard `item/generated` model that offer the following additional features:

- Unlimited amount of layers (instead of the default 5)
- Per-layer [render types][rendertype]

```json5
{
    "loader": "neoforge:item_layers",
    "textures": {
        "layer0": "...",
        "layer1": "...",
        "layer2": "...",
        "layer3": "...",
        "layer4": "...",
        "layer5": "...",
    },
    "render_types": {
        // Map render types to layer numbers. For example, layers 0, 2 and 4 will use cutout.
        "minecraft:cutout": [0, 2, 4],
        "minecraft:cutout_mipped": [1, 3],
        "minecraft:translucent": [5]
    },
    // other stuff the default loader allows here
}
```

To [datagen][modeldatagen] this model, use the custom loader class `ItemLayerModelBuilder`.

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

### Separate Transforms Model

A separate transforms model can be used to switch between different models based on the perspective. The perspectives are the same as for the `display` block in a [normal model][model]. This works by specifying a base model (as a fallback) and then specifying per-perspective override models. Note that each of these can be fully-fledged models if you so desire, but it is usually easiest to just refer to another model by using a child model of that model, like so:

```json5
{
    "loader": "neoforge:separate_transforms",
    // Use the cobblestone model normally.
    "base": {
        "parent": "minecraft:block/cobblestone"
    },
    // Use the stone model only when dropped.
    "perspectives": {
        "ground": {
            "parent": "minecraft:block/stone"
        }
    }
}
```

To [datagen][modeldatagen] this model, use the custom loader class `SeparateTransformsModelBuilder`.

## Creating Custom Model Loaders

To create your own model loader, you need three classes, plus an event handler:

- A geometry loader class
- A geometry class
- A dynamic [baked model][bakedmodel] class
- A [client-side][sides] [event handler][event] for `ModelEvent.RegisterGeometryLoaders` that registers the geometry loader

To illustrate how these classes are connected, we will follow a model being loaded:

- During model loading, a model JSON with the `loader` property set to your loader is passed to your geometry loader. The geometry loader then reads the model JSON and returns a geometry object using the model JSON's properties.
- During model baking, the geometry is baked, returning a dynamic baked model.
- During model rendering, the dynamic baked model is used for rendering.

Let's illustrate this further through a basic class setup. The geometry loader class is named `MyGeometryLoader`, the geometry class is named `MyGeometry`, and the dynamic baked model class is named `MyDynamicModel`:

```java
public class MyGeometryLoader implements IGeometryLoader<MyGeometry> {
    // It is highly recommended to use a singleton pattern for geometry loaders, as all models can be loaded through one loader.
    public static final MyGeometryLoader INSTANCE = new MyGeometryLoader();
    // The id we will use to register this loader. Also used in the loader datagen class.
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("examplemod", "my_custom_loader");
    
    // In accordance with the singleton pattern, make the constructor private.        
    private MyGeometryLoader() {}
    
    @Override
    public MyGeometry read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        // Use the given JsonObject and, if needed, the JsonDeserializationContext to get properties from the model JSON.
        // The MyGeometry constructor may have constructor parameters (see below).
        return new MyGeometry();
    }
}

public class MyGeometry implements IUnbakedGeometry<MyGeometry> {
    // The constructor may have any parameters you need, and store them in fields for further usage below.
    // If the constructor has parameters, the constructor call in MyGeometryLoader#read must match them.
    public MyGeometry() {}

    // Method responsible for model baking, returning our dynamic model. Parameters in this method are:
    // - The geometry baking context. Contains many properties that we will pass into the model, e.g. light and ao values.
    // - The model baker. Can be used for baking sub-models.
    // - The sprite getter. Maps materials (= texture variables) to TextureAtlasSprites. Materials can be obtained from the context.
    //   For example, to get a model's particle texture, call spriteGetter.apply(context.getMaterial("particle"));
    // - The model state. This holds the properties from the blockstate file, e.g. rotations and the uvlock boolean.
    // - The item overrides. This is the code representation of an "overrides" block in an item model.
    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        // See info on the parameters below.
        return new MyDynamicModel(context.useAmbientOcclusion(), context.isGui3d(), context.useBlockLight(),
            spriteGetter.apply(context.getMaterial("particle")), overrides);
    }

    // Method responsible for correctly resolving parent properties. Required if this model loads any nested models or reuses the vanilla loader on itself (see below).
    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        // UnbakedModel#resolveParents
    }
}

// BakedModelWrapper can be used as well to return default values for most methods, allowing you to only override what actually needs to be overridden.
public class MyDynamicModel implements IDynamicBakedModel {
    // Material of the missing texture. Its sprite can be used as a fallback when needed.
    private static final Material MISSING_TEXTURE = 
        new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());

    // Attributes for use in the methods below. Optional, the methods may also use constant values if applicable.
    private final boolean useAmbientOcclusion;
    private final boolean isGui3d;
    private final boolean usesBlockLight;
    private final TextureAtlasSprite particle;
    private final ItemOverrides overrides;

    // The constructor does not require any parameters other than the ones for instantiating the final fields.
    // It may specify any additional parameters to store in fields you deem necessary for your model to work.
    public MyDynamicModel(boolean useAmbientOcclusion, boolean isGui3d, boolean usesBlockLight, TextureAtlasSprite particle, ItemOverrides overrides) {
        this.useAmbientOcclusion = useAmbientOcclusion;
        this.isGui3d = isGui3d;
        this.usesBlockLight = usesBlockLight;
        this.particle = particle;
        this.overrides = overrides;
    }

    // Use our attributes. Refer to the article on baked models for more information on the method's effects.
    @Override
    public boolean useAmbientOcclusion() {
        return useAmbientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return isGui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return usesBlockLight;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        // Return MISSING_TEXTURE.sprite() if you don't need a particle, e.g. when in an item model context.
        return particle;
    }

    @Override
    public ItemOverrides getOverrides() {
        // Return ItemOverrides.EMPTY when in a block model context.
        return overrides;
    }

    // Override this to true if you want to use a custom block entity renderer instead of the default renderer.
    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    // This is where the magic happens. Return a list of the quads to render here. Parameters are:
    // - The blockstate being rendered. May be null if rendering an item.
    // - The side being culled against. May be null, which means quads that cannot be occluded should be returned.
    // - A client-bound random source you can use for randomizing stuff.
    // - The extra data to use. Originates from a block entity (if present), or from BakedModel#getModelData().
    // - The render type for which quads are being requested.
    // NOTE: This may be called many times in quick succession, up to several times per block.
    // This should be as fast as possible and use caching wherever applicable.
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();
        // add elements to the quads list as needed here
        return quads;
    }
}
```

When all is done, don't forget to actually register your loader, otherwise all the work will have been for nothing:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
    event.register(MyGeometryLoader.ID, MyGeometryLoader.INSTANCE);
}
```

### Datagen

Of course, we can also [datagen] our models. To do so, we need a class that extends `CustomLoaderBuilder`:

```java
// This assumes a block model. Use ItemModelBuilder as the generic parameter instead 
// if you're making a custom item model.
public class MyLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder> {
    public MyLoaderBuilder(BlockModelBuilder parent, ExistingFileHelper existingFileHelper) {
        super(
            // Your model loader's id.
            MyGeometryLoader.ID,
            // The parent builder we use. This is always the first constructor parameter.
            parent,
            // The existing file helper we use. This is always the second constructor parameter.
            existingFileHelper,
            // Whether the loader allows inline vanilla elements as a fallback if the loader is absent.
            false
        );
    }
    
    // Add fields and setters for the fields here. The fields can then be used below.
    
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
// This assumes a BlockStateProvider. Use getBuilder("my_cool_block") directly in an ItemModelProvider.
// The parameter for customLoader() is a BiFunction. The parameters of the BiFunction
// are the result of the getBuilder() call and the provider's ExistingFileHelper.
MyLoaderBuilder loaderBuilder = models().getBuilder("my_cool_block").customLoader(MyLoaderBuilder::new);
```

Then, call your field setters on the `loaderBuilder`.

#### Visibility

The default implementation of `CustomLoaderBuilder` holds methods for applying visibility. You may choose to use or ignore the `visibility` property in your model loader. Currently, only the [composite model loader][composite] makes use of this property.

### Reusing the Default Model Loader

In some contexts, it makes sense to reuse the vanilla model loader and just building your model logic on top of that instead of outright replacing it. We can do so using a neat trick: In the model loader, we simply remove the `loader` property and send it back to the model deserializer, tricking it into thinking that it is a regular model now. We then pass it to the geometry, bake the model geometry there (like the default geometry handler would) and pass it along to the dynamic model, where we can then use the model's quads in whatever way we want:

```java
public class MyGeometryLoader implements IGeometryLoader<MyGeometry> {
    public static final MyGeometryLoader INSTANCE = new MyGeometryLoader();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(...);
    
    private MyGeometryLoader() {}
    
    @Override
    public MyGeometry read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        // Trick the deserializer into thinking this is a normal model by removing the loader field and then passing it back into the deserializer.
        jsonObject.remove("loader");
        BlockModel base = context.deserialize(jsonObject, BlockModel.class);
        // other stuff here if needed
        return new MyGeometry(base);
    }
}

public class MyGeometry implements IUnbakedGeometry<MyGeometry> {
    private final BlockModel base;

    // Store the block model for usage below.            
    public MyGeometry(BlockModel base) {
        this.base = base;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        BakedModel bakedBase = new ElementsModel(base.getElements()).bake(context, baker, spriteGetter, modelState, overrides);
        return new MyDynamicModel(bakedBase, /* other parameters here */);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        base.resolveParents(modelGetter);
    }
}

public class MyDynamicModel implements IDynamicBakedModel {
    private final BakedModel base;
    // other fields here

    public MyDynamicModel(BakedModel base, /* other parameters here */) {
        this.base = base;
        // set other fields here
    }

    // other override methods here

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();
        // Add the base model's quads. Can also do something different with the quads here, depending on what you need.
        quads.addAll(base.getQuads(state, side, rand, extraData, renderType));
        // add other elements to the quads list as needed here
        return quads;
    }
    
    // Apply the base model's transforms to our model as well.
    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return base.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }
}
```

[bakedmodel]: bakedmodel.md
[ber]: ../../../blockentities/ber.md
[bewlr]: ../../../blockentities/ber.md#blockentitywithoutlevelrenderer
[composite]: #composite-model
[datagen]: ../../index.md#data-generation
[elements]: index.md#elements
[event]: ../../../concepts/events.md#registering-an-event-handler
[model]: index.md#specification
[modeldatagen]: datagen.md
[rendertype]: index.md#render-types
[sides]: ../../../concepts/sides.md
[transform]: index.md#root-transforms
