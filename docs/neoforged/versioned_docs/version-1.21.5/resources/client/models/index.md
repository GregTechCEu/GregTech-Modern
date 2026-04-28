# Models

Models are JSON files that determine the visual shape and texture(s) of a block or item. A model consists of cuboid elements, each with their own size, that then get assigned a texture to each face.

Items use the associated model(s) defined by its [client item][citems] while blocks use the associated model in the [blockstate file][bsfile]. These locations are relative to the `models` directory, so a model referenced by the name `examplemod:item/example_model` would be defined by the JSON at `assets/examplemod/models/item/example_model.json`.
 
## Specification

_See also: [Model][mcwikimodel] on the [Minecraft Wiki][mcwiki]_

A model is a JSON file with the following optional properties in the root tag:

- `loader`: NeoForge-added. Sets a custom model loader. See [Model Loaders][custommodelloader] for more information.
- `parent`: Sets a parent model, in the form of a [resource location][rl] relative to the `models` folder. All parent properties will be applied and then overridden by the properties set in the declaring model. Common parents include:
    - `minecraft:block/block`: The common parent of all block models.
    - `minecraft:block/cube`: Parent of all models that use a 1x1x1 cube model.
    - `minecraft:block/cube_all`: Variant of the cube model that uses the same texture on all six sides, for example cobblestone or planks.
    - `minecraft:block/cube_bottom_top`: Variant of the cube model that uses the same texture on all four horizontal sides, and separate textures on the top and the bottom. Common examples include sandstone or chiseled quartz.
    - `minecraft:block/cube_column`: Variant of the cube model that has a side texture and a bottom and top texture. Examples include wooden logs, as well as quartz and purpur pillars.
    - `minecraft:block/cross`: Model that uses two planes with the same texture, one rotated 45° clockwise and the other rotated 45° counter-clockwise, forming an X when viewed from above (hence the name). Examples include most plants, e.g. grass, saplings and flowers.
    - `minecraft:item/generated`: Parent for classic 2D flat item models. Used by most items in the game. Ignores an `elements` block since its quads are generated from the textures.
    - `minecraft:item/handheld`: Parent for 2D flat item models that appear to be actually held by the player. Used predominantly by tools. Submodel of `item/generated`, which causes it to ignore the `elements` block as well.
    - Block items commonly (but not always) use their corresponding block models for their [item model][itemmodels]. For example, the cobblestone client item uses the `minecraft:block/cobblestone` model.
- `ambientocclusion`: Whether to enable [ambient occlusion][ao] or not. Only effective on block models. Defaults to `true`. If your custom block model has weird shading, try setting this to `false`.
- `render_type`: NeoForge-added. Sets the render type to use. See [Render Types][rendertype] for more information.
- `gui_light`: Can be `"front"` or `"side"`. If `"front"`, light will come from the front, useful for flat 2D models. If `"side"`, light will come from the side, useful for 3D models (especially block models). Defaults to `"side"`. Only effective on item models.
- `textures`: A sub-object that maps names (known as texture variables) to [texture locations][textures]. Texture variables can then be used in [elements]. They can also be specified in elements, but left unspecified in order for child models to specify them.
    - Block models should additionally specify a `particle` texture. This texture is used when falling on, running across, or breaking the block.
    - Item models can also use layer textures, named `layer0`, `layer1`, etc., where layers with a higher index are rendered above those with a lower index (e.g. `layer1` would be rendered above `layer0`). Only works if the parent is `item/generated`, and only works for up to 5 layers (`layer0` through `layer4`).
- `elements`: A list of cuboid [elements].
- `display`: A sub-object that holds the different display options for different [perspectives], see linked article for possible keys. Only effective on item models, but often specified in block models so that item models can inherit the display options. Every perspective is an optional sub-object that may contain the following options, which are applied in that order:
    - `translation`: The translation of the model, specified as `[x, y, z]`.
    - `rotation`: The rotation of the model, specified as `[x, y, z]`.
    - `scale`: The scale of the model, specified as `[x, y, z]`.
    - `right_rotation`: NeoForge-added. A second rotation that is applied after scaling, specified as `[x, y, z]`.
- `transform`: See [Root Transforms][roottransforms].

:::tip
If you're having trouble finding out how exactly to specify something, have a look at a vanilla model that does something similar.
:::

### Render Types

