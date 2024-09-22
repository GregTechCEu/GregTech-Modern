package com.gregtechceu.gtceu.common.machine.owner;

import earth.terrarium.argonauts.common.handlers.base.MemberException;
import earth.terrarium.argonauts.common.handlers.base.members.MemberState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import earth.terrarium.argonauts.api.guild.Guild;
import earth.terrarium.argonauts.common.handlers.guild.GuildHandler;
import lombok.Getter;

import java.util.UUID;

public final class ArgonautsOwner implements IMachineOwner {

    @Getter
    private Guild guild;
    @Getter
    private UUID playerUUID;
    private MinecraftServer server;

    public ArgonautsOwner() {}

    public ArgonautsOwner(Guild guild, UUID player) {
        this.guild = guild;
        this.playerUUID = player;
        this.server = ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putUUID("guildUUID", guild.id());
        tag.putUUID("playerUUID", playerUUID);
    }

    @Override
    public void load(CompoundTag tag) {
        this.playerUUID = tag.getUUID("playerUUID");
        this.server = ServerLifecycleHooks.getCurrentServer();
        var handler = GuildHandler.read(server);
        this.guild = handler.get(server.getPlayerList().getPlayer(playerUUID));
    }

    @Override
    public boolean isPlayerInTeam(Player player) {
        if(player.getUUID().equals(this.playerUUID)) return true;
        var otherGuild = GuildHandler.read(server).get(server, player.getUUID());
        if(otherGuild != null && otherGuild.equals(this.guild)) return true;

        return false;
    }

    @Override
    public boolean isPlayerFriendly(Player player) {
        if(guild.isPublic()) return true;

        if(guild.members().isMember(player.getUUID())) return true;
        if(guild.members().isInvited(player.getUUID())) return true;
        if(guild.members().isAllied(player.getUUID())) return true;
        return false;
    }

    @Override
    public MachineOwnerType type() {
        return MachineOwnerType.ARGONAUTS;
    }
}
