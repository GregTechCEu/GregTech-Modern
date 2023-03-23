package com.gregtechceu.gtceu.api.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ActiveBlock extends Block {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ActiveBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    public BlockState changeActive(BlockState state, boolean active) {
        if (state.is(this)) {
            return state.setValue(ACTIVE, active);
        }
        return state;
    }

    public boolean isActive(BlockState state) {
        return state.getValue(ACTIVE);
    }
}