Using the optional NeoForge-added `render_type` field, you can set a render type for your model. If this is not set (as is the case in all vanilla models), the game will fall back to the render types hardcoded in `ItemBlockRenderTypes`. If `ItemBlockRenderTypes` doesn't contain the render type for that block either, it will fall back to `minecraft:solid`. Vanilla provides the following default render types:

- `minecraft:solid`: Used for fully solid blocks, such as stone.
- `minecraft:cutout`: Used for blocks where any pixel is either fully solid or fully transparent, i.e. with either full or no transparency, for example glass.
- `minecraft:cutout_mipped`: Variant of `minecraft:cutout` that will scale down textures at large distances to avoid visual artifacts ([mipmapping]). Does not apply the mipmapping to item rendering, as it is usually undesired on items and may cause artifacts. Used for example by leaves.
- `minecraft:cutout_mipped_all`: Variant of `minecraft:cutout_mipped` which applies mipmapping to item models as well.
- `minecraft:translucent`: Used for blocks where any pixel may be partially transparent, for example stained glass.
- `minecraft:tripwire`: Used by blocks with the special requirement of being rendered to the weather target, i.e. tripwire.
- `neoforge:item_unlit`: NeoForge-added. Should be used by blocks that, when rendered from an item, do not take the light directions into account.

Selecting the correct render type is a question of performance to some degree. Solid rendering is faster than cutout rendering, and cutout rendering is faster than translucent rendering. Because of this, you should specify the "strictest" render type applicable for your use case, as it will also be the fastest.

If you want, you can also add your own render types. To do so, subscribe to the [mod bus][modbus] [event] `RegisterNamedRenderTypesEvent` and `#register` your render types. `#register` has three parameters:

- The name of the render type. Should be a `ResourceLocation` prefixed with your mod id.
- The chunk render type. Any of the types in the list returned by `RenderType.chunkBufferLayers()` can be used.
- The entity render type. Must be a render type with the `DefaultVertexFormat.NEW_ENTITY` vertex format.

### Elements

An element is a JSON representation of a cuboid object. It has the following properties:

- `from`: The coordinate of the start corner of the cuboid, specified as `[x, y, z]`. Specified in 1/16 block units. For example, `[0, 0, 0]` would be the "bottom left" corner, `[8, 8, 8]` would be the center, and `[16, 16, 16]` would be the "top right" corner of the block.
- `to`: The coordinate of the end corner of the cuboid, specified as `[x, y, z]`. Like `from`, this is specified in 1/16 block units.

:::tip
Values in `from` and `to` are limited by Minecraft to the range `[-16, 32]`. However, it is highly discouraged to exceed `[0, 16]`, as that will lead to lighting and/or culling issues.
:::

- `neoforge_data`: See [Extra Face Data][extrafacedata].
- `faces`: An object containing data for of up to 6 faces, named `north`, `south`, `east`, `west`, `up` and `down`, respectively. Every face has the following data:
    - `uv`: The uv of the face, specified as `[u1, v1, u2, v2]`, where `u1, v1` is the top left uv coordinates and `u2, v2` is the bottom right uv coordinates.
    - `texture`: The texture to use for the face. Must be a texture variable prefixed with a `#`. For example, if your model had a texture named `wood`, you would use `#wood` to reference that texture. Technically optional, will use the missing texture if absent.
    - `rotation`: Optional. Rotates the texture clockwise by 90, 180 or 270 degrees.
    - `cullface`: Optional. Tells the render engine to skip rendering the face when there is a full block touching it in the specified direction. The direction can be `north`, `south`, `east`, `west`, `up` or `down`.
    - `tintindex`: Optional. Specifies a tint index that may be used by a color handler, see [Tinting][tinting] for more information. Defaults to -1, which means no tinting.
    - `neoforge_data`: See [Extra Face Data][extrafacedata].

Additionally, it can specify the following optional properties:

- `shade`: Only for block models. Optional. Whether the faces of this element should have direction-dependent shading on it or not. Defaults to true.
- `rotation`: A rotation of the object, specified as a sub object containing the following data:
    - `angle`: The rotation angle, in degrees. Can be -45 through 45 in steps of 22.5 degrees.
    - `axis`: The axis to rotate around. It is currently not possible to rotate an object around more than one axis.
    - `origin`: Optional. The origin point to rotate around, specified as `[x, y, z]`. Note that these are absolute values, i.e. they are not relative to the cube's position. If unspecified, will use `[0, 0, 0]`.

#### Extra Face Data

