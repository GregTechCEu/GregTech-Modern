package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

public class GTBucketItem extends BucketItem {

    final Material material;
    final String langKey;

    public GTBucketItem(Fluid fluid, Properties properties, Material material, String langKey) {
        super(fluid, properties.overrideDescription("item.gtceu.bucket"));
        this.material = material;
        this.langKey = langKey;
    }

    public static int color(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof GTBucketItem item) {
            if (index == 1) {
                return RenderUtil.getFluidTint(item.content);
            }
        }
        return -1;
    }

    public Component getDescription() {
        Component materialName = material.getLocalizedName();
        return Component.translatable("item.gtceu.bucket", Component.translatable(this.langKey, materialName));
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
        if (this.content instanceof GTFluid gtFluid) {
            return gtFluid.getBurnTime();
        }
        return 0;
    }
}
