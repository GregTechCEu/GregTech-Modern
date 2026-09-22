package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An item module which adds capability data to the item its attached to.
 */
public abstract class CapabilityProviderItemModule<T> extends ItemModule {

    public CapabilityProviderItemModule(ResourceLocation id) {
        super(id);
    }

    public abstract Capability<T> getCapability();

    public abstract @Nullable T createCapabilityForStack(ModuleContext context, ItemStack stack);

    public abstract void clearCapabilityFromStack(ModuleContext context, ItemStack stack);

    @SuppressWarnings("unchecked")
    public <Q> LazyOptional<Q> getCapability(ModuleContext context, @NotNull Capability<Q> cap) {
        if (getCapability().equals(cap)) {
            var capProvider = (CapabilityProviderItemModule<Q>)this;
            return LazyOptional.of(() -> capProvider.createCapabilityForStack(context, context.getAppliedTo()));
        }
        return LazyOptional.empty();
    }

    @Override
    public void onRemove(ModuleContext moduleContext) {
        super.onRemove(moduleContext);
        clearCapabilityFromStack(moduleContext, moduleContext.getAppliedTo());
    }
}
