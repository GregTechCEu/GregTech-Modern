package com.gregtechceu.gtceu.forge.core.mixins;

import com.gregtechceu.gtceu.api.block.AppearanceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author KilaBash
 * @date 2023/3/27
 * @implNote AppearanceBlockMixin
 */
@Mixin(AppearanceBlock.class)
public abstract class AppearanceBlockMixin extends Block {
    public AppearanceBlockMixin(Properties arg) {
        super(arg);
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        var appearance = AppearanceBlock.class.cast(this).getBlockAppearance(state, level, pos, side, queryState, queryPos);
        return appearance == null ? state : appearance;
    }

}
