package com.gregtechceu.gtceu.api.data.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.List;

public class MultiTargetBlockState {
    public static final Codec<MultiTargetBlockState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuleTest.CODEC.fieldOf("target").forGetter(target -> target.target),
            BlockState.CODEC.listOf().fieldOf("state").forGetter(target -> target.state)
    ).apply(instance, MultiTargetBlockState::new));

    public final RuleTest target;
    public final List<BlockState> state;

    MultiTargetBlockState(RuleTest target, List<BlockState> state) {
        this.target = target;
        this.state = state;
    }
}
