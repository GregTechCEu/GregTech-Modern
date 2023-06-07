package com.gregtechceu.gtceu.common.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RubberLogBlock extends RotatedPillarBlock {

    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

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
}
