package com.gregtechceu.gtceu.api.item.forge;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public interface IGTToolImpl extends IGTTool {
    public static void init() {
    }

    @Nullable
    default ICapabilityProvider definition$initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        List<ICapabilityProvider> providers = new ArrayList<>();
        if (isElectric()) {
            providers.add(ElectricStats.createElectricItem(0L, getElectricTier()).createProvider(stack));
        }
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            ICapabilityProvider behaviorProvider = behavior.createProvider(stack, nbt);
            if (behaviorProvider != null) {
                providers.add(behaviorProvider);
            }
        }
        if (providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        return new CombinedCapabilityProvider(providers);
    }
}
