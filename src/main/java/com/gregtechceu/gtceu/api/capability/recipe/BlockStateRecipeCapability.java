package com.gregtechceu.gtceu.api.capability.recipe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateRecipeCapability extends RecipeCapability<BlockState> {

    public final static BlockStateRecipeCapability CAP = new BlockStateRecipeCapability();

    protected BlockStateRecipeCapability() {
        super("block_state", 0xFFABABAB, false, 5, BlockState.CODEC);
    }

    @Override
    public BlockState fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return null;
    }

    @Override
    public void toNetwork(BlockState ingredient, FriendlyByteBuf friendlyByteBuf) {

    }

    @Override
    public BlockState copyInner(BlockState content) {
        return content;
    }

    @Override
    public BlockState copyInner(BlockState content, int multiplier) {
        return null;
    }
}
