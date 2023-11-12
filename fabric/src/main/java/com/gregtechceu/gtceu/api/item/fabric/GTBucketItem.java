package com.gregtechceu.gtceu.api.item.fabric;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/3/28
 * @implNote GTBucketItem
 */
public class GTBucketItem extends BucketItem {
    Fluid fluid;
    final Material material;
    final String langKey;

    public GTBucketItem(Supplier<? extends Fluid> fluid, Properties properties, Material material, String langKey) {
        super(fluid.get(), properties);
        this.fluid = fluid.get();
        this.material = material;
        this.langKey = langKey;
    }

    public void onRegister() {
        var property = material.getProperty(PropertyKey.FLUID);
        if (property != null) {
            var fluid = material.getFluid();
            if (fluid instanceof GTFluid gtFluid && gtFluid.getBurnTime() > 0) {
                FuelRegistry.INSTANCE.add(this, gtFluid.getBurnTime());
            }
        }
    }

    public static int color(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof GTBucketItem item) {
            if (index == 1) {
                return item.material.getMaterialRGB();
            }
        }
        return -1;
    }

    @Override
    public String getDescriptionId() {
        return "item.gtceu.bucket";
    }

    @Override
    public Component getDescription() {
        Component materialName = material.getLocalizedName();
        return Component.translatable("item.gtceu.bucket", Component.translatable(this.langKey, materialName));
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

}
