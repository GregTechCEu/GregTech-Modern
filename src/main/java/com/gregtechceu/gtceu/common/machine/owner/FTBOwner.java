package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraft.nbt.CompoundTag;

import dev.ftb.mods.ftbteams.FTBTeamsAPIImpl;
import dev.ftb.mods.ftbteams.api.Team;
import lombok.Getter;

import java.util.UUID;

public final class FTBOwner implements IMachineOwner {

    @Getter
    private Team team;
    @Getter
    private UUID player;

    public FTBOwner() {}

    public FTBOwner(Team team, UUID player) {
        this.team = team;
        this.player = player;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putUUID("teamUUID", team.getTeamId());
        tag.putUUID("playerUUID", player);
    }

    @Override
    public void load(CompoundTag tag) {
        this.team = FTBTeamsAPIImpl.INSTANCE.getManager().getTeamByID(tag.getUUID("teamUUID")).orElse(null);
        this.player = tag.getUUID("playerUUID");
    }

    @Override
    public MachineOwnerType type() {
        return MachineOwnerType.FTB;
    }
}
