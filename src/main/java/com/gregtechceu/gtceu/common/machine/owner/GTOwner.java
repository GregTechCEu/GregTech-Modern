package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

public final class GTOwner implements IMachineOwner {

    @Getter
    private UUID playerUUID;

    public GTOwner() {}

    public GTOwner(UUID player) {
        this.playerUUID = player;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putUUID("UUID", playerUUID);
    }

    @Override
    public void load(CompoundTag tag) {
        this.playerUUID = tag.getUUID("UUID");
    }

    @Override
    public boolean isPlayerInTeam(Player player) {
        return true;
    }

    @Override
    public boolean isPlayerFriendly(Player player) {
        return true;
    }

    @Override
    public void displayInfo(List<Component> compList) {
        compList.add(Component.translatable("behavior.portable_scanner.machine_ownership", type().getName()));
        var serverPlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
        String playerName;
        boolean isOnline;
        if (serverPlayer != null) {
            playerName = serverPlayer.getDisplayName().getString();
            isOnline = true;
        } else {
            playerName = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(playerUUID).get().getName();
            isOnline = false;
        }
        compList.add(Component.translatable("behavior.portable_scanner.player_name", playerName, isOnline));
    }

    @Override
    public MachineOwnerType type() {
        return MachineOwnerType.GT;
    }
}
