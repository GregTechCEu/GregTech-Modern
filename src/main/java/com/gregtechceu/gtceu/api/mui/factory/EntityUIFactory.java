package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IUIHolder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntityUIFactory extends AbstractUIFactory<EntityGuiData> {

    public static final EntityUIFactory INSTANCE = new EntityUIFactory();

    protected EntityUIFactory() {
        super(GTCEu.id("entity"));
    }

    public <E extends Entity & IUIHolder<EntityGuiData>> void open(Player player, E entity) {
        Objects.requireNonNull(player);
        verifyEntity(player, entity);
        GuiManager.open(this, new EntityGuiData(player, entity), (ServerPlayer) player);
    }

    private static <E extends Entity & IUIHolder<EntityGuiData>> void verifyEntity(Player player, E entity) {
        Objects.requireNonNull(entity);
        if (!entity.isAlive()) {
            throw new IllegalArgumentException("Can't open dead Entity GUI!");
        } else if (player.level() != entity.level()) {
            throw new IllegalArgumentException("Entity must be in same dimension as the player!");
        }
    }

    @Override
    public @NotNull IUIHolder<EntityGuiData> getGuiHolder(EntityGuiData guiData) {
        return Objects.requireNonNull(castUIHolder(guiData.getGuiHolder()), "Found Entity is not a gui holder!");
    }

    @Override
    public void writeGuiData(EntityGuiData guiData, FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(guiData.getGuiHolder().getId());
    }

    @Override
    public @NotNull EntityGuiData readGuiData(Player entityPlayer, FriendlyByteBuf packetBuffer) {
        return new EntityGuiData(entityPlayer, entityPlayer.level().getEntity(packetBuffer.readInt()));
    }

    @Override
    public boolean canInteractWith(Player player, EntityGuiData guiData) {
        Entity guiHolder = guiData.getGuiHolder();
        return super.canInteractWith(player, guiData) &&
                guiHolder != null &&
                player.distanceToSqr(guiHolder.getX(), guiHolder.getY(), guiHolder.getZ()) <= 64 &&
                player.level() == guiHolder.level() &&
                guiHolder.isAlive();
    }
}
