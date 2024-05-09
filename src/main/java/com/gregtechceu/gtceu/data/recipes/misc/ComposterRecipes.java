package com.gregtechceu.gtceu.data.recipes.misc;

import com.gregtechceu.gtceu.data.blocks.GTBlocks;
import net.minecraft.world.level.ItemLike;

import java.util.function.BiConsumer;

public class ComposterRecipes {

    // Add composter things here.
    public static void addComposterRecipes(BiConsumer<ItemLike, Float> provider) {
        provider.accept(GTBlocks.RUBBER_LEAVES, 0.3F);
        provider.accept(GTBlocks.RUBBER_SAPLING, 0.3F);
    }
}
