package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

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
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class ClassicVeinGenerator extends VeinGenerator {

    // spotless:off
    public static final Codec<ClassicVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Layer.CODEC.fieldOf("primary").forGetter(val -> val.primary),
            Layer.CODEC.fieldOf("secondary").forGetter(val -> val.secondary),
            Layer.CODEC.fieldOf("between").forGetter(val -> val.between),
            Layer.CODEC.fieldOf("sporadic").forGetter(val -> val.sporadic),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("y_radius", 3).forGetter(val -> val.yRadius)
    ).apply(instance, ClassicVeinGenerator::new));
    // spotless:on

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
    public Long2ObjectMap<OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                   BlockPos origin) {
        Long2ObjectMap<OreBlockPlacer> generatedSections = new Long2ObjectOpenHashMap<>();

        int xzRadius = entry.clusterSize().sample(random) / 2;

        int xzRadiusSqr = xzRadius * xzRadius;
        int yRadiusSqr = this.yRadius * this.yRadius;
        int xySqr = xzRadiusSqr * yRadiusSqr;
        int xzSqr = xzRadiusSqr * xzRadiusSqr;
        int xyzSqr = xzSqr * yRadiusSqr;

        int xPos = origin.getX();
        int yPos = origin.getY();
        int zPos = origin.getZ();

        int sectionX = SectionPos.blockToSectionCoord(xPos);
        int sectionY = SectionPos.blockToSectionCoord(yPos);
        int sectionZ = SectionPos.blockToSectionCoord(zPos);

        SectionPos.betweenClosedStream(sectionX - xzRadius, sectionY - this.yRadius, sectionZ - xzRadius,
                sectionX + xzRadius, sectionY + this.yRadius, sectionZ + xzRadius)
                .forEach(pos -> {
                    final long randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order
                    generatedSections.put(pos.asLong(), (worldGenLevel, access, section, layerTarget, sectionPos) -> {
                        placeSection(worldGenLevel, access, section, layerTarget, randomSeed, entry, sectionPos, origin,
                                xzRadius, xySqr, xzSqr, xyzSqr);
                    });
                });

        return generatedSections;
    }

    private void placeSection(WorldGenLevel level, BulkSectionAccess access, LevelChunkSection section,
                              RuleTest layerTarget, long randomSeed, GTOreDefinition entry, SectionPos sectionPos,
                              BlockPos origin, int xzRadius, int xySqr, int xzSqr, int xyzSqr) {
        int xOrigin = origin.getX();
        int yOrigin = origin.getY();
        int zOrigin = origin.getZ();

        RandomSource random = new XoroshiroRandomSource(randomSeed);

        BlockPos.MutableBlockPos pos = origin.mutable();

        // TODO optimize these loops to not have all the useless iteration
        for (int dx = -xzRadius; dx <= xzRadius; dx++) {
            int xr = xzSqr * dx * dx;
            if (xr > xyzSqr) continue;
            int x = dx + xOrigin;
            if (SectionPos.blockToSectionCoord(x) != sectionPos.x()) continue;

            for (int dy = -this.yRadius; dy <= this.yRadius; dy++) {
                int yr = xr + xzSqr * dy * dy + xySqr;
                if (yr > xyzSqr) continue;
                int y = dy + yOrigin;
                if (SectionPos.blockToSectionCoord(y) != sectionPos.y()) continue;
                if (level.isOutsideBuildHeight(y)) continue;

                for (int dz = -xzRadius; dz <= xzRadius; dz++) {
                    int zr = yr + xySqr * dz * dz;
                    if (zr > xyzSqr) continue;
                    int z = dz + zOrigin;
                    if (SectionPos.blockToSectionCoord(z) != sectionPos.z()) continue;

                    pos.set(x, y, z);

                    int layer = y - (yOrigin - this.yRadius);
                    // First try to spawn "between"
                    if (layer >= startBetween && (layer - startBetween + 1) <= between.layers) {
                        if (random.nextFloat() <= entry.density() / 2.0f) {
                            OreVeinUtil.placeOreBlock(between.target, access, section, layerTarget, random, entry, pos);
                            continue;
                        }
                    }

                    // Then try primary/secondary
                    if (layer >= startPrimary) {
                        if (random.nextFloat() <= entry.density()) {
                            OreVeinUtil.placeOreBlock(primary.target, access, section, layerTarget, random, entry, pos);
                            continue;
                        }
                    } else {
                        if (random.nextFloat() <= entry.density()) {
                            OreVeinUtil.placeOreBlock(secondary.target, access, section, layerTarget, random, entry, pos);
                            continue;
                        }
                    }

                    // Then lastly, try sporadic
                    if (random.nextFloat() <= entry.density() / sporadicDivisor) {
                        OreVeinUtil.placeOreBlock(sporadic.target, access, section, layerTarget, random, entry, pos);
                    }
                }
            }
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
                Codec.either(OreConfiguration.TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.codec())
                        .fieldOf("targets").forGetter(layer -> layer.target),
                ExtraCodecs.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("layers", -1)
                        .forGetter(layer -> layer.layers))
                .apply(instance, Layer::new));

        public final Either<List<OreConfiguration.TargetBlockState>, Material> target;
        public int layers;

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
