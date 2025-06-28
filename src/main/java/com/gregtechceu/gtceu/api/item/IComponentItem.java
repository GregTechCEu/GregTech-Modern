package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IComponentItem extends ItemLike, IForgeItem {

    List<IItemComponent> getComponents();

    void attachComponents(IItemComponent... components);

    @Override
    default @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                for (IItemComponent component : getComponents()) {
                    if (component instanceof IComponentCapability componentCapability) {
                        var value = componentCapability.getCapability(stack, cap);
                        if (value.isPresent()) {
                            return value;
                        }
                    }
                }
                return LazyOptional.empty();
            }
        };
    }

    default void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {}
}
