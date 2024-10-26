package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.block.LampBlock;

import com.lowdragmc.lowdraglib.client.model.custommodel.ICTMPredicate;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LampRenderer extends IModelRenderer implements ICTMPredicate {

    public LampRenderer(LampBlock lamp, BlockState state) {
        super(GTCEu.id("block/%s%s_lamp%s%s".formatted(
                lamp.color,
                lamp.bordered ? "" : "_borderless",
                state.getValue(LampBlock.LIGHT) ? "" : "_off",
                state.getValue(LampBlock.LIGHT) && state.getValue(LampBlock.BLOOM) ? "_bloom" : "")));
    }

    @Override
    public boolean isConnected(BlockAndTintGetter level, BlockState state, BlockPos pos,
                               BlockState sourceState, BlockPos sourcePos, Direction side) {
        var stateAppearance = state.getAppearance(level, pos, side, sourceState, sourcePos);
        var sourceStateAppearance = sourceState.getAppearance(level, sourcePos, side, state, pos);
        return stateAppearance.getBlock() == sourceStateAppearance.getBlock();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean useAO() {
        return true;
    }

    @Override
    public boolean reBakeCustomQuads() {
        return true;
    }

    @Override
    public float reBakeCustomQuadsOffset() {
        return 0.0f;
    }
}
