package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.GTCEu.LOGGER;
import static net.minecraft.core.SectionPos.*;

@Accessors(fluent = true, chain = true)
public class CuboidVeinGenerator extends VeinGenerator {

    // spotless:off
    public static final Codec<CuboidVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClassicVeinGenerator.Layer.CODEC.fieldOf("top").forGetter(val -> val.top),
            ClassicVeinGenerator.Layer.CODEC.fieldOf("middle").forGetter(val -> val.middle),
            ClassicVeinGenerator.Layer.CODEC.fieldOf("bottom").forGetter(val -> val.bottom),
            ClassicVeinGenerator.Layer.CODEC.fieldOf("spread").forGetter(val -> val.spread),
            Codec.INT.fieldOf("min_y").forGetter(val -> val.minY),
            Codec.INT.fieldOf("max_y").forGetter(val -> val.maxY)
    ).apply(instance, CuboidVeinGenerator::new));
    // spotless:on

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
    public Long2ObjectMap<OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                   BlockPos origin) {
        Long2ObjectMap<OreBlockPlacer> generatedSections = new Long2ObjectOpenHashMap<>();

        int radius = entry.clusterSize().sample(random) / 2;

        int westBound = origin.getX() - random.nextInt(radius);
        int eastBound = origin.getX() + SECTION_SIZE + random.nextInt(radius);
        int northBound = origin.getZ() - random.nextInt(radius);
        int southBound = origin.getZ() + SECTION_SIZE + random.nextInt(radius);

        int startY = minY + random.nextInt(maxY - minY - 5) - 1;

        int sectionX0 = blockToSectionCoord(westBound), sectionX1 = blockToSectionCoord(eastBound);
        int sectionY0 = blockToSectionCoord(startY), sectionY1 = blockToSectionCoord(startY + totalLayers);
        int sectionZ0 = blockToSectionCoord(northBound), sectionZ1 = blockToSectionCoord(southBound);

        SectionPos.betweenClosedStream(sectionX0, sectionY0, sectionZ0, sectionX1, sectionY1, sectionZ1)
                .forEach(pos -> {
                    final long randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order

                    final int sectionRelativeStartY = startY - pos.minBlockY();
                    if (sectionRelativeStartY > SECTION_MAX_INDEX) {
                        // bottom Y is above this section, skip
                        // I don't think this can actually happen, but better safe than sorry
                        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
                            LOGGER.warn(
                                    "Tried to place part of cuboid vein '{}' in section {} when start Y is above section (section top: {}, vein start: {})!",
                                    GTRegistries.ORE_VEINS.getKey(entry), pos, pos.maxBlockY(), startY);
                        }
                        return;
                    } else if (sectionRelativeStartY + totalLayers < 0) {
                        // top Y is below this section, skip
                        // I don't think this can actually happen, but better safe than sorry
                        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
                            LOGGER.warn(
                                    "Tried to place part of cuboid vein '{}' in section {} when end Y is below section (section bottom: {}, vein end: {})!",
                                    GTRegistries.ORE_VEINS.getKey(entry), pos, pos.minBlockY(), startY + totalLayers);
                        }
                        return;
                    }

                    final int layerOffsetFromSectionBase;
                    final int layersInSection;
                    if (sectionRelativeStartY < 0) {
                        // layer offset is the layer it should start at
                        // for example if sectionRelativeStartY = -3, then layerOffsetFromSectionBase = 3
                        // meaning the first layer that's in this chunk section is layer 3
                        layerOffsetFromSectionBase = -sectionRelativeStartY;
                        // continuing with the same example number:
                        // ...and if the vein starts 3 blocks below this section, there should be 3 fewer layers in it
                        layersInSection = Math.min(totalLayers - layerOffsetFromSectionBase,
                                SECTION_MAX_INDEX - sectionRelativeStartY);
                    } else {
                        // the layers start in this section; the starting layer is 0
                        layerOffsetFromSectionBase = 0;
                        // ...but the end layer might go above the chunk section, so cap it
                        layersInSection = Math.min(totalLayers, SECTION_MAX_INDEX - sectionRelativeStartY);
                    }

                    generatedSections.put(pos.asLong(), (worldGenLevel, access, section, layerTarget, sectionPos) -> {
                        placeSection(worldGenLevel, access, section, layerTarget, sectionPos, origin, randomSeed,
                                entry, westBound, eastBound, northBound, southBound,
                                sectionRelativeStartY, layerOffsetFromSectionBase, layersInSection);
                    });
                });

        return generatedSections;
    }

    private void placeSection(WorldGenLevel level, BulkSectionAccess access, LevelChunkSection section,
                              RuleTest layerTarget, SectionPos sectionPos, BlockPos origin, long randomSeed,
                              GTOreDefinition entry, int westBound, int eastBound, int northBound, int southBound,
                              int sectionRelativeStartY, int layerOffset, int layersInSection) {
        final int westBoundInSection = Mth.clamp(westBound - sectionPos.minBlockX(), 0, SECTION_MAX_INDEX);
        final int eastBoundInSection = Mth.clamp(eastBound - sectionPos.minBlockX(), 0, SECTION_MAX_INDEX);
        final int northBoundInSection = Mth.clamp(northBound - sectionPos.minBlockZ(), 0, SECTION_MAX_INDEX);
        final int southBoundInSection = Mth.clamp(southBound - sectionPos.minBlockZ(), 0, SECTION_MAX_INDEX);

        RandomSource random = new XoroshiroRandomSource(randomSeed);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (; layerOffset <= layersInSection; layerOffset++) {
            // section relative Y
            int sy = sectionRelativeStartY + layerOffset;
            if (sy != sectionRelative(sy)) {
                printBlockPlacementWarning(entry, "cuboid", sectionPos, Direction.Axis.Y, sy);
                continue;
            }
            int globalY = sy + sectionPos.minBlockY();
            if (level.isOutsideBuildHeight(globalY)) continue;

            // section relative X
            for (int sx = westBoundInSection; sx <= eastBoundInSection; sx++) {
                if (sx != sectionRelative(sx)) {
                    printBlockPlacementWarning(entry, "cuboid", sectionPos, Direction.Axis.X, sx);
                    continue;
                }
                int globalX = sx + sectionPos.minBlockX();
                // section relative Z
                for (int sz = northBoundInSection; sz <= southBoundInSection; sz++) {
                    if (sz != sectionRelative(sz)) {
                        printBlockPlacementWarning(entry, "cuboid", sectionPos, Direction.Axis.Z, sz);
                        continue;
                    }
                    int globalZ = sz + sectionPos.minBlockZ();

                    // determine density based on distance from the origin chunk
                    // this makes the vein more concentrated towards the center
                    int xDistance = globalX - origin.getX();
                    int zDistance = globalZ - origin.getZ();
                    float volume = Mth.sqrt(2f + (xDistance * xDistance) + (zDistance * zDistance));

                    int localDensity = (int) Math.max(1f, 8f * entry.density() / volume);
                    int weightX = Math.max(1,
                            Math.max(Mth.abs(westBound - globalX), Mth.abs(eastBound - globalX)) / localDensity);
                    int weightZ = Math.max(1,
                            Math.max(Mth.abs(southBound - globalZ), Mth.abs(northBound - globalZ)) / localDensity);

                    pos.set(globalX, globalY, globalZ);
                    if (layerOffset <= bottomLayer) {
                        // layers 0, 1, and 2 are bottom and spread
                        if (!placeBottom(access, section, layerTarget, entry, pos, random, weightX, weightZ)) {
                            placeSpread(access, section, layerTarget, entry, pos, random, weightX, weightZ);
                        }
                    } else if (layerOffset <= middleLayer1) {
                        // layer 3 is bottom, middle, and spread
                        if (!placeMiddle(access, section, layerTarget, entry, pos, random, weightX, weightZ)) {
                            if (!placeBottom(access, section, layerTarget, entry, pos, random, weightX, weightZ)) {
                                placeSpread(access, section, layerTarget, entry, pos, random, weightX, weightZ);
                            }
                        }
                    } else if (layerOffset <= middleLayer2) {
                        // layer 4 is middle, and spread
                        if (!placeMiddle(access, section, layerTarget, entry, pos, random, weightX, weightZ)) {
                            placeSpread(access, section, layerTarget, entry, pos, random, weightX, weightZ);
                        }
                    } else if (layerOffset <= topLayer) {
                        // layers 5 and 6 is top, middle, and spread
                        if (!placeMiddle(access, section, layerTarget, entry, pos, random, weightX, weightZ)) {
                            if (!placeTop(access, section, layerTarget, entry, pos, random, weightX, weightZ)) {
                                placeSpread(access, section, layerTarget, entry, pos, random, weightX, weightZ);
                            }
                        }
                    } else {
                        // layers 7 and 8 is top and spread
                        if (!placeTop(access, section, layerTarget, entry, pos, random, weightX, weightZ)) {
                            placeSpread(access, section, layerTarget, entry, pos, random, weightX, weightZ);
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if an ore should be placed
     *
     * @param random  the random to use
     * @param weightX the x weight
     * @param weightZ the z weight
     * @return whether the ore should be placed
     */
    protected static boolean shouldPlaceOre(@NotNull RandomSource random, int weightX, int weightZ) {
        return random.nextInt(weightX) == 0 || random.nextInt(weightZ) == 0;
    }

    /**
     * Place the top ore
     *
     * @return whether the ore was placed
     */
    private boolean placeTop(BulkSectionAccess access, LevelChunkSection section, RuleTest layerTarget,
                             GTOreDefinition entry, BlockPos pos, RandomSource random, int weightX, int weightZ) {
        if (shouldPlaceOre(random, weightX, weightZ)) {
            OreVeinUtil.placeOreBlock(this.top.target, access, section, layerTarget, random, entry, pos);
            return true;
        }
        return false;
    }

    /**
     * Place the middle ore
     *
     * @return whether the ore was placed
     */
    private boolean placeMiddle(BulkSectionAccess access, LevelChunkSection section, RuleTest layerTarget,
                                GTOreDefinition entry, BlockPos pos, RandomSource random, int weightX, int weightZ) {
        if (random.nextInt(2) == 0 && shouldPlaceOre(random, weightX, weightZ)) {
            OreVeinUtil.placeOreBlock(this.middle.target, access, section, layerTarget, random, entry, pos);
            return true;
        }
        return false;
    }

    /**
     * Place the bottom ore
     *
     * @return whether the ore was placed
     */
    private boolean placeBottom(BulkSectionAccess access, LevelChunkSection section, RuleTest layerTarget,
                                GTOreDefinition entry, BlockPos pos, RandomSource random, int weightX, int weightZ) {
        if (shouldPlaceOre(random, weightX, weightZ)) {
            OreVeinUtil.placeOreBlock(this.bottom.target, access, section, layerTarget, random, entry, pos);
            return true;
        }
        return false;
    }

    /**
     * Place the spread ore
     *
     * @return whether the ore was placed
     */
    private boolean placeSpread(BulkSectionAccess access, LevelChunkSection section, RuleTest layerTarget,
                                GTOreDefinition entry, BlockPos pos, RandomSource random, int weightX, int weightZ) {
        if (random.nextFloat() <= entry.density() * spreadDivisor && shouldPlaceOre(random, weightX, weightZ)) {
            OreVeinUtil.placeOreBlock(this.spread.target, access, section, layerTarget, random, entry, pos);
            return true;
        }
        return false;
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
