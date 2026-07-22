package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.UsernameCache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public non-sealed class PlayerOwner extends MachineOwner {

    private static final Component displayName = Component.translatable("gtceu.ownership.name.player");

    public PlayerOwner(UUID playerUUID) {
        super(playerUUID);
    }

    @UnmodifiableView
    @Override
    public @NotNull Set<UUID> getMembers() {
        return Set.of(getUUID());
    }

    @Override
    public boolean isPlayerInTeam(UUID playerUUID) {
        return this.playerUUID.equals(playerUUID);
    }

    @Override
    public boolean isPlayerFriendly(UUID playerUUID) {
        return this.playerUUID.equals(playerUUID);
    }

    @Override
    public UUID getUUID() {
        return playerUUID;
    }

    @Override
    public String getName() {
        return UsernameCache.getLastKnownUsername(playerUUID);
    }

    @Override
    public Component getTypeDisplayName() {
        return displayName;
    }

    @Override
    public void displayInfo(List<Component> compList) {
        super.displayInfo(compList);
        MachineOwner.displayPlayerInfo(compList, playerUUID);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof PlayerOwner && super.equals(object);
    }
}
