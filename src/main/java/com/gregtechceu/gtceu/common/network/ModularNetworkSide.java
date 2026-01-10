package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.common.network.packets.ui.CloseAllGuiPacket;
import com.gregtechceu.gtceu.common.network.packets.ui.CloseGuiPacket;
import com.gregtechceu.gtceu.common.network.packets.ui.ReopenGuiPacket;
import com.gregtechceu.gtceu.common.network.packets.ui.SyncHandlerPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

public abstract class ModularNetworkSide {

    @Getter
    private final boolean client;
    private final Int2ReferenceOpenHashMap<ModularSyncManager> activeScreens = new Int2ReferenceOpenHashMap<>();
    private final Reference2IntOpenHashMap<ModularSyncManager> inverseActiveScreens = new Reference2IntOpenHashMap<>();
    // TODO: contextual syncer stack: in game containers shouldn't be closed in closeAll

    ModularNetworkSide(boolean client) {
        this.client = client;
    }

    abstract void sendPacket(GTNetwork.INetPacket packet, Player player);

    void activateInternal(int networkId, ModularSyncManager manager) {
        if (activeScreens.containsKey(networkId))
            throw new IllegalStateException("Network ID " + networkId + " is already active.");
        activeScreens.put(networkId, manager);
        inverseActiveScreens.put(manager, networkId);
    }

    public void closeAll(Player player) {
        closeAll(player, true);
    }

    @ApiStatus.Internal
    public void closeAll(Player player, boolean sync) {
        if (activeScreens.isEmpty()) return;
        int currentContainer = -1;
        if (player.containerMenu instanceof ModularContainerMenu mc && !mc.isClientOnly()) {
            currentContainer = inverseActiveScreens.getInt(mc.getSyncManager());
        }
        var it = activeScreens.int2ReferenceEntrySet().fastIterator();
        while (it.hasNext()) {
            var entry = it.next();
            int nid = entry.getIntKey();
            ModularSyncManager msm = entry.getValue();
            if (nid == currentContainer) closeContainer(player);
            if (!msm.isClosed()) msm.onClose();
            msm.dispose();
        }
        activeScreens.clear();
        inverseActiveScreens.clear();
        if (sync) sendPacket(new CloseAllGuiPacket(), player);
    }

    @ApiStatus.Internal
    public void receivePacket(SyncHandlerPacket packet) {
        ModularSyncManager msm = activeScreens.get(packet.networkId);
        if (msm == null) return; // silently discard packets for inactive screens
        try {
            int id = packet.action ? 0 : packet.packet.readVarInt();
            msm.receiveWidgetUpdate(packet.panel, packet.key, packet.action, id, packet.packet);
        } catch (IndexOutOfBoundsException e) {
            GTCEu.LOGGER.error("Failed to read packet for sync handler {} in panel {}", packet.key, packet.panel);
        }
    }

    @ApiStatus.Internal
    public void sendSyncHandlerPacket(String panel, SyncHandler syncHandler, FriendlyByteBuf buffer, Player player) {
        ModularSyncManager msm = syncHandler.getSyncManager().getModularSyncManager();
        if (!inverseActiveScreens.containsKey(msm)) return;
        int id = inverseActiveScreens.getInt(msm);
        sendPacket(new SyncHandlerPacket(id, panel, syncHandler.getKey(), false, buffer), player);
    }

    @ApiStatus.Internal
    public void sendActionPacket(ModularSyncManager msm, String panel, String key, FriendlyByteBuf buffer,
                                 Player player) {
        if (!inverseActiveScreens.containsKey(msm)) return;
        int id = inverseActiveScreens.getInt(msm);
        sendPacket(new SyncHandlerPacket(id, panel, key, true, buffer), player);
    }

    @ApiStatus.Internal
    public void closeContainer(int networkId, boolean dispose, Player player, boolean sync) {
        closeContainer(player);
        deactivate(networkId, dispose);
        if (sync) {
            sendPacket(new CloseGuiPacket(networkId, dispose), player);
        }
    }

    abstract void closeContainer(Player player);

    void deactivate(int networkId, boolean dispose) {
        ModularSyncManager msm = activeScreens.get(networkId);
        if (msm == null) return;
        if (!msm.isClosed()) msm.onClose();
        if (dispose) {
            activeScreens.remove(networkId);
            inverseActiveScreens.removeInt(msm);
            msm.dispose();
        }
    }

    @ApiStatus.Internal
    public void reopen(Player player, ModularSyncManager msm, boolean sync) {
        if (player.containerMenu != msm.getMenu()) {
            closeContainer(player);
            player.containerMenu = msm.getMenu();
            msm.onOpen();
            MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, msm.getMenu()));
        }
        if (sync) sendPacket(new ReopenGuiPacket(inverseActiveScreens.getInt(msm)), player);
    }

    @ApiStatus.Internal
    public void reopen(Player player, int networkId, boolean sync) {
        reopen(player, activeScreens.get(networkId), sync);
    }
}
