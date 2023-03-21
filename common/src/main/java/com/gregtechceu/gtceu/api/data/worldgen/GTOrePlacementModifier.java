package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.config.AllConfigs;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOrePlacementModifier
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTOrePlacementModifier extends PlacementModifier {
    public static PlacementModifierType<GTOrePlacementModifier> ORE_PLACEMENT = GTRegistries.register(Registry.PLACEMENT_MODIFIERS, GTCEu.id("ore_placement"), () -> GTOrePlacementModifier.CODEC);

    public static final Codec<GTOrePlacementModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GTOreFeatureEntry.CODEC
                    .fieldOf("entry")
                    .forGetter(GTOrePlacementModifier::getEntry)
    ).apply(instance, GTOrePlacementModifier::new));

    private final GTOreFeatureEntry entry;

    public GTOrePlacementModifier(GTOreFeatureEntry entry) {
        this.entry = entry;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        int count = getCount(getFrequency(), random);
        if (count == 0) {
            return Stream.empty();
        }

        int minY = getMinY();
        int maxY = getMaxY();

        return IntStream.range(0, count)
                .mapToObj(i -> pos)
                .map(p -> {
                    int x = random.nextInt(16) + p.getX();
                    int z = random.nextInt(16) + p.getZ();
                    int y = Mth.randomBetweenInclusive(random, minY, maxY) + 120;
                    return new BlockPos(x, y, z);
                });
    }

    public int getCount(float frequency, RandomSource random) {
        int floored = Mth.floor(frequency);
        return floored + (random.nextFloat() < (frequency - floored) ? 1 : 0);
    }

    @Override
    public PlacementModifierType<?> type() {
        return ORE_PLACEMENT;
    }

    public GTOreFeatureEntry getEntry() {
        return entry;
    }

    public float getFrequency() {
        return entry.frequency;
    }

    public int getMinY() {
        return entry.minHeight;
    }

    public int getMaxY() {
        return entry.maxHeight;
    }
}
