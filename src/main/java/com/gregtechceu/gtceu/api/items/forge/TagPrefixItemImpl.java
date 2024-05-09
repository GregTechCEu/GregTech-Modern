package com.gregtechceu.gtceu.api.items.forge;

import com.gregtechceu.gtceu.api.materials.material.Material;
import com.gregtechceu.gtceu.api.tags.TagPrefix;
import com.gregtechceu.gtceu.api.items.TagPrefixItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/8/12
 * @implNote TagPrefixItemImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TagPrefixItemImpl extends TagPrefixItem {
    protected TagPrefixItemImpl(Properties properties, TagPrefix tagPrefix, Material material) {
        super(properties, tagPrefix, material);
    }

    public static TagPrefixItem create(Properties properties, TagPrefix tagPrefix, Material material) {
        return new TagPrefixItemImpl(properties, tagPrefix, material);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return getItemBurnTime();
    }

}
