package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.mui.base.IItemUIHolder;
import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryGuiData;
import com.gregtechceu.gtceu.api.mui.factory.UIFactories;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public record ItemFilterBehaviour(Function<ItemStack, ItemFilter> filterCreator)
        implements IInteractionItem, IItemUIHolder {

    @Override
    public void onAttached(Item item) {
        ItemFilter.FILTERS.put(item, filterCreator);
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
    public ModularPanel buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return ItemFilter.loadFilter(data.getUsedItemStack()).getPanel(data, syncManager, settings);
    }

    // @Override
    // public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
    // var held = holder.getHeld();
    // return new ModularUI(176, 157, holder, entityPlayer)
    // .background(GuiTextures.BACKGROUND)
    // .widget(new LabelWidget(5, 5, held.getDescriptionId()))
    // .widget(ItemFilter.loadFilter(held).openConfigurator((176 - 80) / 2, (60 - 55) / 2 + 15))
    // .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 75, true));
    // }
}
