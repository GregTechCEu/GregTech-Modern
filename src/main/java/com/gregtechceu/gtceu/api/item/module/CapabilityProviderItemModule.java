package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.Nullable;

/**
 * An item module which adds capability data to the item its attached to.<br>
 * Due to limitations with capability handling, having multiple item modules providing the same capability will not work.<br>
 * Capability data is stored on the item which the module is inserted into, not the module item itsself.
 */
public abstract class CapabilityProviderItemModule<T> extends ItemModule {

    public CapabilityProviderItemModule(ResourceLocation id) {
        super(id);
    }

    public abstract ItemCapability<T, @Nullable Void> getCapability();

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

    /**
     * Called when the module is attached, should apply existing capability data from the module item to the new capability.<br>
     * E.g. if a battery is attached, the battery's current charge should be copied to the battery item module capability.
     */
    public abstract void applyInitialCapabilityData(T newCap, ModuleContext context);

    /**
     * Returns the capability, or {@code null} if not available.
     */
    public abstract @Nullable T createCapabilityForStack(ModuleContext context, ItemStack stack);

    /**
     * Called when the module is removed, should clear capability data from the item stack (e.g. remove the {@link GTDataComponents#ENERGY_CONTENT} component from the item stack).<br>
     */
    public abstract void clearCapabilityFromStack(ModuleContext context, ItemStack stack);

    @SuppressWarnings("unchecked")
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(getCapability(), (s, v) -> {
            var modular = GTCapabilityHelper.getModularItem(s);
            if (modular == null) return null;

            ModuleContext context = null;
            CapabilityProviderItemModule<T> capProvider = null;
            for (ModuleContext module : modular.getAllModuleInstances()) {
                if (module.getModule() instanceof CapabilityProviderItemModule<?> providerItemModule &&
                        providerItemModule.getCapability().equals(getCapability())) {
                    capProvider = (CapabilityProviderItemModule<T>) providerItemModule;
                    context = module;
                    break;
                }
            }
            if (capProvider == null) return null;
            return capProvider.createCapabilityForStack(context, s);
        }, item);
    }
}
