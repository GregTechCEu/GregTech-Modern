package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@AllArgsConstructor
public class CuboidVeinGenerator extends VeinGenerator {

    public static final Codec<Either<List<OreConfiguration.TargetBlockState>, Material>> LAYER_CODEC = Codec
            .either(OreConfiguration.TargetBlockState.CODEC.listOf(), GTCEuAPI.materialManager.codec());

    public static final Codec<CuboidVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LAYER_CODEC.fieldOf("top").forGetter(val -> val.top),
            LAYER_CODEC.fieldOf("middle").forGetter(val -> val.middle),
            LAYER_CODEC.fieldOf("bottom").forGetter(val -> val.bottom),
            LAYER_CODEC.fieldOf("spread").forGetter(val -> val.spread),
            Codec.INT.fieldOf("min_y").forGetter(val -> val.minY),
            Codec.INT.fieldOf("max_y").forGetter(val -> val.maxY)).apply(instance, CuboidVeinGenerator::new));

    private Either<List<OreConfiguration.TargetBlockState>, Material> top;
    private Either<List<OreConfiguration.TargetBlockState>, Material> middle;
    private Either<List<OreConfiguration.TargetBlockState>, Material> bottom;
    private Either<List<OreConfiguration.TargetBlockState>, Material> spread;

    private int minY, maxY;

    public CuboidVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    @Override
    public List<Map.Entry<Either<BlockState, Material>, Integer>> getAllEntries() {
        List<Map.Entry<Either<BlockState, Material>, Integer>> result = new ArrayList<>();
        // Entries' values are counted based on how many layers the entry is in.
        top.map(blockStates -> blockStates.stream().map(state -> Either.<BlockState, Material>left(state.state)),
                material -> Stream.of(Either.<BlockState, Material>right(material)))
                .forEach(entry -> result.add(Map.entry(entry, 2)));
        middle.map(blockStates -> blockStates.stream().map(state -> Either.<BlockState, Material>left(state.state)),
                material -> Stream.of(Either.<BlockState, Material>right(material)))
                .forEach(entry -> result.add(Map.entry(entry, 3)));
        bottom.map(blockStates -> blockStates.stream().map(state -> Either.<BlockState, Material>left(state.state)),
                material -> Stream.of(Either.<BlockState, Material>right(material)))
                .forEach(entry -> result.add(Map.entry(entry, 4)));
        spread.map(blockStates -> blockStates.stream().map(state -> Either.<BlockState, Material>left(state.state)),
                material -> Stream.of(Either.<BlockState, Material>right(material)))
                .forEach(entry -> result.add(Map.entry(entry, 7)));
        return result;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random);

        int westBound = origin.getX() - random.nextInt(size);
        int eastBound = origin.getX() + random.nextInt(size);
        int northBound = origin.getZ() - random.nextInt(size);
        int southBound = origin.getZ() + random.nextInt(size);

        int minY = this.minY;
        int startY = minY + random.nextInt(this.maxY - minY - 5);

        int topAmount = 0;
        int middleAmount = 0;
        int bottomAmount = 0;
        int spreadAmount = 0;

        for (int layerOffset = -1; layerOffset <= 7; layerOffset++) {
            int layer = startY + layerOffset;
            if (level.isOutsideBuildHeight(layer))
                continue;
            for (int x = westBound; x < eastBound; x++) {
                for (int z = northBound; z < southBound; z++) {
                    final var randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order

                    // determine density based on distance from the origin chunk
                    // this makes the vein more concentrated towards the center
                    double xLength = origin.getX() - x;
                    double zLength = origin.getZ() - z;
                    double volume = Math.sqrt(2 + (xLength * xLength) + (zLength * zLength));

                    int localDensity = (int) Math.max(1, entry.density() * volume);
                    int weightX = Math.max(1, Math.max(Mth.abs(westBound - x), Mth.abs(eastBound - x)) / localDensity);
                    int weightZ = Math.max(1,
                            Math.max(Mth.abs(southBound - z), Mth.abs(northBound - z)) / localDensity);

                    BlockPos pos = new BlockPos(x, layer, z);
                    if (layerOffset <= 1) {
                        // layers -1, 0, and 1 are bottom and spread
                        if (placeBottom(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            bottomAmount++;
                        } else if (placeSpread(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            spreadAmount++;
                        }
                    } else if (layerOffset == 2) {
                        // layer 2 is bottom, middle, and spread
                        if (placeMiddle(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            middleAmount++;
                        } else if (placeBottom(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            bottomAmount++;
                        } else if (placeSpread(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            spreadAmount++;
                        }
                    } else if (layerOffset == 3) {
                        // layer 3 is middle, and spread
                        if (placeMiddle(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            middleAmount++;
                        } else if (placeSpread(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            spreadAmount++;
                        }
                    } else if (layerOffset <= 5) {
                        // layers 4 and 5 is top, middle, and spread
                        if (placeMiddle(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            middleAmount++;
                        } else if (placeTop(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            topAmount++;
                        } else if (placeSpread(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            spreadAmount++;
                        }
                    } else {
                        // layers 6 and 7 is top and spread
                        if (placeTop(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            topAmount++;
                        } else if (placeSpread(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            spreadAmount++;
                        }
                    }
                }
            }
        }

        return generatedBlocks;
    }

    /**
     * Check if an ore should be placed
     *
     * @param random  the random to use
     * @param weightX the x weight
     * @param weightZ the z weight
     * @return if the ore should be placed
     */
    protected static boolean shouldPlaceOre(@NotNull RandomSource random, int weightX, int weightZ) {
        return random.nextInt(weightX) == 0 || random.nextInt(weightZ) == 0;
    }

    /**
     * Place the top ore
     *
     * @return if the ore was placed
     */
    private boolean placeTop(Map<BlockPos, OreBlockPlacer> generatedBlocks, GTOreDefinition entry,
                             long randomSeed, BlockPos pos,
                             RandomSource random, int weightX, int weightZ) {
        var top = this.top;
        if (shouldPlaceOre(random, weightX, weightZ)) {
            generatedBlocks.put(pos, (access, section) -> placeOre(access, section, pos, randomSeed, top, entry));
            return true;
        }
        return false;
    }

    /**
     * Place the middle ore
     *
     * @return if the ore was placed
     */
    private boolean placeMiddle(Map<BlockPos, OreBlockPlacer> generatedBlocks, GTOreDefinition entry,
                                long randomSeed, BlockPos pos,
                                RandomSource random, int weightX, int weightZ) {
        var middle = this.middle;
        if (random.nextInt(2) == 0 && shouldPlaceOre(random, weightX, weightZ)) {
            generatedBlocks.put(pos, (access, section) -> placeOre(access, section, pos, randomSeed, middle, entry));
            return true;
        }
        return false;
    }

    /**
     * Place the bottom ore
     *
     * @return if the ore was placed
     */
    private boolean placeBottom(Map<BlockPos, OreBlockPlacer> generatedBlocks, GTOreDefinition entry,
                                long randomSeed, BlockPos pos,
                                RandomSource random, int weightX, int weightZ) {
        var bottom = this.bottom;
        if (shouldPlaceOre(random, weightX, weightZ)) {
            generatedBlocks.put(pos, (access, section) -> placeOre(access, section, pos, randomSeed, bottom, entry));
            return true;
        }
        return false;
    }

    /**
     * Place the spread ore
     *
     * @return if the ore was placed
     */
    private boolean placeSpread(Map<BlockPos, OreBlockPlacer> generatedBlocks, GTOreDefinition entry,
                                long randomSeed, BlockPos pos,
                                RandomSource random, int weightX, int weightZ) {
        var spread = this.spread;
        if (random.nextInt(7) == 0 && shouldPlaceOre(random, weightX, weightZ)) {
            generatedBlocks.put(pos, (access, section) -> placeOre(access, section, pos, randomSeed, spread, entry));
            return true;
        }
        return false;
    }

    public void placeOre(BulkSectionAccess access, LevelChunkSection section, BlockPos pos, long randomSeed,
                         Either<List<OreConfiguration.TargetBlockState>, Material> ore, GTOreDefinition entry) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int x = SectionPos.sectionRelative(pos.getX());
        int y = SectionPos.sectionRelative(pos.getY());
        int z = SectionPos.sectionRelative(pos.getZ());

        BlockState existing = section.getBlockState(x, y, z);

        ore.ifLeft(blockStates -> {
            for (OreConfiguration.TargetBlockState targetState : blockStates) {
                if (!OreVeinUtil.canPlaceOre(existing, access::getBlockState, random, entry, targetState, pos))
                    continue;
                if (targetState.state.isAir())
                    continue;
                section.setBlockState(x, y, z, targetState.state, false);
                break;
            }
        }).ifRight(material -> {
            if (!OreVeinUtil.canPlaceOre(existing, access::getBlockState, random, entry, pos))
                return;
            BlockState currentState = access.getBlockState(pos);
            var prefix = ChemicalHelper.getOrePrefix(currentState);
            if (prefix.isEmpty()) return;
            Block toPlace = ChemicalHelper.getBlock(prefix.get(), material);
            if (toPlace == null || toPlace.defaultBlockState().isAir())
                return;
            section.setBlockState(x, y, z, toPlace.defaultBlockState(), false);
        });
    }

    @Override
    public VeinGenerator build() {
        return null;
    }

    @Override
    public VeinGenerator copy() {
        return null;
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return null;
    }
}
