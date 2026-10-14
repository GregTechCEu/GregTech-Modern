---
title: Creating Connected Textures
---

**Connected textures** are, as the name would imply, textures that connect with neighboring blocks.

The CTM renderer will draw the block faces by assembling 4 quadrants from the 5 available block textures.
The normal `texture.png` is the block's "unconnected" texture, and is used when CTM is disabled or the block
has nothing to connect to.  
`texture.png` has the outside corner quadrants and `texture_ctm.png` contains the connections.
```
┌─────────────────┐ ┌────────────────────────────────┐
│ texture.png     │ │ texture_ctm.png                │
│ ╔══════╤══════╗ │ │  ──────┼────── ║ ─────┼───── ║ │
│ ║      │      ║ │ │ │      │      │║      │      ║ │
│ ║ 4/4  │ 4/5  ║ │ │ │ 0/0  │ 0/1  │║ 0/2  │ 0/3  ║ │
│ ╟──────┼──────╢ │ │ ┼──────┼──────┼╟──────┼──────╢ │
│ ║      │      ║ │ │ │      │      │║      │      ║ │
│ ║ 5/4  │ 5/5  ║ │ │ │ 1/0  │ 1/1  │║ 1/2  │ 1/3  ║ │
│ ╚══════╧══════╝ │ │  ──────┼────── ║ ─────┼───── ║ │
└─────────────────┘ │ ═══════╤═══════╝ ─────┼───── ╚ │
                    │ │      │      ││      │      │ │
                    │ │ 2/0  │ 2/1  ││ 2/2  │ 2/3  │ │
                    │ ┼──────┼──────┼┼──────┼──────┼ │
                    │ │      │      ││      │      │ │
                    │ │ 3/0  │ 3/1  ││ 3/2  │ 3/3  │ │
                    │ ═══════╧═══════╗ ─────┼───── ╔ │
                    └────────────────────────────────┘
```

For example, combining sections 4/4, 2/1, 5/4, and 3/1, we can generate a texture connected to the right!
```
╔══════╤═══════
║      │      │
║ 4/4  │ 2/1  │
╟──────┼──────┼
║      │      │
║ 5/4  │ 3/1  │
╚══════╧═══════
```  
Combining sections 0/2, 2/3, 5/4, and 3/1, we can generate a texture in the shape of an L (connected to the right and up):
```
║ ─────┼───── ╚
║      │      │
║ 0/2  │ 2/3  │
╟──────┼──────┼
║      │      │
║ 5/4  │ 3/1  │
╚══════╧═══════
```


??? example "Example MCMeta file"
    (For a texture `mypack/assets/textures/blocks/texture.png` with a ctm texture `mypack/assets/textures/blocks/texture_ctm.png`)
    ```json title="mypack:blocks/texture.png.mcmeta"
    {
        "gtceu": {
            "connection_texture": "mypack:blocks/texture_ctm"
        }
    }
    ```
    The CTM texture layout is [here](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/resources/assets/gtceu/textures/block/ctm_test.png) for the unconnected texture and [here](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/resources/assets/gtceu/textures/block/ctm_test_ctm.png) for its connections.
    Its MCMeta metadata file is [this one](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/resources/assets/gtceu/textures/block/ctm_test.png.mcmeta).
