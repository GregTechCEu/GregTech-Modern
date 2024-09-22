package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.server.ServerLifecycleHooks;

import earth.terrarium.argonauts.api.guild.Guild;
import earth.terrarium.argonauts.common.handlers.guild.GuildHandler;
import lombok.Getter;

import java.util.UUID;

public final class ArgonautsOwner implements IMachineOwner {

    @Getter
    private Guild guild;
    @Getter
    private UUID player;

    public ArgonautsOwner() {}

    public ArgonautsOwner(Guild guild, UUID player) {
        this.guild = guild;
        this.player = player;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putUUID("guildUUID", guild.id());
        tag.putUUID("playerUUID", player);
    }

    @Override
    public void load(CompoundTag tag) {
        this.player = tag.getUUID("playerUUID");
        this.guild = GuildHandler.read(ServerLifecycleHooks.getCurrentServer())
                .get(ServerLifecycleHooks.getCurrentServer(), player);
    }

    @Override
    public MachineOwnerType type() {
        return MachineOwnerType.ARGONAUTS;
    }
}
