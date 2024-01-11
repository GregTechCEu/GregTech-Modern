package com.gregtechceu.gtceu.api.data.worldgen.modifier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOrePlacementModifier
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FrequencyModifier extends PlacementModifier {
    public static final PlacementModifierType<FrequencyModifier> FREQUENCY_MODIFIER = GTRegistries.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, GTCEu.id("frequency"), () -> FrequencyModifier.CODEC);

    public static final Codec<FrequencyModifier> CODEC = ExtraCodecs.POSITIVE_FLOAT.fieldOf("chance").xmap(FrequencyModifier::new, (modifier) -> modifier.frequency).codec();

    private final float frequency;

    public FrequencyModifier(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        int count = getCount(frequency, random);
        if (count == 0) {
            return Stream.empty();
        }
        return Stream.of(pos);
    }

    public int getCount(float frequency, RandomSource random) {
        int floored = Mth.floor(frequency);
        return floored + (random.nextFloat() < (frequency - floored) ? 1 : 0);
    }

    @Override
    public PlacementModifierType<?> type() {
        return FREQUENCY_MODIFIER;
    }

}
