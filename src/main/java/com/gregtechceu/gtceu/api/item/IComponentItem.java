package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.List;

public interface IComponentItem extends ItemLike {

    List<IItemComponent> getComponents();

    void attachComponents(IItemComponent... components);

    default void attachCapabilities(RegisterCapabilitiesEvent event) {
        for (IItemComponent component : getComponents()) {
            if (component instanceof IComponentCapability componentCapability) {
                componentCapability.attachCapabilites(event, this.asItem());
            }
        }
    }

    default void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {

    }
}
