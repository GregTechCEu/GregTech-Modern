package com.gregtechceu.gtceu.api.gui.factory;

import com.gregtechceu.gtceu.GTCEu;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class GTHeldItemUIFactory extends UIFactory<GTHeldItemUIHolder> {

    public static final GTHeldItemUIFactory INSTANCE = new GTHeldItemUIFactory();

    public GTHeldItemUIFactory() {
        super(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "held_item"));
    }

    public boolean openUI(ServerPlayer player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof IGTHeldItemUI heldItemUI) {
            return openUI(heldItemUI.createUIHolder(player, hand), player);
        }
        return false;
    }

    @Override
    protected ModularUI createUITemplate(GTHeldItemUIHolder holder, Player entityPlayer) {
        return holder.createUI(entityPlayer);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected GTHeldItemUIHolder readHolderFromSyncData(RegistryFriendlyByteBuf syncData) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return null;
        InteractionHand hand = syncData.readEnum(InteractionHand.class);
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof IGTHeldItemUI heldItemUI) {
            return heldItemUI.createUIHolder(player, hand);
        }
        return null;
    }

    @Override
    protected void writeHolderToSyncData(RegistryFriendlyByteBuf syncData, GTHeldItemUIHolder holder) {
        syncData.writeEnum(holder.getHand());
    }
}
