package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.codec.GTCodecUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;
import java.util.function.Function;

import static net.minecraft.core.SectionPos.blockToSectionCoord;

public class StandardVeinGenerator extends VeinGenerator {

    // spotless:off
    @SuppressWarnings("deprecation")
    private static final Codec<StandardVeinGenerator> CODEC_SEPARATE = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(ext -> ext.block.get()),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("deep_block").forGetter(ext -> ext.deepBlock.get()),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("nether_block").forGetter(ext -> ext.netherBlock.get())
    ).apply(instance, StandardVeinGenerator::new));
    private static final Codec<StandardVeinGenerator> CODEC_LIST = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(OreConfiguration.TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.codec()).fieldOf("targets").forGetter(ext -> ext.blocks)
    ).apply(instance, StandardVeinGenerator::new));

    public static final Codec<StandardVeinGenerator> CODEC = Codec.either(CODEC_SEPARATE, CODEC_LIST)
            .xmap(GTCodecUtils::unboxEither, Either::left);
    // spotless:on

    private static final int SPREAD_Y = 2;

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

    private List<VeinEntry> defaultEntries = null;

    private List<VeinEntry> getDefaultEntries() {
        if (defaultEntries == null) {
            defaultEntries = List.of(
                    VeinEntry.ofBlock(block.get().defaultBlockState(), 1),
                    VeinEntry.ofBlock(deepBlock.get().defaultBlockState(), 1),
                    VeinEntry.ofBlock(netherBlock.get().defaultBlockState(), 1));
        }
        return defaultEntries;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        if (this.blocks != null) {
            return VeinGenerator.mapTarget(blocks, 1).toList();
        } else {
            return getDefaultEntries();
        }
    }

    public VeinGenerator build() {
        if (this.blocks != null) return this;
        // if (this.blocks.left().isPresent() && !this.blocks.left().get().isEmpty()) return this;
        List<OreConfiguration.TargetBlockState> targetStates = new ArrayList<>();
        if (this.block != null) {
            targetStates.add(OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                    this.block.get().defaultBlockState()));
        }

        if (this.deepBlock != null) {
            targetStates.add(OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                    this.deepBlock.get().defaultBlockState()));
        }

        if (this.netherBlock != null) {
            targetStates.add(OreConfiguration.target(new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES),
                    this.netherBlock.get().defaultBlockState()));
        }

        this.blocks = Either.left(targetStates);
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new StandardVeinGenerator(this.blocks.mapBoth(ArrayList::new, Function.identity()));
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    @Override
    public Long2ObjectMap<OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                   BlockPos origin) {
        Long2ObjectMap<OreBlockPlacer> generatedSections = new Long2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random);

        float dir = random.nextFloat() * (float) Math.PI;
        float spreadXZ = size / 8.0F;
        int maxRadius = Mth.ceil((size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double xOffset = Math.sin(dir) * spreadXZ;
        double zOffset = Math.cos(dir) * spreadXZ;
        double x0 = origin.getX() + xOffset;
        double x1 = origin.getX() - xOffset;
        double z0 = origin.getZ() + zOffset;
        double z1 = origin.getZ() - zOffset;
        double y0 = origin.getY() - random.nextInt(3) + SPREAD_Y;
        double y1 = origin.getY() + random.nextInt(3) - SPREAD_Y;

        int xStart = origin.getX() - Mth.ceil(spreadXZ) - maxRadius;
        int yStart = origin.getY() - SPREAD_Y - maxRadius;
        int zStart = origin.getZ() - Mth.ceil(spreadXZ) - maxRadius;
        int sizeXZ = 2 * (Mth.ceil(spreadXZ) + maxRadius);
        int sizeY = 2 * (SPREAD_Y + maxRadius);

        BitSet tested = new BitSet(sizeXZ * sizeY * sizeXZ);
        double[] data = new double[size * 4];
        for (int i = 0; i < size; ++i) {
            double step = (double) i / size;
            double xx = Mth.lerp(step, x0, x1);
            double yy = Mth.lerp(step, y0, y1);
            double zz = Mth.lerp(step, z0, z1);

            double ss = random.nextDouble() * (double) size / 16.0D;
            double randomShapeOffset = ((Math.sin(Math.PI * step) + 1.0D) * ss + 1.0D) / 2.0D;

            // noinspection PointlessArithmeticExpression
            data[i * 4 + 0] = xx;
            data[i * 4 + 1] = yy;
            data[i * 4 + 2] = zz;
            data[i * 4 + 3] = randomShapeOffset;
        }

        for (int i = 0; i < size - 1; ++i) {
            if (data[i * 4 + 3] <= 0.0D) continue;

            for (int j = i + 1; j < size; ++j) {
                if (data[j * 4 + 3] <= 0.0D) continue;

                // noinspection PointlessArithmeticExpression
                double dx = data[i * 4 + 0] - data[j * 4 + 0];
                double dy = data[i * 4 + 1] - data[j * 4 + 1];
                double dz = data[i * 4 + 2] - data[j * 4 + 2];
                double dRandomShapeOffset = data[i * 4 + 3] - data[j * 4 + 3];

                if (dRandomShapeOffset * dRandomShapeOffset <= (dx * dx) + (dy * dy) + (dz * dz)) {
                    continue;
                }

                if (dRandomShapeOffset > 0.0D) {
                    data[j * 4 + 3] = -1.0D;
                } else {
                    data[i * 4 + 3] = -1.0D;
                }
            }
        }

        SectionPos.betweenClosedStream(blockToSectionCoord(x0), blockToSectionCoord(y0), blockToSectionCoord(z0),
                blockToSectionCoord(x1), blockToSectionCoord(y1), blockToSectionCoord(z1))
                .forEach(pos -> {
                    final long randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order
                    generatedSections.put(pos.asLong(), (worldGenLevel, access, section, sectionPos) -> {
                        placeSection(worldGenLevel, access, section, sectionPos, entry, size, randomSeed, data,
                                xStart, yStart, zStart, sizeXZ, sizeY, tested);
                    });
                });

        return generatedSections;
    }

    private void placeSection(WorldGenLevel level, BulkSectionAccess access, LevelChunkSection section,
                              SectionPos sectionPos, GTOreDefinition entry, int sampledSize, long randomSeed,
                              double[] data, int xStart, int yStart, int zStart, int sizeXZ, int sizeY, BitSet tested) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        BlockPos.MutableBlockPos orePos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < sampledSize; i++) {
            double randomShapeOffset = data[i * 4 + 3];
            if (randomShapeOffset < 0.0) continue;

            // noinspection PointlessArithmeticExpression
            double xx = data[i * 4 + 0];
            double yy = data[i * 4 + 1];
            double zz = data[i * 4 + 2];
            int xMin = Math.max(Mth.floor(xx - randomShapeOffset), xStart);
            int yMin = Math.max(Mth.floor(yy - randomShapeOffset), yStart);
            int zMin = Math.max(Mth.floor(zz - randomShapeOffset), zStart);
            int xMax = Math.max(Mth.floor(xx + randomShapeOffset), xMin);
            int yMax = Math.max(Mth.floor(yy + randomShapeOffset), yMin);
            int zMax = Math.max(Mth.floor(zz + randomShapeOffset), zMin);

            for (int x = xMin; x <= xMax; x++) {
                if (blockToSectionCoord(x) != sectionPos.x()) continue;
                double xd = (x + 0.5D - xx) / randomShapeOffset;
                if ((xd * xd) >= 1.0D) continue;

                for (int y = yMin; y <= yMax; y++) {
                    if (blockToSectionCoord(y) != sectionPos.y()) continue;
                    double yd = (y + 0.5D - yy) / randomShapeOffset;
                    if ((xd * xd + yd * yd) >= 1.0D) continue;
                    if (level.isOutsideBuildHeight(y)) continue;

                    for (int z = zMin; z <= zMax; z++) {
                        if (blockToSectionCoord(z) != sectionPos.z()) continue;
                        double zd = (z + 0.5D - zz) / randomShapeOffset;
                        if ((xd * xd + yd * yd + zd * zd) >= 1.0D) continue;

                        int bitSetIndex = x - xStart + (y - yStart) * sizeXZ + (z - zStart) * sizeXZ * sizeY;
                        if (tested.get(bitSetIndex)) continue;
                        tested.set(bitSetIndex);

                        if (random.nextFloat() > entry.density()) return;

                        orePos.set(x, y, z);

                        OreVeinUtil.placeOreBlock(this.blocks, access, section, random, entry, orePos);
                    }
                }
            }
        }
    }
}
