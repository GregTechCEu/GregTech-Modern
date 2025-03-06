package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.UsernameCache;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

public class PlayerOwner extends MachineOwner {

    private UUID playerUUID;

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
    public void displayInfo(List<Component> compList) {
        compList.add(Component.translatable("behavior.portable_scanner.machine_ownership", type().getName()));
        IMachineOwner.displayPlayerInfo(compList, playerUUID);
    }

    @Override
    public MachineOwnerType type() {
        return MachineOwnerType.PLAYER;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PlayerOwner that)) return false;

        return playerUUID.equals(that.playerUUID);
    }

    @Override
    public int hashCode() {
        return playerUUID.hashCode();
    }
}
