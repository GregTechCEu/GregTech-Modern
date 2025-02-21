package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.UUID;

public final class PlayerOwner implements IMachineOwner {

    private UUID playerUUID;

    public PlayerOwner() {}

    public PlayerOwner(UUID player) {
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
        var serverPlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
        final String[] playerName = new String[1];
        boolean isOnline;
        if (serverPlayer != null) {
            playerName[0] = serverPlayer.getDisplayName().getString();
            isOnline = true;
        } else {
            var cache = ServerLifecycleHooks.getCurrentServer().getProfileCache();
            if (cache != null) {
                cache.get(playerUUID).ifPresent(value -> playerName[0] = value.getName());
            }
            isOnline = false;
        }
        compList.add(Component.translatable("behavior.portable_scanner.player_name", playerName[0], isOnline));
    }

    @Override
    public MachineOwnerType type() {
        return MachineOwnerType.PLAYER;
    }
}
