package com.gregtechceu.gtceu.api.item.fabric;

import com.gregtechceu.gtceu.client.renderer.item.GTBucketItemRenderer;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
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
    public GTBucketItem(Supplier<? extends Fluid> fluid, Properties properties) {
        super(fluid.get(), properties);
        this.fluid = fluid.get();
        renderer = FluidHelper.isLighterThanAir(FluidStack.create(this.fluid, FluidHelper.getBucket())) ? GTBucketItemRenderer.INSTANCE_GAS : GTBucketItemRenderer.INSTANCE;
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

}
