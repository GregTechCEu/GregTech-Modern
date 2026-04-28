# Textures

All textures in Minecraft are PNG files located within a namespace's `textures` folder. JPG, GIF and other image formats are not supported. The path of [identifiers] referring to textures is generally relative to the `textures` folder, so for example, the identifier `examplemod:block/example_block` refers to the texture file at `assets/examplemod/textures/block/example_block.png`.

Textures should generally be in sizes that are powers of two, for example 16x16 or 32x32. Unlike older versions, modern Minecraft natively supports block and item texture sizes greater than 16x16. For textures that are not in powers of two that you render yourself anyway (for example GUI backgrounds), create an empty file in the next available power-of-two size (often 256x256), and add your texture in the top left corner of that file, leaving the rest of the file empty. The actual size of the drawn texture can then be set in the code that uses the texture.

## Texture Metadata

Texture metadata can be specified in a file named exactly the same as the texture, with an additional `.mcmeta` suffix. For example, an animated texture at `textures/block/example.png` would need an accompanying `textures/block/example.png.mcmeta` file. The `.mcmeta` file has the following format (all optional):

```json5
{
    // Metadata for a general texture
    "texture": {
        // Whether the texture will be blurred if needed. Defaults to false.
        // Currently specified by the codec, but unused otherwise both in the files and in code.
        "blur": true,
        // Whether the texture will be clamped if needed. Defaults to false.
        // Currently specified by the codec, but unused otherwise both in the files and in code.
        "clamp": true,
        // Sets the strategy used when generating mipmaps (lower resolutions of textures used at
        // a distance).
        // Can either be:
        // - `mean`: The default that averages the color between four pixels.
        // - `cutout`: 'mean', except that all levels are generated from the original texture
        // rather than the close mipmap, with alpha value snapped to 0 or 1 using a threshold
        // of 0.2.
        // - `strict_cutout`: 'cutout', except the alpha value snaps using a threshold of 0.6.
        // - `dark_cutout`: 'mean', except that the surrounding pixels are only included in the
        // average if their alpha is not 0.
        "mipmap_strategy": "mean",
        // Offsets the cutoff alpha when determining whether a pixel should be made either fully
        // opaque or transparent for mipmaps. For example, setting to 0.3 with the 'cutout' strategy
        // changes the alpha value snap to 0.2 + 0.3 = 0.5.
        "alpha_cutoff_bias": 0.3
    },

    // Metadata for a texture used as a gui sprite
    "gui": {
        // Specifies how the texture will be scaled if needed. Can be one of these three:
        "scaling": {
            "type": "stretch" // default
        },
        "scaling": {
            "type": "tile",
            "width": 16,
            "height": 16
        },
        "scaling": {
            // Like "tile", but allows specifying the border offsets.
            "type": "nine_slice",
            "width": 16,
            "height": 16,
            // May also be a single int that is used as the value for all four sides.
            "border": {
                "left": 0,
                "top": 0,
                "right": 0,
                "bottom": 0
            },
            // When true the center part of the texture will be applied like
            // the stretch type instead of a nine slice tiling.
            "stretch_inner": true
        }
    },

    // Metadata for an animated texture
    // See below
    "animation": {}
}
```

## Animated Textures

Minecraft natively supports animated textures for blocks and items. Animated textures consist of a texture file where the different animation stages are located below each other (for example, an animated 16x16 texture with 8 phases would be represented through a 16x128 PNG file).

To actually be animated and not just be displayed as a distorted texture, there must be an `animation` object in the texture metadata. The sub-object can be empty, but may contain the following optional entries:

```json5
{
    "animation": {
        // A custom order in which the frames are played. If omitted, the frames are played top to bottom.
        "frames": [1, 0],
        // How long one frame stays before switching to the next animation stage, in frames. Defaults to 1.
        "frametime": 5,
        // Whether to interpolate between animation stages. Defaults to false.
        "interpolate": true,
        // Width and height of one animation stage. If omitted, uses the texture width for both of these.
        "width": 12,
        "height": 12
    }
}
```

[identifiers]: ../../misc/identifier.md
