package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import net.minecraft.world.level.ItemLike;

import java.util.function.BiConsumer;

public class ComposterRecipes {

    // Add composter things here.
    public static void addComposterRecipes(BiConsumer<ItemLike, Float> provider) {
        provider.accept(GTBlocks.RUBBER_LEAVES.get(), 0.3F);
        provider.accept(GTBlocks.RUBBER_SAPLING.get(), 0.3F);
    }
}
