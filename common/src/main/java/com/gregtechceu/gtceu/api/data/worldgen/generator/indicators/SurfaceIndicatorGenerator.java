package com.gregtechceu.gtceu.api.data.worldgen.generator.indicators;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Function;
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
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GeneratedVeinMetadata veinPosition) {
        int radius = this.radius.sample(random);
        float density = this.density.sample(random);

        var centerAtY1 = veinPosition.center().atY(1);
        var centerX = veinPosition.center().getX();
        var centerZ = veinPosition.center().getZ();

        Stream<BlockPos> positions = BlockPos.betweenClosedStream(
                centerX - radius, 1, centerZ - radius,
                centerX + radius, 1, centerZ + radius
        ).map(BlockPos::immutable);

        return positions
                .filter(pos -> pos.equals(veinPosition.center()) || random.nextFloat() <= density)
                .filter(pos -> Math.sqrt(pos.distSqr(centerAtY1)) <= radius)
                .collect(Collectors.toMap(
                        Function.identity(),
                        // Note that while the returned block pos for each placer here is at Y=1, the placer itself will
                        // use the surface Y position on generation. This needs to be done because we can't reliably
                        // retrieve the surface heightmap for ungenerated chunks at this point.
                        pos -> createPlacer(level, pos, veinPosition)
                ));
    }

    private OreBlockPlacer createPlacer(WorldGenLevel level, BlockPos posAtY1, GeneratedVeinMetadata veinPosition) {
        return (access, __) -> {
            int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, posAtY1.getX(), posAtY1.getZ());
            var pos = posAtY1.atY(surfaceY);

            if (level.isOutsideBuildHeight(pos))
                return;

            // This is necessary because the heightmap can't be determined at the time of creating the placers
            var section = access.getSection(pos);

            int sectionX = SectionPos.sectionRelative(pos.getX());
            int sectionY = SectionPos.sectionRelative(pos.getY());
            int sectionZ = SectionPos.sectionRelative(pos.getZ());

            if (!section.getBlockState(sectionX, sectionY, sectionZ).isAir())
                return;

            section.setBlockState(sectionX, sectionY, sectionZ, block.get().defaultBlockState(), false);
        };
    }

    @Override
    public Codec<? extends IndicatorGenerator> codec() {
        return CODEC;
    }
}
