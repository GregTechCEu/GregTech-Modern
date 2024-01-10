package com.gregtechceu.gtceu.api.item.forge;

import com.gregtechceu.gtceu.api.capability.forge.CombinedCapabilityProvider;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public interface IGTToolImpl extends IGTTool {
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

    static boolean definition$isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (stack.getItem() instanceof IGTTool gtTool) {
            if (TierSortingRegistry.isTierSorted(gtTool.getTier())) {
                return TierSortingRegistry.isCorrectTierForDrops(gtTool.getTier(), state) && gtTool.getToolClasses(stack).stream().anyMatch(type -> type.harvestTags.stream().anyMatch(state::is));
            } else {
                int i = gtTool.getTier().getLevel();
                if (i < 3 && state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
                    return false;
                } else if (i < 2 && state.is(BlockTags.NEEDS_IRON_TOOL)) {
                    return false;
                } else {
                    return i < 1 && state.is(BlockTags.NEEDS_STONE_TOOL) ? false : gtTool.getToolClasses(stack).stream().anyMatch(type -> type.harvestTags.stream().anyMatch(state::is));
                }
            }
        }
        return stack.getItem().isCorrectToolForDrops(state);
    }
}
