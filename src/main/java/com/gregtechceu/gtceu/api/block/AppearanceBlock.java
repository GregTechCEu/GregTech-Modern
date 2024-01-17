package com.gregtechceu.gtceu.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/27
 * @implNote AppearanceBlock
 */
public class AppearanceBlock extends Block implements IAppearance {
    public AppearanceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        var appearance = AppearanceBlock.class.cast(this).getBlockAppearance(state, level, pos, side, queryState, queryPos);
        return appearance == null ? state : appearance;
    }
}
