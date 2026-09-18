package brachy.modularui.network;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IPacketWriter;
import brachy.modularui.network.packets.CloseAllGuisPacket;
import brachy.modularui.network.packets.CloseGuiPacket;
import brachy.modularui.network.packets.ReopenGuiPacket;
import brachy.modularui.network.packets.SyncHandlerPacket;
import brachy.modularui.screen.ModularContainerMenu;
import brachy.modularui.value.sync.ModularSyncManager;
import brachy.modularui.value.sync.SyncHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

public abstract class ModularNetworkSide {

    private final Int2ReferenceOpenHashMap<ModularSyncManager> activeScreens = new Int2ReferenceOpenHashMap<>();
    private final Reference2IntOpenHashMap<ModularSyncManager> inverseActiveScreens = new Reference2IntOpenHashMap<>();
    // TODO: contextual syncer stack: in game containers shouldn't be closed in closeAll

    public abstract boolean isClient();

    abstract void sendPacket(CustomPacketPayload packet, Player player);

    void activateInternal(int networkId, ModularSyncManager manager) {
        if (activeScreens.containsKey(networkId)) throw new IllegalStateException("Network ID " + networkId + " is already active.");
        activeScreens.put(networkId, manager);
        inverseActiveScreens.put(manager, networkId);
    }

    public void onPlayerLeave(Player player) {
        this.activeScreens.clear();
        this.inverseActiveScreens.clear();
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
        if (sync) sendPacket(new CloseAllGuisPacket(), player);
    }

    @ApiStatus.Internal
    public void receivePacket(Player player, SyncHandlerPacket packet) {
        ModularSyncManager msm = activeScreens.get(packet.networkId());
        if (msm == null) return; // silently discard packets for inactive screens
        try {
            int id = packet.action() ? 0 : packet.packet().readVarInt();
            msm.receiveWidgetUpdate(packet.panel(), packet.key(), packet.action(), id, packet.packet());
        } catch (IndexOutOfBoundsException e) {
            ModularUI.LOGGER.error("Failed to read packet for sync handler {} in panel {}", packet.key(), packet.panel());
            ModularUI.LOGGER.catching(e);
        }
    }

    @ApiStatus.Internal
    public void sendSyncHandlerPacket(String panel, SyncHandler<?> syncHandler, IPacketWriter<? super RegistryFriendlyByteBuf> writer, Player player) {
        ModularSyncManager msm = syncHandler.getSyncManager().getModularSyncManager();
        if (!inverseActiveScreens.containsKey(msm)) return;
        int id = inverseActiveScreens.getInt(msm);
        sendPacket(new SyncHandlerPacket(id, panel, syncHandler.getKey(), false, writer), player);
    }

    @ApiStatus.Internal
    public void sendActionPacket(ModularSyncManager msm, String panel, String key,
                                 IPacketWriter<? super RegistryFriendlyByteBuf> writer, Player player) {
        if (!inverseActiveScreens.containsKey(msm)) return;
        int id = inverseActiveScreens.getInt(msm);
        sendPacket(new SyncHandlerPacket(id, panel, key, true, writer), player);
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
            NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, msm.getMenu()));
        }
        if (sync) sendPacket(new ReopenGuiPacket(inverseActiveScreens.getInt(msm)), player);
    }

    @ApiStatus.Internal
    public void reopen(Player player, int networkId, boolean sync) {
        reopen(player, activeScreens.get(networkId), sync);
    }
}
