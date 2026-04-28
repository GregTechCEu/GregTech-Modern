package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public record FluidFilterBehaviour(Function<ItemStack, FluidFilter> filterCreator) implements IItemUIFactory {

    @Override
    public void onAttached(Item item) {
        IItemUIFactory.super.onAttached(item);
        FluidFilter.FILTERS.put(item, filterCreator);
    }

    @Override
    public Object createUI(GTHeldItemUIHolder holder, Player entityPlayer) {
        return FluidFilterBehaviourUI.create(holder, entityPlayer);
    }
}
