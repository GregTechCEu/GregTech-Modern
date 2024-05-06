package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public interface IComponentItem extends ItemLike {

    void attachComponents(IItemComponent... components);

    default <T> LazyOptional<T> getCapability(@NotNull final ItemStack itemStack, @NotNull final Capability<T> cap) {
        return LazyOptional.empty();
    }
}
