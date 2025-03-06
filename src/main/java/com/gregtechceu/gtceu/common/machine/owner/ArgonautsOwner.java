package com.gregtechceu.gtceu.common.machine.owner;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;
import net.minecraftforge.server.ServerLifecycleHooks;

import earth.terrarium.argonauts.api.client.guild.GuildClientApi;
import earth.terrarium.argonauts.api.guild.Guild;
import earth.terrarium.argonauts.api.guild.GuildApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({ "UnstableApiUsage", "removal", "deprecation" })
public class ArgonautsOwner extends MachineOwner {

    @Getter
    private UUID playerUUID;

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
    public void displayInfo(List<Component> compList) {
        compList.add(Component.translatable("behavior.portable_scanner.machine_ownership", type().getName()));
        compList.add(Component.translatable("behavior.portable_scanner.guild_name", getName()));
        IMachineOwner.displayPlayerInfo(compList, playerUUID);
    }

    @Override
    public MachineOwnerType type() {
        return MachineOwnerType.ARGONAUTS;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ArgonautsOwner that)) return false;

        return playerUUID.equals(that.playerUUID);
    }

    @Override
    public int hashCode() {
        return playerUUID.hashCode();
    }
}
