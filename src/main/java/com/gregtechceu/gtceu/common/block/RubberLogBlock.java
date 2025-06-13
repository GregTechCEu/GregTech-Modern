package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RubberLogBlock extends RotatedPillarBlock {

    public static final BooleanProperty NATURAL = GTBlockStateProperties.NATURAL;

    public RubberLogBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(NATURAL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NATURAL);
    }

    public boolean isNatural(BlockState state) {
        return state.getOptionalValue(NATURAL).orElse(false);
    }

    public BlockState changeNatural(BlockState state, boolean natural) {
        if (state.is(this)) {
            return state.setValue(NATURAL, natural);
        }
        return state;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction,
                                                     boolean simulate) {
        if (toolAction == ToolActions.AXE_STRIP) {
            return GTBlocks.STRIPPED_RUBBER_LOG.getDefaultState().setValue(RotatedPillarBlock.AXIS,
                    state.getValue(RotatedPillarBlock.AXIS));
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }
}
