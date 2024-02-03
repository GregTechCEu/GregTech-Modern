package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.SerializerBlockState;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapBlockStateIngredient;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BlockStateRecipeCapability extends RecipeCapability<BlockState> {

    public final static BlockStateRecipeCapability CAP = new BlockStateRecipeCapability();

    protected BlockStateRecipeCapability() {
        super("block_state", 0xFFABABAB, SerializerBlockState.INSTANCE);
    }

    @Override
    public BlockState copyInner(BlockState content) {
        return content;
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object ingredient) {
        return List.of(new MapBlockStateIngredient((BlockState) ingredient));
    }
}