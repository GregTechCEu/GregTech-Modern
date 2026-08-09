package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.cover.filter.Filter;
import com.gregtechceu.gtceu.api.cover.filter.Filters;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.UIFactories;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

import java.util.function.Function;

public record FilterBehaviour<T>(Class<T> filterableObjectType, Function<ItemStack, Filter<T>> filterCreator)
        implements IItemUIHolder {

    @Override
    public void onAttached(Item item) {
        Filters.registerFilter(filterableObjectType, item, filterCreator);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide) {
            if (player.isCrouching()) {
                UIFactories.playerInventory().openFromHand(player, usedHand);
                return InteractionResultHolder.success(player.getItemInHand(usedHand));
            }
        }
        return InteractionResultHolder.fail(player.getItemInHand(usedHand));
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return Filters.loadFilter(filterableObjectType, data.getUsedItemStack()).getPanel(data, syncManager, settings,
                true);
    }
}
