package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class GTOwner implements IMachineOwner {

    @Getter
    private UUID player;

    public GTOwner() {}

    public GTOwner(UUID player) {
        this.player = player;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putUUID("UUID", player);
    }

    @Override
    public void load(CompoundTag tag) {
        this.player = tag.getUUID("UUID");
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
    public MachineOwnerType type() {
        return MachineOwnerType.GT;
    }
}
