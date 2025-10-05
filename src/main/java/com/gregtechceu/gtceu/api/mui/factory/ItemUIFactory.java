package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IUIHolder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ItemUIFactory extends AbstractUIFactory<HandGuiData> {

    public static final ItemUIFactory INSTANCE = new ItemUIFactory();

    private ItemUIFactory() {
        super(GTCEu.id("item"));
    }

    public void open(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer entityServerPlayer) {
            open(entityServerPlayer, hand);
            return;
        }
        throw new IllegalStateException("Synced GUIs must be opened from server side");
    }

    public void open(ServerPlayer player, InteractionHand hand) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(hand);
        HandGuiData guiData = new HandGuiData(player, hand);
        GuiManager.open(this, guiData, player);
    }

    @Override
    public @NotNull IUIHolder<HandGuiData> getGuiHolder(HandGuiData data) {
        return Objects.requireNonNull(castUIHolder(data.getUsedItem().getItem()), "Item was not a gui holder!");
    }

    @Override
    public void writeGuiData(HandGuiData guiData, FriendlyByteBuf buffer) {
        buffer.writeByte(guiData.getHand().ordinal());
    }

    @Override
    public @NotNull HandGuiData readGuiData(Player player, FriendlyByteBuf buffer) {
        return new HandGuiData(player, InteractionHand.values()[buffer.readByte()]);
    }
}
