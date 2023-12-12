package com.gregtechceu.gtceu.api.item.gui;

import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

public class PlayerInventoryHolder implements IUIHolder {

    public static void openHandItemUI(Player player, InteractionHand hand) {
        PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
        holder.openUI();
    }

    public final Player player;

    final InteractionHand hand;

    ItemStack sampleItem;
    BooleanSupplier validityCheck;

    @Environment(EnvType.CLIENT)
    public PlayerInventoryHolder(Player player, InteractionHand hand, ItemStack sampleItem) {
        this.player = player;
        this.hand = hand;
        this.sampleItem = sampleItem;
        this.validityCheck = () -> ItemStack.isSameItem(sampleItem, player.getItemInHand(hand));
    }

    public PlayerInventoryHolder(Player Player, InteractionHand hand) {
        this.player = Player;
        this.hand = hand;
        this.sampleItem = player.getItemInHand(hand);
        this.validityCheck = () -> ItemStack.isSameItem(sampleItem, player.getItemInHand(hand));
    }

    public PlayerInventoryHolder setCustomValidityCheck(BooleanSupplier validityCheck) {
        this.validityCheck = validityCheck;
        return this;
    }

    public ModularUI createUI(Player player) {
        IItemUIFactory uiFactory = (IItemUIFactory) sampleItem.getItem();
        return uiFactory.createUI(new HeldItemUIFactory.HeldItemHolder(player, hand), player);
    }

    public void openUI() {
        PlayerInventoryUIFactory.INSTANCE.openUI(this, (ServerPlayer) player);
    }

    @Override
    public boolean isInvalid() {
        return !validityCheck.getAsBoolean();
    }

    @Override
    public boolean isRemote() {
        return player.level().isClientSide;
    }

    public ItemStack getCurrentItem() {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!ItemStack.isSameItem(sampleItem, itemStack))
            return null;
        return itemStack;
    }

    /**
     * Will replace current item in hand with the given one
     * will also update sample item to this item
     */
    public void setCurrentItem(ItemStack item) {
        this.sampleItem = item;
        player.setItemInHand(hand, item);
    }

    @Override
    public void markAsDirty() {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
    }
}