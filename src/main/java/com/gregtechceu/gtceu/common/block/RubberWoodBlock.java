package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;

import org.jetbrains.annotations.Nullable;

public class RubberWoodBlock extends RotatedPillarBlock {

    public RubberWoodBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility toolAction,
                                                     boolean simulate) {
        if (toolAction == ItemAbilities.AXE_STRIP) {
            return GTBlocks.STRIPPED_RUBBER_WOOD.getDefaultState().setValue(RotatedPillarBlock.AXIS,
                    state.getValue(RotatedPillarBlock.AXIS));
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }
}
