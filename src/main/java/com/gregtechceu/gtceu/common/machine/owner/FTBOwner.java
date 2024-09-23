package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import dev.ftb.mods.ftbteams.FTBTeamsAPIImpl;
import dev.ftb.mods.ftbteams.api.Team;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public final class FTBOwner implements IMachineOwner {

    @Getter
    private Team team;
    @Getter
    private UUID playerUUID;

    public FTBOwner() {}

    public FTBOwner(Team team, UUID player) {
        this.team = team;
        this.playerUUID = player;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putUUID("teamUUID", team.getTeamId());
        tag.putUUID("playerUUID", playerUUID);
    }

    @Override
    public void load(CompoundTag tag) {
        this.team = FTBTeamsAPIImpl.INSTANCE.getManager().getTeamByID(tag.getUUID("teamUUID")).orElse(null);
        this.playerUUID = tag.getUUID("playerUUID");
    }

    @Override
    public boolean isPlayerInTeam(Player player) {
        if (player.getUUID().equals(this.playerUUID)) return true;
        if (FTBTeamsAPIImpl.INSTANCE.getManager().arePlayersInSameTeam(player.getUUID(), this.playerUUID)) return true;

        return false;
    }

    @Override
    public boolean isPlayerFriendly(Player player) {
        if (team.getRankForPlayer(player.getUUID()).isAllyOrBetter()) return true;
        return false;
    }

    @Override
    public void displayInfo(List<Component> compList) {
        compList.add(Component.translatable("behavior.portable_scanner.machine_ownership", type().getName()));
        compList.add(Component.translatable("behavior.portable_scanner.team_name", team.getName()));
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
        return MachineOwnerType.FTB;
    }
}
