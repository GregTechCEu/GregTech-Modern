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
        if (this.blocks != null) return this.blocks.map(blockStates -> blockStates.stream().map(state -> Either.<BlockState, Material>left(state.state)).map(entry -> Map.entry(entry, 1)).collect(Collectors.toList()), material -> List.of(Map.entry(Either.right(material), 1)));
        return List.of(Map.entry(Either.left(block.get().defaultBlockState()), 1), Map.entry(Either.left(deepBlock.get().defaultBlockState()), 1), Map.entry(Either.left(netherBlock.get().defaultBlockState()), 1));
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
        float f = random.nextFloat() * (float)Math.PI;
        float f1 = (float)entry.getClusterSize() / 8.0F;
        int i = Mth.ceil(((float)entry.getClusterSize() / 16.0F * 2.0F + 1.0F) / 2.0F);
        double d0 = origin.getX() + Math.sin(f) * f1;
        double d1 = origin.getX() - Math.sin(f) * f1;
        double d2 = origin.getZ() + Math.cos(f) * f1;
        double d3 = origin.getZ() - Math.cos(f) * f1;
        double d4 = origin.getY() + random.nextInt(3) - 2;
        double d5 = origin.getY() + random.nextInt(3) - 2;
        int k = origin.getX() - Mth.ceil(f1) - i;
        int l = origin.getY() - 2 - i;
        int i1 = origin.getZ() - Mth.ceil(f1) - i;
        int j1 = 2 * (Mth.ceil(f1) + i);
        int k1 = 2 * (2 + i);

        for(int l1 = k; l1 <= k + j1; ++l1) {
            for(int i2 = i1; i2 <= i1 + j1; ++i2) {
                if (l <= level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, l1, i2)) {
                    if (this.doPlaceNormal(level, random, entry, this.blocks, d0, d1, d2, d3, d4, d5, k, l, i1, j1, k1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean doPlaceNormal(WorldGenLevel level, RandomSource random, GTOreDefinition entry, Either<List<OreConfiguration.TargetBlockState>, Material> targets,
                                    double pMinX, double pMaxX, double pMinZ, double pMaxZ, double pMinY, double pMaxY, int pX, int pY, int pZ,
                                    int pWidth, int pHeight) {
        MutableInt placedAmount = new MutableInt(1);
        BitSet placedBlocks = new BitSet(pWidth * pHeight * pWidth);
        BlockPos.MutableBlockPos posCursor = new BlockPos.MutableBlockPos();
        int size = entry.getClusterSize();
        float density = entry.getDensity();
        double[] shape = new double[size * 4];

        for(int k = 0; k < size; ++k) {
            float f = (float)k / (float)size;
            double d0 = Mth.lerp(f, pMinX, pMaxX);
            double d1 = Mth.lerp(f, pMinY, pMaxY);
            double d2 = Mth.lerp(f, pMinZ, pMaxZ);
            double d3 = random.nextDouble() * (double)size / 16.0D;
            double d4 = ((double)(Mth.sin((float)Math.PI * f) + 1.0F) * d3 + 1.0D) / 2.0D;
            shape[k * 4] = d0;
            shape[k * 4 + 1] = d1;
            shape[k * 4 + 2] = d2;
            shape[k * 4 + 3] = d4;
        }

        for(int l3 = 0; l3 < size - 1; ++l3) {
            if (!(shape[l3 * 4 + 3] <= 0.0D)) {
                for(int i4 = l3 + 1; i4 < size; ++i4) {
                    if (!(shape[i4 * 4 + 3] <= 0.0D)) {
                        double d8 = shape[l3 * 4] - shape[i4 * 4];
                        double d10 = shape[l3 * 4 + 1] - shape[i4 * 4 + 1];
                        double d12 = shape[l3 * 4 + 2] - shape[i4 * 4 + 2];
                        double d14 = shape[l3 * 4 + 3] - shape[i4 * 4 + 3];
                        if (d14 * d14 > d8 * d8 + d10 * d10 + d12 * d12) {
                            if (d14 > 0.0D) {
                                shape[i4 * 4 + 3] = -1.0D;
                            } else {
                                shape[l3 * 4 + 3] = -1.0D;
                            }
                        }
                    }
                }
            }
        }

        BulkSectionAccess access = new BulkSectionAccess(level);

        try {
            for(int j4 = 0; j4 < size; ++j4) {
                double d9 = shape[j4 * 4 + 3];
                if (!(d9 < 0.0D)) {
                    double x = shape[j4 * 4];
                    double y = shape[j4 * 4 + 1];
                    double z = shape[j4 * 4 + 2];
                    int k4 = Math.max(Mth.floor(x - d9), pX);
                    int l = Math.max(Mth.floor(y - d9), pY);
                    int i1 = Math.max(Mth.floor(z - d9), pZ);
                    int j1 = Math.max(Mth.floor(x + d9), k4);
                    int k1 = Math.max(Mth.floor(y + d9), l);
                    int l1 = Math.max(Mth.floor(z + d9), i1);

                    for(int posX = k4; posX <= j1; ++posX) {
                        double radX = ((double)posX + 0.5D - x) / d9;
                        if (radX * radX < 1.0D) {
                            for(int posY = l; posY <= k1; ++posY) {
                                double radY = ((double)posY + 0.5D - y) / d9;
                                if (radX * radX + radY * radY < 1.0D) {
                                    for(int posZ = i1; posZ <= l1; ++posZ) {
                                        double radZ = ((double)posZ + 0.5D - z) / d9;
                                        if (radX * radX + radY * radY + radZ * radZ < 1.0D && !level.isOutsideBuildHeight(posY)) {
                                            int isPlaced = posX - pX + (posY - pY) * pWidth + (posZ - pZ) * pWidth * pHeight;
                                            if (!placedBlocks.get(isPlaced)) {
                                                placedBlocks.set(isPlaced);
                                                posCursor.set(posX, posY, posZ);
                                                if (level.ensureCanWrite(posCursor)) {
                                                    LevelChunkSection levelchunksection = access.getSection(posCursor);
                                                    if (levelchunksection != null) {
                                                        int i3 = SectionPos.sectionRelative(posX);
                                                        int j3 = SectionPos.sectionRelative(posY);
                                                        int k3 = SectionPos.sectionRelative(posZ);
                                                        BlockState blockstate = levelchunksection.getBlockState(i3, j3, k3);

                                                        if (random.nextFloat() <= density) {
                                                            targets.ifLeft(blockStates -> {
                                                                for(OreConfiguration.TargetBlockState targetState : blockStates) {
                                                                    if (GTOreFeature.canPlaceOre(blockstate, access::getBlockState, random, entry, targetState, posCursor)) {
                                                                        levelchunksection.setBlockState(i3, j3, k3, targetState.state, false);
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
                                                                levelchunksection.setBlockState(i3, j3, k3, toPlace.defaultBlockState(), false);
                                                                placedAmount.increment();
                                                            });
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable throwable1) {
            try {
                access.close();
            } catch (Throwable throwable) {
                throwable1.addSuppressed(throwable);
            }

            throw throwable1;
        }

        access.close();
        return placedAmount.getValue() > 0;
    }
}
