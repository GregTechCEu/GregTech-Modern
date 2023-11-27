package com.gregtechceu.gtceu.api.data.worldgen.generator.indicators;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreIndicatorPlacer;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SurfaceIndicatorGenerator extends IndicatorGenerator {
    public static final Codec<SurfaceIndicatorGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(ext -> ext.block.get()),
            IntProvider.codec(1, 32).fieldOf("radius").forGetter(ext -> ext.radius),
            FloatProvider.codec(0.0f, 1.0f).fieldOf("density").forGetter(ext -> ext.density)
    ).apply(instance, SurfaceIndicatorGenerator::new));

    private NonNullSupplier<? extends Block> block = NonNullSupplier.of(() -> Blocks.AIR);
    private IntProvider radius = ConstantInt.of(5);
    private FloatProvider density = ConstantFloat.of(0.2f);

    public SurfaceIndicatorGenerator(GTOreDefinition entry) {
        super(entry);
    }

    public SurfaceIndicatorGenerator(Block block, IntProvider radius, FloatProvider density) {
        this.block = NonNullSupplier.of(() -> block);
        this.radius = radius;
        this.density = density;
    }

    public SurfaceIndicatorGenerator surfaceRockBlock(Material material) {
        var surfaceRockBlock = GTBlocks.SURFACE_ROCK_BLOCKS.get(material);

        if (surfaceRockBlock == null)
            throw new IllegalArgumentException("No surface rock registered for material " + material.getName());

        return block(surfaceRockBlock);
    }

    public SurfaceIndicatorGenerator block(Block block) {
        return block(NonNullSupplier.of(() -> block));
    }

    public SurfaceIndicatorGenerator block(NonNullSupplier<? extends Block> supplier) {
        this.block = supplier;
        return this;
    }

    public SurfaceIndicatorGenerator radius(int radius) {
        return radius(ConstantInt.of(radius));
    }

    public SurfaceIndicatorGenerator radius(IntProvider provider) {
        this.radius = provider;
        return this;
    }

    public SurfaceIndicatorGenerator density(float density) {
        return density(ConstantFloat.of(density));
    }

    private SurfaceIndicatorGenerator density(FloatProvider provider) {
        this.density = provider;
        return this;
    }

    @Override
    public Map<ChunkPos, OreIndicatorPlacer> generate(WorldGenLevel level, RandomSource random, GeneratedVeinMetadata metadata) {
        int radius = this.radius.sample(random);
        float density = this.density.sample(random);

        var centerAtY1 = metadata.center().atY(1);
        var centerX = metadata.center().getX();
        var centerZ = metadata.center().getZ();

        Stream<BlockPos> positionStream = BlockPos.betweenClosedStream(
                centerX - radius, 1, centerZ - radius,
                centerX + radius, 1, centerZ + radius
        ).map(BlockPos::immutable);

        var positions = positionStream
                .filter(pos -> pos.equals(metadata.center()) || random.nextFloat() <= density)
                .filter(pos -> Math.sqrt(pos.distSqr(centerAtY1)) <= radius)
                .toList();

        return WorldGeneratorUtils.groupByChunks(positions).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> createPlacer(level, entry.getValue())));
    }

    private OreIndicatorPlacer createPlacer(WorldGenLevel level, List<BlockPos> positionsWithoutY) {
        return (access) -> {
            var positions = positionsWithoutY.stream()
                    .map(pos -> pos.atY(level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ())))
                    .filter(pos -> !level.isOutsideBuildHeight(pos))
                    .toList();

            for (BlockPos pos : positions) {
                // This is necessary because the heightmap can't be determined at the time of creating the placers
                var section = Objects.requireNonNull(access.getSection(pos));

                int sectionX = SectionPos.sectionRelative(pos.getX());
                int sectionY = SectionPos.sectionRelative(pos.getY());
                int sectionZ = SectionPos.sectionRelative(pos.getZ());

                if (!section.getBlockState(sectionX, sectionY, sectionZ).isAir())
                    return;

                section.setBlockState(sectionX, sectionY, sectionZ, block.get().defaultBlockState(), false);
            }
        };
    }

    @Override
    public int getSearchRadiusModifier(int veinRadius) {
        return Math.max(0, radius.getMaxValue() - veinRadius);
    }

    @Override
    public Codec<? extends IndicatorGenerator> codec() {
        return CODEC;
    }
}
