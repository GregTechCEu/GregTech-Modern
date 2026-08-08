---
title: Ore Generation
---


# Ore Generation

Due to Minecraft's worldgen limitations (1), GTCEu's ore vein generation does not use the native worldgen feature system.  
Instead, we have our own system of generating ore veins separately from the actual ore placement,
so that ores are only ever placed for the currently generating chunk.  
This page roughly describes the process of generating, caching and placing ores.
{ .annotate }

1. In Minecraft, worldgen features are only able to generate in a 3x3 chunk area, centered on the feature's origin chunk.  
   Because GTCEu introduces veins that may be larger than that (and have a random offset additionally),
   the ore generation would exceed the allowed area in certain situations, causing the server thread to freeze/deadlock.


The generation can be (roughly) split up into three steps:

- Vein Generation
- Generated Vein Caching
- Ore Placement (during chunk generation)

This document will cover these steps from the bottom up, starting at the chunk generation mixin (`ChunkGeneratorMixin.gtceu$applyBiomeDecoration()`).


## Chunk Generation & Ore Placement

The `ChunkGeneratorMixin` holds a reference to the `OrePlacer` (not to be confused with `OreBlockPlacer`) - which is used to place the
generated veins' blocks into the world, limited to the currently generating chunk.


## Generated Vein Caching

When trying to generate a chunk, the `OrePlacer` will query the `OreGenCache` for a list of veins surrounding the current chunk.

The radius for querying the surrounding area is determined by the `oreVeinRandomOffset` config option, as well as the largest registered vein size.  
It is therefore automatically compatible with any additional (or changed default) veins registered through either KubeJS, or by an addon.

Of course, the ore gen cache can only hold a limited amount of generated veins at once (see the `oreGenerationChunkCacheSize` config option).


### Randomness

Because veins may be removed from the cache before all of their chunks are generated, it is **extremely important** that the ore generation is fully deterministic!  

This ensures that we do not generate ore veins that are either cut off, or have a mismatch in shape or type across chunk borders.  
It also automatically applies across game restarts, keeping continuity even then.

The only situation where ore veins will differ across chunk borders (other than certain internal changes to the generation, of course), is after
the relevant config options have been changed.

In our case, that means that the `RandomSource`s used for world generation must be completely new for generating each vein, so that its type, shape, offset,
contents, etc. are not influenced by previous queries to the random generator.  
It is completely and exclusively seeded from the world's seed, as well as the chunk position.

For the random ore vein offset, we also include the vein's world generation layer in the random seed.  
This may need to include an additional component in the future, in case we add support for multiple veins per chunk and worldgen-layer.


## Vein Generation

Whenever the `OreGenCache` cannot find a vein for a specific chunk, it will request a list of that chunk's `GeneratedVein`s from the `OreGenerator`.

The `OreGenerator` is responsible for determining a vein's type, its origin (influenced by the `oreVeinRandomOffset` config option), as well as providing the appropriate
randomness source to the used implementation of `VeinGenerator`.

!!! info "Vein Origin vs Center"
    
    A vein's origin is always the chunk it originates in, regardless of the random offset.  
    The actual center of a vein **is** influenced by the random offset and might not be located at the chunk center - or in the same chunk at all.

Once the relevant `VeinGenerator` implementation has finished generating the vein's shape, it will be cached per chunk, inside a `GeneratedVein`.


### `VeinGenerator` and `OreBlockPlacer`

A vein generator is what will generate the actual shape of the vein.

It should, however, never try to place any blocks directly. Instead, its `generate()` method will only return a map of `OreBlockPlacer`s by block position, which
are responsible for actually placing the blocks in the world, as soon as a chunk generates.  
Each `OreBlockPlacer` should only place either a single block, or no block.


### Using Randomness in `OreBlockPlacer`s

In certain situations, the process of actually placing the block requires a randomness source (e.g. to determine the chance of its block being placed).

To keep the ore generation fully deterministic in this case as well, it is recommended to generate a new seed using the supplied `RandomSource` at the time of
vein shape generation. This seed should be passed into the `OreBlockPlacer` returned for the each block position.

Inside the `OreBlockPlacer`, you can then simply create a new `RandomSource` using the precomputed seed.

??? example "Using Randomness in an OreBlockPlacer"

    ```java
    public class MyVeinGenerator {
        public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin) {
            Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

            for (BlockPos pos : determineShapePositions()) {
                final var randomSeed = random.nextLong(); // Fully deterministic regardless of chunk generation order
                generatedBlocks.put(pos, (access, section) -> placeBlock(access, section, randomSeed, pos, entry));
            }

            return generatedBlocks;
        }

        private void placeBlock(BulkSectionAccess level, LevelChunkSection section, long randomSeed, BlockPos pos, GTOreDefinition entry) {
            RandomSource rand = new XoroshiroRandomSource(randomSeed);

            // ...
        }
    }
    ```