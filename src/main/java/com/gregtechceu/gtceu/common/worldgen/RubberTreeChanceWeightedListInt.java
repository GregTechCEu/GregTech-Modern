package com.gregtechceu.gtceu.common.worldgen;

import com.gregtechceu.gtceu.config.ConfigHolder;

import com.gregtechceu.gtceu.data.worldgen.GTIntProviderTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class RubberTreeChanceWeightedListInt extends IntProvider {

    public static final RubberTreeChanceWeightedListInt INSTANCE = new RubberTreeChanceWeightedListInt();
    public static final MapCodec<RubberTreeChanceWeightedListInt> CODEC = MapCodec.unit(INSTANCE);

    private final SimpleWeightedRandomList<IntProvider> distribution;
    private final int maxValue;

    public RubberTreeChanceWeightedListInt() {
        if (ConfigHolder.INSTANCE.worldgen.rubberTreeSpawnChance <= 0.0f) {
            distribution = SimpleWeightedRandomList.<IntProvider>builder()
                    .add(ConstantInt.of(0), 1)
                    .build();
            this.maxValue = 0;
        } else {
            float chance = 1.0F / ConfigHolder.INSTANCE.worldgen.rubberTreeSpawnChance;
            if (Math.abs(chance - (int) chance) > 1.0E-5F) {
                throw new IllegalStateException("Chance data cannot be represented as list weight");
            } else {
                this.distribution = SimpleWeightedRandomList.<IntProvider>builder()
                        .add(ConstantInt.of(0), (int) chance - 1)
                        .add(ConstantInt.of(1), 1)
                        .build();
            }
            this.maxValue = 1;
        }
    }

    @Override
    public int sample(RandomSource random) {
        return this.distribution.getRandomValue(random).orElseThrow(IllegalStateException::new).sample(random);
    }

    @Override
    public int getMinValue() {
        return 0;
    }

    @Override
    public int getMaxValue() {
        return this.maxValue;
    }

    @Override
    public IntProviderType<?> getType() {
        return GTIntProviderTypes.RUBBER_TREE_CHANCE.get();
    }
}
