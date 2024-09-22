package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraft.nbt.CompoundTag;

import dev.ftb.mods.ftbteams.FTBTeamsAPIImpl;
import dev.ftb.mods.ftbteams.api.Team;
import lombok.Getter;
import net.minecraft.world.entity.player.Player;

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
        if(player.getUUID().equals(this.playerUUID)) return true;
        if(FTBTeamsAPIImpl.INSTANCE.getManager().arePlayersInSameTeam(player.getUUID(), this.playerUUID)) return true;

        return false;
    }

    @Override
    public boolean isPlayerFriendly(Player player) {
        if(team.getRankForPlayer(player.getUUID()).isAllyOrBetter()) return true;
        return false;
    }

    @Override
    public MachineOwnerType type() {
        return MachineOwnerType.FTB;
    }
}
