package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.Nullable;

/**
 * An item module which adds capability data to the item its attached to.
 */
public abstract class CapabilityProviderItemModule<T> extends ItemModule {

    public CapabilityProviderItemModule(ResourceLocation id) {
        super(id);
    }

    public abstract ItemCapability<T, @Nullable Void> getCapability();

    public abstract @Nullable T createCapabilityForStack(ModuleContext context, ItemStack stack);

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

    @Override
    public void onRemove(ModuleContext moduleContext) {
        super.onRemove(moduleContext);
        clearCapabilityFromStack(moduleContext, moduleContext.getAppliedTo());
    }
}
