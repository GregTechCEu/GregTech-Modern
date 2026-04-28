package com.gregtechceu.gtceu.api.gui.factory;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GTHeldItemUIHolder implements IUIHolder {

    private final IGTHeldItemUI heldItemUI;
    private final Player player;
    private final InteractionHand hand;
    private final ItemStack held;

    public GTHeldItemUIHolder(IGTHeldItemUI heldItemUI, Player player, InteractionHand hand) {
        this.heldItemUI = heldItemUI;
        this.player = player;
        this.hand = hand;
        this.held = player.getItemInHand(hand);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return heldItemUI.createUI(entityPlayer, this);
    }

    @Override
    public boolean isInvalid() {
        return !ItemStack.isSameItemSameComponents(player.getItemInHand(hand), held);
    }

    @Override
    public boolean isRemote() {
        return player.level().isClientSide();
    }

    @Override
    public void markAsDirty() {}

    public Player getPlayer() {
        return player;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getHeld() {
        return held;
    }
}
