package com.gregtechceu.gtceu.api.item.fabric;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.client.renderer.item.GTBucketItemRenderer;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
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
public class GTBucketItem extends BucketItem implements IItemRendererProvider {
    Fluid fluid;
    IRenderer renderer;
    final Material material;
    public GTBucketItem(Supplier<? extends Fluid> fluid, Properties properties, Material material) {
        super(fluid.get(), properties);
        this.fluid = fluid.get();
        renderer = FluidHelper.isLighterThanAir(FluidStack.create(this.fluid, FluidHelper.getBucket())) ? GTBucketItemRenderer.INSTANCE_GAS : GTBucketItemRenderer.INSTANCE;
        this.material = material;
    }

    public void onRegister() {
        var fluid = material.getProperty(PropertyKey.FLUID);
        if (fluid != null && fluid.getBurnTime() > 0) {
            FuelRegistry.INSTANCE.add(this, fluid.getBurnTime());
        }
    }

    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return renderer;
    }

    public static int color(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof GTBucketItem item) {
            if (index == 1) {
                return FluidHelper.getColor(FluidStack.create(item.fluid, FluidHelper.getBucket()));
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
        return Component.translatable("item.gtceu.bucket", material.getLocalizedName());
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

}
