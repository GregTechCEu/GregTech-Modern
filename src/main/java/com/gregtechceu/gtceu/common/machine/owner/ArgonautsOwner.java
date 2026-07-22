package com.gregtechceu.gtceu.common.machine.owner;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;
import net.minecraftforge.server.ServerLifecycleHooks;

import earth.terrarium.argonauts.api.client.guild.GuildClientApi;
import earth.terrarium.argonauts.api.guild.Guild;
import earth.terrarium.argonauts.api.guild.GuildApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

@SuppressWarnings({ "UnstableApiUsage", "removal", "deprecation" })
public non-sealed class ArgonautsOwner extends MachineOwner {

    private static final Component displayName = Component.translatable("gtceu.ownership.name.argonauts");

    public ArgonautsOwner(UUID playerUUID) {
        super(playerUUID);
    }

    public @Nullable Guild getPlayerGuild(UUID playerUUID) {
        if (GTCEu.isClientThread()) {
            return GuildClientApi.API.getPlayerGuild(playerUUID);
        } else {
            return GuildApi.API.getPlayerGuild(ServerLifecycleHooks.getCurrentServer(), playerUUID);
        }
    }

    public @Nullable Guild getGuild() {
        return getPlayerGuild(playerUUID);
    }

    @UnmodifiableView
    @Override
    public @NotNull Set<UUID> getMembers() {
        var guild = getGuild();
        if (guild == null) return Collections.emptySet();
        Set<UUID> members = new HashSet<>(guild.members().size());
        for (var member : guild.members().allMembers()) {
            members.add(member.profile().getId());
        }
        return members;
    }

    @Override
    public boolean isPlayerInTeam(UUID playerUUID) {
        if (this.playerUUID.equals(playerUUID)) return true;
        var otherGuild = getPlayerGuild(playerUUID);
        return otherGuild != null && otherGuild.equals(getGuild());
    }

    @Override
    public boolean isPlayerFriendly(UUID playerUUID) {
        var guild = getGuild();
        if (guild == null) {
            return this.playerUUID.equals(playerUUID);
        }
        return guild.isPublic() || guild.members().isMember(playerUUID) || guild.members().isAllied(playerUUID);
    }

    @Override
    public UUID getUUID() {
        var guild = getGuild();
        return guild != null ? guild.id() : EMPTY;
    }

    @Override
    public String getName() {
        var guild = getGuild();
        return guild != null ? guild.displayName().getString() :
                Component.translatable("gtceu.tooltip.status.trinary.unknown").getString();
    }

    @Override
    public Component getTypeDisplayName() {
        return displayName;
    }

    @Override
    public void displayInfo(List<Component> compList) {
        super.displayInfo(compList);
        compList.add(Component.translatable("behavior.portable_scanner.guild_name", getName()));
        MachineOwner.displayPlayerInfo(compList, playerUUID);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ArgonautsOwner && super.equals(object);
    }
}
