package com.gregtechceu.gtceu.api.mui.factory;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
public class HandGuiData extends GuiData {

    @Getter
    private final InteractionHand hand;

    public HandGuiData(Player player, InteractionHand hand) {
        super(player);
        this.hand = hand;
    }

    public ItemStack getUsedItem() {
        return getPlayer().getItemInHand(this.hand);
    }

    public void setItemInMainHand(ItemStack item) {
        getPlayer().setItemInHand(InteractionHand.MAIN_HAND, item);
    }

    public void setItemInOffHand(ItemStack item) {
        getPlayer().setItemInHand(InteractionHand.OFF_HAND, item);
    }

    public void setItemInUsedHand(ItemStack item) {
        getPlayer().setItemInHand(this.hand, item);
    }
}
