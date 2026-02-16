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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class ClassicVeinGenerator extends VeinGenerator {

    public static final Codec<ClassicVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Layer.CODEC.fieldOf("primary").forGetter(val -> val.primary),
            Layer.CODEC.fieldOf("secondary").forGetter(val -> val.secondary),
            Layer.CODEC.fieldOf("between").forGetter(val -> val.between),
            Layer.CODEC.fieldOf("sporadic").forGetter(val -> val.sporadic),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("y_radius", 3).forGetter(val -> val.yRadius))
            .apply(instance, ClassicVeinGenerator::new));

    private Layer primary;
    private Layer secondary;
    private Layer between;
    private Layer sporadic;
    @Setter
    private int yRadius = 6;

    // Provided for readability
    private int sporadicDivisor;
    private int startPrimary;
    private int startBetween;

    @Setter
    private RuleTest[] rules;

    public ClassicVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    public ClassicVeinGenerator(Layer primary, Layer secondary, Layer between, Layer sporadic, int yRadius) {
        this.primary = primary;
        this.secondary = secondary;
        this.between = between;
        this.sporadic = sporadic;
        this.yRadius = yRadius;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        List<VeinEntry> entries = new ArrayList<>(primary.size() + secondary.size() + between.size() + sporadic.size());
        VeinGenerator.mapTarget(primary.target, primary.layers).forEach(entries::add);
        VeinGenerator.mapTarget(secondary.target, secondary.layers).forEach(entries::add);
        VeinGenerator.mapTarget(between.target, between.layers).forEach(entries::add);
        VeinGenerator.mapTarget(sporadic.target, 1).forEach(entries::add);
        return entries;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        int radius = entry.clusterSize().sample(random) / 2;
        int ySize = radius / 2;
        int xy2 = radius * radius * ySize * ySize;
        int xz2 = radius * radius * radius * radius;
        int yz2 = ySize * ySize * radius * radius;
        int xyz2 = xy2 * radius * radius;

        int xPos = origin.getX();
        int yPos = origin.getY();
        int zPos = origin.getZ();

        int max = Math.max(ySize, radius);
        int yMax = Math.min(max, yRadius);
        BlockPos minPos = new BlockPos(xPos - max, yPos - yMax, zPos - max);

        for (int xOffset = -max; xOffset <= max; xOffset++) {
            int xr = yz2 * xOffset * xOffset;
            if (xr > xyz2) continue;
            for (int yOffset = -yMax; yOffset <= yMax; yOffset++) {
                int yr = xr + xz2 * yOffset * yOffset + xy2;
                if (yr > xyz2) continue;
                if (level.isOutsideBuildHeight(yOffset + yPos))
                    continue;
                for (int zOffset = -max; zOffset <= max; zOffset++) {
                    int zr = yr + xy2 * zOffset * zOffset;
                    if (zr > xyz2) continue;

                    final var randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order
                    BlockPos currentPos = new BlockPos(xOffset + xPos, yOffset + yPos, zOffset + zPos);
                    generatedBlocks.put(currentPos,
                            (access, section) -> placeBlock(access, section, randomSeed, entry, currentPos, minPos));
                }
            }
        }
        return generatedBlocks;
    }

    private void placeBlock(BulkSectionAccess access, LevelChunkSection section, long randomSeed,
                            GTOreDefinition entry,
                            BlockPos blockPos, BlockPos lowestPos) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int x = SectionPos.sectionRelative(blockPos.getX());
        int y = SectionPos.sectionRelative(blockPos.getY());
        int z = SectionPos.sectionRelative(blockPos.getZ());

        BlockState blockState = section.getBlockState(x, y, z);
        int layer = blockPos.getY() - lowestPos.getY();

        // First try to spawn "between"
        if (layer >= startBetween && layer - startBetween + 1 <= between.layers) {
            if (random.nextFloat() <= entry.density() / 2) {
                between.place(blockState, access, section, randomSeed, entry, blockPos);
                return;
            }
        }

        // Then try primary/secondary
        if (layer >= startPrimary) {
            if (random.nextFloat() <= entry.density()) {
                primary.place(blockState, access, section, randomSeed, entry, blockPos);
                return;
            }
        } else {
            if (random.nextFloat() <= entry.density()) {
                secondary.place(blockState, access, section, randomSeed, entry, blockPos);
                return;
            }
        }

        // Then lastly, try sporadic
        if (random.nextFloat() <= entry.density() / sporadicDivisor) {
            sporadic.place(blockState, access, section, randomSeed, entry, blockPos);
        }
    }

    @Override
    public VeinGenerator build() {
        primary.layers = primary.layers == -1 ? 4 : primary.layers;
        secondary.layers = secondary.layers == -1 ? 3 : secondary.layers;
        between.layers = between.layers == -1 ? 3 : between.layers;

        // Ensure "between" is not more than the total primary and secondary layers
        Preconditions.checkArgument(primary.layers + secondary.layers >= between.layers,
                "Error: cannot have more \"between\" layers than primary and secondary layers combined!");

        this.sporadicDivisor = primary.layers + secondary.layers - 1;
        this.startPrimary = secondary.layers;
        this.startBetween = secondary.layers - between.layers / 2;
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new ClassicVeinGenerator(this.primary.copy(), this.secondary.copy(), this.between.copy(),
                this.sporadic.copy(), this.yRadius);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public ClassicVeinGenerator primary(Consumer<Layer.Builder> builder) {
        Layer.Builder layerBuilder = new Layer.Builder(
                rules != null ? rules : new RuleTest[] { AlwaysTrueTest.INSTANCE });
        builder.accept(layerBuilder);
        primary = layerBuilder.build();
        return this;
    }

    public ClassicVeinGenerator secondary(Consumer<Layer.Builder> builder) {
        Layer.Builder layerBuilder = new Layer.Builder(
                rules != null ? rules : new RuleTest[] { AlwaysTrueTest.INSTANCE });
        builder.accept(layerBuilder);
        secondary = layerBuilder.build();
        return this;
    }

    public ClassicVeinGenerator between(Consumer<Layer.Builder> builder) {
        Layer.Builder layerBuilder = new Layer.Builder(
                rules != null ? rules : new RuleTest[] { AlwaysTrueTest.INSTANCE });
        builder.accept(layerBuilder);
        between = layerBuilder.build();
        return this;
    }

    public ClassicVeinGenerator sporadic(Consumer<Layer.Builder> builder) {
        Layer.Builder layerBuilder = new Layer.Builder(
                rules != null ? rules : new RuleTest[] { AlwaysTrueTest.INSTANCE });
        builder.accept(layerBuilder);
        sporadic = layerBuilder.build();
        return this;
    }

    @AllArgsConstructor
    public static class Layer {

        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(OreConfiguration.TargetBlockState.CODEC.listOf(), GTCEuAPI.materialManager.codec())
                        .fieldOf("targets").forGetter(layer -> layer.target),
                ExtraCodecs.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("layers", -1)
                        .forGetter(layer -> layer.layers))
                .apply(instance, Layer::new));

        public final Either<List<OreConfiguration.TargetBlockState>, Material> target;
        public int layers;

        public void place(BlockState blockState, BulkSectionAccess access, LevelChunkSection section, long randomSeed,
                          GTOreDefinition entry, BlockPos pos) {
            RandomSource random = new XoroshiroRandomSource(randomSeed);
            int x = SectionPos.sectionRelative(pos.getX());
            int y = SectionPos.sectionRelative(pos.getY());
            int z = SectionPos.sectionRelative(pos.getZ());

            target.ifLeft(blockStates -> {
                for (OreConfiguration.TargetBlockState targetState : blockStates) {
                    if (!OreVeinUtil.canPlaceOre(blockState, access::getBlockState, random, entry, targetState, pos))
                        continue;
                    if (targetState.state.isAir())
                        continue;
                    section.setBlockState(x, y, z, targetState.state, false);
                    break;
                }
            }).ifRight(material -> {
                if (!OreVeinUtil.canPlaceOre(blockState, access::getBlockState, random, entry, pos))
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

        public Layer copy() {
            return new Layer(this.target.mapBoth(ArrayList::new, Function.identity()), layers);
        }

        public int size() {
            return target.left().isPresent() ? target.left().get().size() : 1;
        }

        public static class Builder {

            private Either<List<OreConfiguration.TargetBlockState>, Material> target;
            private int size = -1;
            private final RuleTest[] rules;

            protected Builder(RuleTest... rules) {
                this.rules = rules;
            }

            public Layer.Builder block(Supplier<? extends Block> block) {
                return state(block.get().defaultBlockState());
            }

            public Layer.Builder state(Supplier<? extends BlockState> state) {
                return state(state.get());
            }

            public Layer.Builder state(BlockState state) {
                this.target = Either
                        .left(Arrays.stream(this.rules).map(rule -> OreConfiguration.target(rule, state)).toList());
                return this;
            }

            public Layer.Builder mat(Material material) {
                this.target = Either.right(material);
                return this;
            }

            public Layer.Builder size(int size) {
                this.size = size;
                return this;
            }

            public Layer build() {
                return new Layer(target, size);
            }
        }
    }
}
