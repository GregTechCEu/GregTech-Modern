package com.gregtechceu.gtceu.common.item.tool.behavior.forge;

import com.gregtechceu.gtceu.api.misc.forge.VoidFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.item.tool.behavior.PlungerBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class PlungerBehaviorImpl extends PlungerBehavior implements IComponentCapability {
    public static PlungerBehavior create() {
        return new PlungerBehaviorImpl();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, LazyOptional.of(() -> new VoidFluidHandlerItemStack(itemStack) {
                @Override
                public int fill(FluidStack resource, FluidAction doFill) {
                    int result = super.fill(resource, doFill);
                    if (result > 0) {
                        ToolHelper.damageItem(getContainer(), null);
                    }
                    return result;
                }
            }));
        }
        return LazyOptional.empty();
    }
}
