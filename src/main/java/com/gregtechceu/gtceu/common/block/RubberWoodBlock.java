package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import org.jetbrains.annotations.Nullable;

public class RubberWoodBlock extends RotatedPillarBlock {

    public RubberWoodBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction,
                                                     boolean simulate) {
        if (toolAction == ToolActions.AXE_STRIP) {
            return GTBlocks.STRIPPED_RUBBER_WOOD.getDefaultState().setValue(RotatedPillarBlock.AXIS,
                    state.getValue(RotatedPillarBlock.AXIS));
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }
}
