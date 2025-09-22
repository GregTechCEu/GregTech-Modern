package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

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
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings({ "UnusedReturnValue", "BooleanMethodIsAlwaysInverted" })
@Accessors(fluent = true, chain = true)
public class CuboidVeinGenerator extends VeinGenerator {

    public static final Codec<CuboidVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClassicVeinGenerator.Layer.CODEC.fieldOf("top").forGetter(val -> val.top),
            ClassicVeinGenerator.Layer.CODEC.fieldOf("middle").forGetter(val -> val.middle),
            ClassicVeinGenerator.Layer.CODEC.fieldOf("bottom").forGetter(val -> val.bottom),
            ClassicVeinGenerator.Layer.CODEC.fieldOf("spread").forGetter(val -> val.spread),
            Codec.INT.fieldOf("min_y").forGetter(val -> val.minY),
            Codec.INT.fieldOf("max_y").forGetter(val -> val.maxY)).apply(instance, CuboidVeinGenerator::new));

    private ClassicVeinGenerator.Layer top;
    private ClassicVeinGenerator.Layer middle;
    private ClassicVeinGenerator.Layer bottom;
    private ClassicVeinGenerator.Layer spread;

    @Setter
    private int minY, maxY;

    private int spreadDivisor;
    private int totalLayers;
    private int bottomLayer;
    private int middleLayer1;
    private int middleLayer2;
    private int topLayer;

    public CuboidVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    public CuboidVeinGenerator(ClassicVeinGenerator.Layer top, ClassicVeinGenerator.Layer middle,
                               ClassicVeinGenerator.Layer bottom, ClassicVeinGenerator.Layer spread,
                               int minY, int maxY) {
        this.top = top;
        this.middle = middle;
        this.bottom = bottom;
        this.spread = spread;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        List<VeinEntry> entries = new ArrayList<>(top.size() + middle.size() + bottom.size() + spread.size());
        VeinGenerator.mapTarget(top.target, top.layers).forEach(entries::add);
        VeinGenerator.mapTarget(middle.target, middle.layers).forEach(entries::add);
        VeinGenerator.mapTarget(bottom.target, bottom.layers).forEach(entries::add);
        VeinGenerator.mapTarget(spread.target, 1).forEach(entries::add);
        return entries;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random) / 2;

        int westBound = origin.getX() - random.nextInt(size);
        int eastBound = origin.getX() + 16 + random.nextInt(size);
        int northBound = origin.getZ() - random.nextInt(size);
        int southBound = origin.getZ() + 16 + random.nextInt(size);

        int startY = minY + random.nextInt(this.maxY - minY - 5) - 1;

        for (int layerOffset = 0; layerOffset <= totalLayers; layerOffset++) {
            int layer = startY + layerOffset;
            if (level.isOutsideBuildHeight(layer))
                continue;
            for (int x = westBound; x < eastBound; x++) {
                for (int z = northBound; z < southBound; z++) {
                    final var randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order

                    // determine density based on distance from the origin chunk
                    // this makes the vein more concentrated towards the center
                    int xLength = x - origin.getX();
                    int zLength = z - origin.getZ();
                    double volume = Math.sqrt(2 + (xLength * xLength) + (zLength * zLength));

                    int localDensity = (int) Math.max(1, 8 * entry.density() / volume);
                    int weightX = Math.max(1, Math.max(Mth.abs(westBound - x), Mth.abs(eastBound - x)) / localDensity);
                    int weightZ = Math.max(1,
                            Math.max(Mth.abs(southBound - z), Mth.abs(northBound - z)) / localDensity);

                    BlockPos pos = new BlockPos(x, layer, z);
                    if (layerOffset <= bottomLayer) {
                        // layers 0, 1, and 2 are bottom and spread
                        if (!placeBottom(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            placeSpread(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ);
                        }
                    } else if (layerOffset <= middleLayer1) {
                        // layer 3 is bottom, middle, and spread
                        if (!placeMiddle(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            if (!placeBottom(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                                placeSpread(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ);
                            }
                        }
                    } else if (layerOffset <= middleLayer2) {
                        // layer 4 is middle, and spread
                        if (!placeMiddle(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            placeSpread(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ);
                        }
                    } else if (layerOffset <= topLayer) {
                        // layers 5 and 6 is top, middle, and spread
                        if (!placeMiddle(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            if (!placeTop(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                                placeSpread(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ);
                            }
                        }
                    } else {
                        // layers 7 and 8 is top and spread
                        if (!placeTop(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ)) {
                            placeSpread(generatedBlocks, entry, randomSeed, pos, random, weightX, weightZ);
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
        var top = this.top.target;
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
        var middle = this.middle.target;
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
        var bottom = this.bottom.target;
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
        var spread = this.spread.target;
        if (random.nextFloat() <= entry.density() * spreadDivisor && shouldPlaceOre(random, weightX, weightZ)) {
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
        top.layers = top.layers == -1 ? 2 : top.layers;
        middle.layers = middle.layers == -1 ? 3 : middle.layers;
        bottom.layers = bottom.layers == -1 ? 2 : bottom.layers;

        // Ensure "middle" is not more than the total top and bottom layers
        Preconditions.checkArgument(top.layers + bottom.layers >= middle.layers,
                "Error: cannot have more \"middle\" layers than top and bottom layers combined!");

        totalLayers = top.layers + middle.layers + bottom.layers;
        bottomLayer = (int) (totalLayers / 7.0f * 2.0f);
        middleLayer1 = (int) (totalLayers / 7.0f * 3.0f);
        middleLayer2 = (int) (totalLayers / 7.0f * 4.0f);
        topLayer = (int) (totalLayers / 7.0f * 6.0f);
        spreadDivisor = (top.layers + middle.layers - 1) / 2;
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new CuboidVeinGenerator(top.copy(), middle.copy(), bottom.copy(), spread.copy(), minY, maxY);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public CuboidVeinGenerator top(Consumer<ClassicVeinGenerator.Layer.Builder> builder) {
        ClassicVeinGenerator.Layer.Builder layerBuilder = new ClassicVeinGenerator.Layer.Builder(
                AlwaysTrueTest.INSTANCE);
        builder.accept(layerBuilder);
        top = layerBuilder.build();
        return this;
    }

    public CuboidVeinGenerator middle(Consumer<ClassicVeinGenerator.Layer.Builder> builder) {
        ClassicVeinGenerator.Layer.Builder layerBuilder = new ClassicVeinGenerator.Layer.Builder(
                AlwaysTrueTest.INSTANCE);
        builder.accept(layerBuilder);
        middle = layerBuilder.build();
        return this;
    }

    public CuboidVeinGenerator bottom(Consumer<ClassicVeinGenerator.Layer.Builder> builder) {
        ClassicVeinGenerator.Layer.Builder layerBuilder = new ClassicVeinGenerator.Layer.Builder(
                AlwaysTrueTest.INSTANCE);
        builder.accept(layerBuilder);
        bottom = layerBuilder.build();
        return this;
    }

    public CuboidVeinGenerator spread(Consumer<ClassicVeinGenerator.Layer.Builder> builder) {
        ClassicVeinGenerator.Layer.Builder layerBuilder = new ClassicVeinGenerator.Layer.Builder(
                AlwaysTrueTest.INSTANCE);
        builder.accept(layerBuilder);
        spread = layerBuilder.build();
        return this;
    }
}