Extra face data (`neoforge_data`) can be applied to both an element and a single face of an element. It is optional in all contexts where it is available. If both element-level and face-level extra face data is specified, the face-level data will override the element-level data. Extra data can specify the following data:

- `color`: Tints the face with the given color. Must be an ARGB value. Can be specified as a string or as a decimal integer (JSON does not support hex literals). Defaults to `0xFFFFFFFF`. This can be used as a replacement for tinting if the color values are constant.
- `block_light`: Overrides the block light value used for this face. Defaults to 0.
- `sky_light`: Overrides the sky light value used for this face. Defaults to 0.
- `ambient_occlusion`: Disables or enables ambient occlusion for this face. Defaults to the value set in the model.

### Root Transforms

Adding the `transform` property at the top level of a model tells the loader that a transformation to all geometry should be applied right before the rotations in a [blockstate file][bsfile] (for block models) or the transformations in a `display` block (for item models) are applied. This is added by NeoForge.

The root transforms can be specified in two ways. The first way would be as a single property named `matrix` containing a transformation 3x4 matrix (row major order, last row is omitted) in the form of a nested JSON array. The matrix is the composition of the translation, left rotation, scale, right rotation and the transformation origin in that order. An example would look like this:

```json5
{
    // ...
    "transform": {
        "matrix": [
            [0, 0, 0, 0],
            [0, 0, 0, 0],
            [0, 0, 0, 0]
        ]
    }
}
```

The second way is to specify a JSON object containing any combination of the following entries, applied in that order:

- `translation`: The relative translation. Specified as a three-dimensional vector (`[x, y, z]`) and defaults to `[0, 0, 0]` if absent.
- `rotation` or `left_rotation`: Rotation around the translated origin to be applied before scaling. Defaults to no rotation. Specified in one of the following ways:
    - A JSON object with a single axis to rotation mapping, e.g. `{"x": 90}`
    - An array of JSON objects with a single axis to rotation mapping each, applied in the order they are specified in, e.g. `[{"x": 90}, {"y": 45}, {"x": -22.5}]`
    - An array with three values that each specify the rotation around each axis, e.g. `[90, 45, -22.5]`
    - An array with four values directly specifying a quaternion, e.g. `[0.38268346, 0, 0, 0.9238795]` (= 45 degrees around the X axis)
- `scale`: The scale relative to the translated origin. Specified as a three-dimensional vector (`[x, y, z]`) and defaults to `[1, 1, 1]` if absent.
- `post_rotation` or `right_rotation`: Rotation around the translated origin to be applied after scaling. Defaults to no rotation. Specified the same as `rotation`.
- `origin`: Origin point used for rotation and scaling. The transformation is also moved here as a final step. Specified either as a three-dimensional vector (`[x, y, z]`) or using one of the three builtin values `"corner"` (= `[0, 0, 0]`), `"center"` (= `[0.5, 0.5, 0.5]`) or `"opposing-corner"` (= `[1, 1, 1]`, default).

## Blockstate Files

_See also: [Blockstate files][mcwikiblockstate] on the [Minecraft Wiki][mcwiki]_

Blockstate files are used by the game to assign different models to different [blockstates]. There must be exactly one blockstate file per block registered to the game. Specifying block models for blockstates works in three mutually exclusive ways: via variants, multipart, or the NeoForge added definition type.

Inside a `variants` block, there is an element for each blockstate. This is the predominant way of associating blockstates with models, used by the vast majority of blocks.
- The key is the string representation of the blockstate without the block name, so for example `"type=top,waterlogged=false"` for a non-waterlogged top slab, or `""` for a block with no properties. It is worth noting that unused properties may be omitted. For example, if the `waterlogged` property has no influence on the model chosen, two objects `type=top,waterlogged=false` and `type=top,waterlogged=true` may be collapsed into one `type=top` object. This also means that an empty string is valid for every block.
- The value is either a single model object or an array of model objects. If an array of model objects is used, a model will be randomly chosen from it. A model object consists of the following data:
    - `type`: NeoForge-added. Sets a custom block state model loader. See [Block State Model Loaders][bsmmodelloader] for more information.
    - `model`: A path to a model file location, relative to the namespace's `models` folder, for example `minecraft:block/cobblestone`.
    - `x` and `y`: Rotation of the model on the x-axis/y-axis. Limited to steps of 90 degrees. Optional each, defaults to 0.
    - `uvlock`: Whether to lock the UVs of the model when rotating or not. Optional, defaults to false.
    - `weight`: Only useful with arrays of model objects. Gives the object a weight, used when choosing a random model object. Optional, defaults to 1.

