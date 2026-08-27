package com.gregtechceu.gtceu.common.condition;

import com.gregtechceu.gtceu.GTCEu;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

public class IsDevEnvironmentCondition implements ICondition {
    public static final MapCodec<IsDevEnvironmentCondition> CODEC = MapCodec.unit(new IsDevEnvironmentCondition());

    @Override
    public boolean test(IContext iContext) {
        return GTCEu.isDev();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
