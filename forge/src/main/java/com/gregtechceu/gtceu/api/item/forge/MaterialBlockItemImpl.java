package com.gregtechceu.gtceu.api.item.forge;

import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/8/12
 * @implNote MaterialBlockItemImpl
 */
public class MaterialBlockItemImpl extends MaterialBlockItem {
    protected MaterialBlockItemImpl(MaterialBlock block, Properties properties) {
        super(block, properties);
    }

    public static MaterialBlockItem create(MaterialBlock block, Item.Properties properties) {
        return new MaterialBlockItemImpl(block, properties);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return getItemBurnTime();
    }
}
