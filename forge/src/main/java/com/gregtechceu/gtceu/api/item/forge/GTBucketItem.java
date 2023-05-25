package com.gregtechceu.gtceu.api.item.forge;

import com.gregtechceu.gtceu.client.renderer.item.GTBucketItemRenderer;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/3/28
 * @implNote GTBucketItem
 */
public class GTBucketItem extends BucketItem implements IItemRendererProvider {

    IRenderer renderer;

    public GTBucketItem(Supplier<? extends Fluid> fluid, Properties properties, boolean isGas) {
        super(fluid, properties);
        this.renderer = isGas ? GTBucketItemRenderer.INSTANCE_GAS : GTBucketItemRenderer.INSTANCE;
    }

    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return renderer;
    }

    public static int color(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof GTBucketItem item) {
            if (index == 1) {
                return FluidHelper.getColor(FluidStack.create(item.getFluid(), FluidHelper.getBucket()));
            }
        }
        return -1;
    }

    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag nbt) {
        return this.getClass() == GTBucketItem.class ? new FluidBucketWrapper(stack) : super.initCapabilities(stack, nbt);
    }

}