In contrast, inside a `multipart` block, elements are combined depending on the properties of the blockstate. This method is mainly used by fences and walls, who enable the four directional parts based on boolean properties. A multipart element consists of two parts: a `when` block and an `apply` block.

- The `when` block specifies either a string representation of a blockstate or a list of properties that must be met for the element to apply. The lists can either be named `"OR"` or `"AND"`, performing the respective logical operation on its contents. Both single blockstate and list values can additionally specify multiple actual values by separating them with `|` (for example `facing=east|facing=west`).
- The `apply` block specifies the model object or an array of model objects to use. This works exactly like with a `variants` block.

Finally, a `neoforge:definition_type` can specify a custom model loader to register the block state file See [Block State Definition Loaders][bsdmodelloader] for more information.

## Client Items

[Client items][citems] are used by the game to assign a model or multiple models to the states of the `ItemStack`. While there are some item-specific fields in the model JSON, client items consume models to render based on context, so most of their information has been moved to their own separate [section][citems].

## Tinting

Some blocks, such as grass or leaves, change their texture color based on their location and/or properties. [Model elements][elements] can specify a tint index on their faces, which will allow a color handler to handle the respective faces. The code side of things works through three events, one for block color handlers, one for block tints based on biome (used in conjunction with the block color handlers), and one for item tint sources. They work pretty similar, so let's have a look at a block handler first:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
    // Parameters are the block's state, the level the block is in, the block's position, and the tint index.
    // The level and position may be null.
    event.register((state, level, pos, tintIndex) -> {
        // Replace with your own calculation. See the BlockColors class for vanilla references.
        // Colors are in ARGB format. Generally, if the tint index is -1, it means that no tinting
        // should take place and a default value should be used instead.
        return 0xFFFFFFFF;
    },
    // A varargs of blocks to apply the tinting to
    EXAMPLE_BLOCK.get(), ...);
}
```

Here is an example for a color resolver:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerColorResolvers(RegisterColorHandlersEvent.ColorResolvers event) {
    // Parameters are the current biome, the block's X position, and the block's Z position.
    event.register((biome, x, z) -> {
        // Replace with your own calculation. See the BiomeColors class for vanilla references.
        // Colors are in ARGB format.
        return 0xFFFFFFFF;
    });
}
```

For item tinting, please see the [relevant section in the client items article][itemtints].

## Registering Standalone Models

Models that are not associated with a block or item in some way, but are still required in other contexts (e.g. [block entity renderers][ber]), can be registered through `ModelEvent.RegisterStandalone`:

```java
// This can be any type as long as it can be obtained from the ResolvedModel and the ModelBaker
public static final StandaloneModelKey<QuadCollection> EXAMPLE_KEY = new StandaloneModelKey<>(
    // The model id, relative to `assets/<namespace>/models/<path>.json`
    ResourceLocation.fromNamespaceAndPath("examplemod", "block/example_unused_model")
);

@SubscribeEvent // on the mod event bus only on the physical client
public static void registerAdditional(ModelEvent.RegisterStandalone event) {
    event.register(
        // The model to get
        EXAMPLE_KEY,
        // The data of the model we care about, in this case the baked quads to render
        StandaloneModelBaker.quadCollection()
    );
}
```

[ao]: https://en.wikipedia.org/wiki/Ambient_occlusion
[ber]: ../../../blockentities/ber.md
[bsfile]: #blockstate-files
[bsdmodelloader]: modelloaders.md#block-state-definition-loaders
[bsmmodelloader]: modelloaders.md#block-state-model-loaders
[custommodelloader]: modelloaders.md#model-loaders
[elements]: #elements
[event]: ../../../concepts/events.md
[extrafacedata]: #extra-face-data
[citems]: items.md
[itemmodel]: items.md#a-basic-model
[itemtints]: items.md#tinting
[mcwiki]: https://minecraft.wiki
[mcwikiblockstate]: https://minecraft.wiki/w/Tutorials/Models#Block_states
[mcwikimodel]: https://minecraft.wiki/w/Model
[mipmapping]: https://en.wikipedia.org/wiki/Mipmap
[modbus]: ../../../concepts/events.md#event-buses
[perspectives]: modelsystem.md#perspectives
[rendertype]: #render-types
[roottransforms]: #root-transforms
[rl]: ../../../misc/resourcelocation.md
[textures]: ../textures.md
[tinting]: #tinting
