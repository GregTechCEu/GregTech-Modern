package com.gregtechceu.gtceu.common.worldgen.modifier;

import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

public class RubberTreeChancePlacement extends RepeatingPlacement {

    public static final PlacementModifierType<RubberTreeChancePlacement> RUBBER_TREE_CHANCE_PLACEMENT = () -> RubberTreeChancePlacement.CODEC;

    public static final RubberTreeChancePlacement INSTANCE = new RubberTreeChancePlacement();
    public static final MapCodec<RubberTreeChancePlacement> CODEC = MapCodec.unit(INSTANCE);

    @Override
    protected int count(RandomSource random, @NotNull BlockPos pos) {
        return random.nextFloat() < ConfigHolder.INSTANCE.worldgen.rubberTreeSpawnChance ? 1 : 0;
    }

    @Override
    public PlacementModifierType<?> type() {
        return RUBBER_TREE_CHANCE_PLACEMENT;
    }
}
