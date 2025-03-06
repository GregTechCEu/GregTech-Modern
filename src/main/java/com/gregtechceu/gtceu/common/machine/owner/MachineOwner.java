package com.gregtechceu.gtceu.common.machine.owner;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.server.ServerLifecycleHooks;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
public abstract class MachineOwner {

public sealed interface IMachineOwner permits PlayerOwner, ArgonautsOwner, FTBOwner {

    UUID EMPTY = new UUID(0, 0);
    Map<UUID, IMachineOwner> MACHINE_OWNERS = new Object2ObjectOpenHashMap<>();
    Map<UUID, PlayerOwner> PLAYER_OWNERS = new Object2ObjectOpenHashMap<>();

    MachineOwnerType type();

    void displayInfo(List<Component> compList);

    static void displayPlayerInfo(List<Component> compList, UUID playerUUID) {
        final var playerName = UsernameCache.getLastKnownUsername(playerUUID);
        var online = "gtceu.tooltip.status.trinary.";
        if (GTCEu.isClientThread()) {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                online += connection.getOnlinePlayerIds().contains(playerUUID);
            } else {
                online += "unknown";
            }
        } else {
            online += ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID) != null;
        }
        compList.add(Component.translatable("behavior.portable_scanner.player_name",
                playerName, Component.translatable(online)));
    }

    static @Nullable IMachineOwner getOwner(UUID playerUUID) {
        if (playerUUID == null) {
            return null;
        }
        return MACHINE_OWNERS.computeIfAbsent(playerUUID, IMachineOwner::makeOwner);
    }

    /**
     * Do not use this method, use the caching {@link #getOwner(UUID)} or {@link #getPlayerOwner(UUID)} instead
     * 
     * @param playerUUID the uuid of the player who owns the machine
     * @return ownership object
     */
    @ApiStatus.Internal
    private static IMachineOwner makeOwner(UUID playerUUID) {
        IMachineOwner owner;
        if (IMachineOwner.MachineOwnerType.FTB.isAvailable()) {
            owner = new FTBOwner(playerUUID);
        } else if (IMachineOwner.MachineOwnerType.ARGONAUTS.isAvailable()) {
            owner = new ArgonautsOwner(playerUUID);
        } else {
            owner = getPlayerOwner(playerUUID);
        }
        return owner;
    }

    static @Nullable PlayerOwner getPlayerOwner(UUID playerUUID) {
        if (playerUUID == null) {
            return null;
        }
        return PLAYER_OWNERS.computeIfAbsent(playerUUID, PlayerOwner::new);
    }

    default boolean isPlayerInTeam(Player player) {
        return isPlayerInTeam(player.getUUID());
    }

    boolean isPlayerInTeam(UUID playerUUID);

    default boolean isPlayerFriendly(Player player) {
        return isPlayerFriendly(player.getUUID());
    }

    boolean isPlayerFriendly(UUID playerUUID);

    static boolean canOpenOwnerMachine(Player player, MetaMachine machine) {
        if (!ConfigHolder.INSTANCE.machines.onlyOwnerGUI) return true;
        if (player.hasPermissions(ConfigHolder.INSTANCE.machines.ownerOPBypass)) return true;
        var owner = machine.getOwner();
        if (owner == null) return true;
        return owner.isPlayerInTeam(player) || owner.isPlayerFriendly(player);
    }

    static boolean canBreakOwnerMachine(Player player, MetaMachine machine) {
        if (!ConfigHolder.INSTANCE.machines.onlyOwnerBreak) return true;
        if (player.hasPermissions(ConfigHolder.INSTANCE.machines.ownerOPBypass)) return true;
        var owner = machine.getOwner();
        if (owner == null) return true;
        return owner.isPlayerInTeam(player);
    }

    UUID getUUID();

    String getName();

    enum MachineOwnerType {

        PLAYER(() -> true, "Player"),
        FTB(GTCEu.Mods::isFTBTeamsLoaded, "FTB Teams"),
        ARGONAUTS(GTCEu.Mods::isArgonautsLoaded, "Argonauts Guild");

        public static final MachineOwnerType[] VALUES = values();

        private BooleanSupplier availabilitySupplier;
        private boolean available;

        @Getter
        private final String name;

        MachineOwnerType(BooleanSupplier availabilitySupplier, String name) {
            this.availabilitySupplier = availabilitySupplier;
            this.name = name;
        }

        public boolean isAvailable() {
            if (availabilitySupplier != null) {
                this.available = availabilitySupplier.getAsBoolean();
                this.availabilitySupplier = null;
            }
            return available;
        }
    }
}
