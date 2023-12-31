package com.gregtechceu.gtceu.api.item.forge;

import com.gregtechceu.gtceu.api.capability.forge.CombinedCapabilityProvider;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public interface IGTToolImpl extends IGTTool {
    static void init() {
    }

    @Nullable
    default ICapabilityProvider definition$initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        List<ICapabilityProvider> providers = new ArrayList<>();
        if (isElectric()) {
            ElectricStats item = ElectricStats.createElectricItem(0L, getElectricTier());
            providers.add(new ICapabilityProvider() {
                @Override
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction arg) {
                    return item instanceof IComponentCapability componentCapability ? componentCapability.getCapability(stack, capability) : null;
                }
            });
        }
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior instanceof IComponentCapability componentCapability) {
                providers.add(new ICapabilityProvider() {
                    @Override
                    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction arg) {
                        return componentCapability.getCapability(stack, capability);
                    }
                });
            }
        }
        if (providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        return new CombinedCapabilityProvider(providers);
    }
}
