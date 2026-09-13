package com.gregtechceu.gtceu.api.mui.modular_item;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.*;
import brachy.modularui.factory.inventory.InventoryType;
import brachy.modularui.factory.inventory.InventoryTypes;

public class ModularItemUIFactory extends AbstractUIFactory<PlayerInventoryGuiData<?>> {

    public static final ModularItemUIFactory INSTANCE = new ModularItemUIFactory();

    protected ModularItemUIFactory() {
        super(GTCEu.id("modular_item"));
    }

    public void openFromHand(Player player, InteractionHand hand) {
        GuiManager.open(
                this, PlayerInventoryGuiData.of(player, InventoryTypes.PLAYER, null, player.getInventory().selected),
                verifyServerSide(player));
    }

    @Override
    public IUIHolder<PlayerInventoryGuiData<?>> getGuiHolder(PlayerInventoryGuiData<?> data) {
        return new ModularItemManagerUI();
    }

    @Override
    public void writeGuiData(PlayerInventoryGuiData<?> guiData, FriendlyByteBuf buffer) {
        guiData.getInventoryType().write(buffer);
        writeContext(buffer, guiData.getInventoryType(), guiData.getContext());
        buffer.writeVarInt(guiData.getSlotIndex());
    }

    private static <T> void writeContext(FriendlyByteBuf buffer, InventoryType<T> type, Object context) {
        type.writeContext(buffer, type.castContext(context));
    }

    @Override
    public PlayerInventoryGuiData<?> readGuiData(Player player, FriendlyByteBuf buffer) {
        return readContext(player, buffer, InventoryType.read(buffer));
    }

    private static <T> PlayerInventoryGuiData<?> readContext(Player player, FriendlyByteBuf buffer,
                                                             InventoryType<T> inventoryType) {
        return PlayerInventoryGuiData.of(player, inventoryType, inventoryType.readContext(buffer), buffer.readVarInt());
    }
}
