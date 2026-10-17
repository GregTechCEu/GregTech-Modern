package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An item module which adds capability data to the item its attached to.<br>
 * Due to limitations with capability handling, having multiple item modules providing the same capability will not
 * work.<br>
 * Capability data is stored on the item which the module is inserted into, not the module item itsself.
 */
public abstract class CapabilityProviderItemModule<T> extends ItemModule {

    public CapabilityProviderItemModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public void onAttach(ModuleContext moduleContext) {
        super.onAttach(moduleContext);
        T cap = createCapabilityForStack(moduleContext, moduleContext.getAppliedTo());
        if (cap != null) applyInitialCapabilityData(cap, moduleContext);
    }

    @Override
    public void onRemove(ModuleContext moduleContext) {
        super.onRemove(moduleContext);
        clearCapabilityFromStack(moduleContext, moduleContext.getAppliedTo());
    }

    public abstract Capability<T> getCapability();

    /**
     * Called when the module is attached, should apply existing capability data from the module item to the new
     * capability.<br>
     * E.g. if a battery is attached, the battery's current charge should be copied to the battery item module
     * capability.
     */
    public abstract void applyInitialCapabilityData(T newCap, ModuleContext context);

    /**
     * Returns the capability, or {@code null} if not available.
     */
    public abstract @Nullable T createCapabilityForStack(ModuleContext context, ItemStack stack);

    /**
     * Called when the module is removed, should clear capability data from the item stack (e.g. remove the energy
     * content NBT from the item stack).
     */
    public abstract void clearCapabilityFromStack(ModuleContext context, ItemStack stack);

    @SuppressWarnings("unchecked")
    public <Q> LazyOptional<Q> getCapability(ModuleContext context, @NotNull Capability<Q> cap) {
        if (getCapability().equals(cap)) {
            var capProvider = (CapabilityProviderItemModule<Q>) this;
            return LazyOptional.of(() -> capProvider.createCapabilityForStack(context, context.getAppliedTo()));
        }
        return LazyOptional.empty();
    }
}
