package com.gregtechceu.gtceu.common.worldgen.modifier;

import com.gregtechceu.gtceu.common.data.worldgen.GTPlacementModifiers;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

import com.mojang.serialization.Codec;

public class RubberTreeChancePlacement extends RepeatingPlacement {

    public static final RubberTreeChancePlacement INSTANCE = new RubberTreeChancePlacement();
    public static final Codec<RubberTreeChancePlacement> CODEC = Codec.unit(INSTANCE);

    @Override
    protected int count(RandomSource random, BlockPos pos) {
        return random.nextFloat() < ConfigHolder.INSTANCE.worldgen.rubberTreeSpawnChance ? 1 : 0;
    }

    @Override
    public PlacementModifierType<?> type() {
        return GTPlacementModifiers.RUBBER_TREE_CHANCE.get();
    }
}
