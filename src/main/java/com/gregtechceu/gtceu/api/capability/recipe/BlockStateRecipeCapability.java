package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.SerializerBlockState;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BlockStateRecipeCapability extends RecipeCapability<BlockState> {

    public final static BlockStateRecipeCapability CAP = new BlockStateRecipeCapability();

    protected BlockStateRecipeCapability() {
        super("block_state", 0xFFABABAB, false, 5, SerializerBlockState.INSTANCE);
    }

    @Override
    public BlockState copyInner(@NotNull BlockState content) {
        return content;
    }
}
