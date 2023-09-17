package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeature;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StandardVeinGenerator extends VeinGenerator {
    public static final Codec<StandardVeinGenerator> CODEC_SEPARATE = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(ext -> ext.block.get()),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("deep_block").forGetter(ext -> ext.deepBlock.get()),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("nether_block").forGetter(ext -> ext.netherBlock.get())
    ).apply(instance, StandardVeinGenerator::new));

    public static final Codec<StandardVeinGenerator> CODEC_LIST = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(OreConfiguration.TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.codec()).fieldOf("targets").forGetter(ext -> ext.blocks)
    ).apply(instance, StandardVeinGenerator::new));

    public static final Codec<StandardVeinGenerator> CODEC = Codec.either(CODEC_SEPARATE, CODEC_LIST).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

    public NonNullSupplier<? extends Block> block;
    public NonNullSupplier<? extends Block> deepBlock;
    public NonNullSupplier<? extends Block> netherBlock;

    public Either<List<OreConfiguration.TargetBlockState>, Material> blocks;

    public StandardVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    public StandardVeinGenerator(Block block, Block deepBlock, Block netherBlock) {
        this.block = NonNullSupplier.of(() -> block);
        this.deepBlock = NonNullSupplier.of(() -> deepBlock);
        this.netherBlock = NonNullSupplier.of(() -> netherBlock);
    }

    public StandardVeinGenerator(Either<List<OreConfiguration.TargetBlockState>, Material> blocks) {
        this.blocks = blocks;
    }

    public StandardVeinGenerator withBlock(NonNullSupplier<? extends Block> block) {
        this.block = block;
        this.deepBlock = block;
        return this;
    }

    public StandardVeinGenerator withNetherBlock(NonNullSupplier<? extends Block> block) {
        this.netherBlock = block;
        return this;
    }

    public StandardVeinGenerator withMaterial(Material material) {
        this.blocks = Either.right(material);
        return this;
    }

    @Override
    public List<Map.Entry<Either<BlockState, Material>, Integer>> getAllEntries() {
        if (this.blocks != null) {
            return this.blocks.map(blockStates -> blockStates.stream()
                    .map(state -> Either.<BlockState, Material>left(state.state))
                    .map(entry -> Map.entry(entry, 1))
                    .collect(Collectors.toList()), material -> List.of(Map.entry(Either.right(material), 1))
            );
        } else {
            return List.of(
                    Map.entry(Either.left(block.get().defaultBlockState()), 1),
                    Map.entry(Either.left(deepBlock.get().defaultBlockState()), 1),
                    Map.entry(Either.left(netherBlock.get().defaultBlockState()), 1)
            );
        }
    }

    public VeinGenerator build() {
        if (this.blocks != null) return this;
        // if (this.blocks.left().isPresent() && !this.blocks.left().get().isEmpty()) return this;
        List<OreConfiguration.TargetBlockState> targetStates = new ArrayList<>();
        if (this.block != null) {
            targetStates.add(OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), this.block.get().defaultBlockState()));
        }

        if (this.deepBlock != null) {
            targetStates.add(OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), this.deepBlock.get().defaultBlockState()));
        }

        if (this.netherBlock != null) {
            targetStates.add(OreConfiguration.target(new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES), this.netherBlock.get().defaultBlockState()));
        }

        this.blocks = Either.left(targetStates);
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new StandardVeinGenerator(this.blocks);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    @Override
    public boolean generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin) {
        float f = random.nextFloat() * (float) Math.PI;
        float f1 = (float) entry.getClusterSize() / 8.0F;
        int i = Mth.ceil(((float) entry.getClusterSize() / 16.0F * 2.0F + 1.0F) / 2.0F);
        double minX = origin.getX() + Math.sin(f) * f1;
        double maxX = origin.getX() - Math.sin(f) * f1;
        double minZ = origin.getZ() + Math.cos(f) * f1;
        double maxZ = origin.getZ() - Math.cos(f) * f1;
        double minY = origin.getY() + random.nextInt(3) - 2;
        double maxY = origin.getY() + random.nextInt(3) - 2;
        int x = origin.getX() - Mth.ceil(f1) - i;
        int y = origin.getY() - 2 - i;
        int z = origin.getZ() - Mth.ceil(f1) - i;
        int width = 2 * (Mth.ceil(f1) + i);
        int height = 2 * (2 + i);

        for (int heightmapX = x; heightmapX <= x + width; ++heightmapX) {
            for (int heightmapZ = z; heightmapZ <= z + width; ++heightmapZ) {
                if (y > level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, heightmapX, heightmapZ))
                    continue;

                if (this.doPlaceNormal(level, random, entry, origin, this.blocks, minX, maxX, minZ, maxZ, minY, maxY, x, y, z, width, height)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean doPlaceNormal(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin,
                                    Either<List<OreConfiguration.TargetBlockState>, Material> targets,
                                    double pMinX, double pMaxX, double pMinZ, double pMaxZ, double pMinY, double pMaxY,
                                    int pX, int pY, int pZ, int pWidth, int pHeight) {
        MutableInt placedAmount = new MutableInt(1);
        BitSet placedBlocks = new BitSet(pWidth * pHeight * pWidth);
        BlockPos.MutableBlockPos posCursor = new BlockPos.MutableBlockPos();
        int size = entry.getClusterSize();
        float density = entry.getDensity();
        double[] shape = new double[size * 4];

        for (int centerOffset = 0; centerOffset < size; ++centerOffset) {
            float centerOffsetFraction = (float) centerOffset / (float) size;
            double x = Mth.lerp(centerOffsetFraction, pMinX, pMaxX);
            double y = Mth.lerp(centerOffsetFraction, pMinY, pMaxY);
            double z = Mth.lerp(centerOffsetFraction, pMinZ, pMaxZ);

            double randomOffsetModifier = random.nextDouble() * (double) size / 16.0D;
            double randomShapeOffset = ((double) (Mth.sin((float) Math.PI * centerOffsetFraction) + 1.0F) * randomOffsetModifier + 1.0D) / 2.0D;

            int shapeIdxOffset = centerOffset * 4;
            shape[shapeIdxOffset] = x;
            shape[shapeIdxOffset + 1] = y;
            shape[shapeIdxOffset + 2] = z;
            shape[shapeIdxOffset + 3] = randomShapeOffset;
        }

        for (int centerOffset = 0; centerOffset < size - 1; ++centerOffset) {
            int shapeIdxOffset1 = centerOffset * 4;
            if (shape[shapeIdxOffset1 + 3] <= 0.0D)
                continue;

            for (int i4 = centerOffset + 1; i4 < size; ++i4) {
                int shapeIdxOffset2 = i4 * 4;
                if (shape[shapeIdxOffset2 + 3] <= 0.0D)
                    continue;

                double x = shape[shapeIdxOffset1] - shape[shapeIdxOffset2];
                double y = shape[shapeIdxOffset1 + 1] - shape[shapeIdxOffset2 + 1];
                double z = shape[shapeIdxOffset1 + 2] - shape[shapeIdxOffset2 + 2];
                double randomShapeOffset = shape[shapeIdxOffset1 + 3] - shape[shapeIdxOffset2 + 3];

                if (!(randomShapeOffset * randomShapeOffset > (x * x) + (y * y) + (z * z)))
                    continue;

                if (randomShapeOffset > 0.0D) {
                    shape[shapeIdxOffset2 + 3] = -1.0D;
                } else {
                    shape[shapeIdxOffset1 + 3] = -1.0D;
                }
            }
        }

        try (BulkSectionAccess access = new BulkSectionAccess(level)) {
            for (int centerOffset = 0; centerOffset < size; ++centerOffset) {
                int shapeIdxOffset = centerOffset * 4;

                generateShape(
                        level, random, entry, origin, targets, pX, pY, pZ, pWidth, pHeight,
                        shape, shapeIdxOffset, placedBlocks, posCursor, access, density, placedAmount
                );
            }
        }

        return placedAmount.getValue() > 0;
    }

    private static void generateShape(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin,
                                      Either<List<OreConfiguration.TargetBlockState>, Material> targets,
                                      int pX, int pY, int pZ, int pWidth, int pHeight, double[] shape, int shapeIdxOffset,
                                      BitSet placedBlocks, BlockPos.MutableBlockPos posCursor, BulkSectionAccess access,
                                      float density, MutableInt placedAmount) {
        double randomShapeOffset = shape[shapeIdxOffset + 3];
        if (randomShapeOffset < 0.0D)
            return;

        double x = shape[shapeIdxOffset];
        double y = shape[shapeIdxOffset + 1];
        double z = shape[shapeIdxOffset + 2];

        int minX = Math.max(Mth.floor(x - randomShapeOffset), pX);
        int minY = Math.max(Mth.floor(y - randomShapeOffset), pY);
        int minZ = Math.max(Mth.floor(z - randomShapeOffset), pZ);
        int maxX = Math.max(Mth.floor(x + randomShapeOffset), minX);
        int maxY = Math.max(Mth.floor(y + randomShapeOffset), minY);
        int maxZ = Math.max(Mth.floor(z + randomShapeOffset), minZ);

        // Guard against generating outside the allowed 3x3 chunk area for features:
        int minXBounds = origin.getX() - 22;
        int maxXBounds = origin.getX() + 22;
        int minZBounds = origin.getZ() - 22;
        int maxZBounds = origin.getZ() + 22;

        for (int posX = minX; posX <= maxX; ++posX) {
            double radX = ((double) posX + 0.5D - x) / randomShapeOffset;
            if (!((radX * radX) < 1.0D))
                continue;

            for (int posY = minY; posY <= maxY; ++posY) {
                double radY = ((double) posY + 0.5D - y) / randomShapeOffset;
                if (!((radX * radX) + (radY * radY) < 1.0D))
                    continue;

                for (int posZ = minZ; posZ <= maxZ; ++posZ) {
                    double radZ = ((double) posZ + 0.5D - z) / randomShapeOffset;
                    if (!((radX * radX) + (radY * radY) + (radZ * radZ) < 1.0D) || level.isOutsideBuildHeight(posY))
                        continue;

                    int isPlaced = posX - pX + (posY - pY) * pWidth + (posZ - pZ) * pWidth * pHeight;
                    if (placedBlocks.get(isPlaced))
                        continue;

                    if (posX < minXBounds || posX > maxXBounds || posZ < minZBounds || posZ > maxZBounds)
                        continue;

                    placedBlocks.set(isPlaced);
                    placeBlock(level, random, entry, targets, posCursor, access, density, placedAmount, posX, posY, posZ);

                }
            }
        }
    }

    private static void placeBlock(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                   Either<List<OreConfiguration.TargetBlockState>, Material> targets,
                                   BlockPos.MutableBlockPos posCursor, BulkSectionAccess access,
                                   float density, MutableInt placedAmount, int posX, int posY, int posZ) {
        posCursor.set(posX, posY, posZ);
        if (!level.ensureCanWrite(posCursor))
            return;

        LevelChunkSection levelchunksection = access.getSection(posCursor);
        if (levelchunksection == null)
            return;

        int sectionX = SectionPos.sectionRelative(posX);
        int sectionY = SectionPos.sectionRelative(posY);
        int sectionZ = SectionPos.sectionRelative(posZ);
        BlockState blockstate = levelchunksection.getBlockState(sectionX, sectionY, sectionZ);

        if (!(random.nextFloat() <= density))
            return;

        targets.ifLeft(blockStates -> {
            for (OreConfiguration.TargetBlockState targetState : blockStates) {
                if (GTOreFeature.canPlaceOre(blockstate, access::getBlockState, random, entry, targetState, posCursor)) {
                    levelchunksection.setBlockState(sectionX, sectionY, sectionZ, targetState.state, false);
                    placedAmount.increment();
                    break;
                }
            }
        }).ifRight(material -> {
            if (!GTOreFeature.canPlaceOre(blockstate, access::getBlockState, random, entry, posCursor))
                return;
            BlockState currentState = access.getBlockState(posCursor);
            var prefix = ChemicalHelper.ORES_INVERSE.get(currentState);
            if (prefix == null) return;
            Block toPlace = ChemicalHelper.getBlock(prefix, material);
            if (toPlace == null || toPlace.defaultBlockState().isAir())
                return;
            levelchunksection.setBlockState(sectionX, sectionY, sectionZ, toPlace.defaultBlockState(), false);
            placedAmount.increment();
        });
    }
}
