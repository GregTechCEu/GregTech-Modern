package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public abstract class ModularNetwork {

    // You have to make sure you are choosing the logical side you are currently on otherwise you can mess things badly,
    // since
    // there is no validation.
    public static final Client CLIENT = new Client();
    public static final Server SERVER = new Server();

    public static ModularNetworkSide get(boolean client) {
        return client ? CLIENT : SERVER;
    }

    public static ModularNetworkSide get(Side side) {
        return side.isClient() ? CLIENT : SERVER;
    }

    public static ModularNetworkSide get(Player player) {
        return get(NetworkUtils.isClient(player));
    }

    public static final class Client extends ModularNetworkSide {

        private Client() {
            super(true);
        }

        public void activate(int nid, ModularSyncManager msm) {
            activateInternal(nid, msm);
        }

        @Override
        void sendPacket(GTNetwork.INetPacket packet, Player player) {
            GTNetwork.sendToServer(packet);
        }

        @Override
        void closeContainer(Player player) {
            // mimics EntityPlayerSP.closeScreenAndDropStack() but without closing the screen
            player.getInventory().setPickedItem(ItemStack.EMPTY);
            player.containerMenu = player.inventoryMenu;
        }

        @SideOnly(Side.CLIENT)
        public void closeContainer(int networkId, boolean dispose, Player player) {
            closeContainer(networkId, dispose, player, true);
        }

        @SideOnly(Side.CLIENT)
        public void closeAll() {
            closeAll(Minecraft.getInstance().player);
        }

        @SideOnly(Side.CLIENT)
        public void reopenSyncerOf(Screen guiScreen) {
            if (guiScreen instanceof IMuiScreen ms && !ms.getScreen().isClientOnly()) {
                ModularSyncManager msm = ms.getScreen().getSyncManager();
                reopen(Minecraft.getInstance().player, msm, true);
            }
        }
    }

    public static final class Server extends ModularNetworkSide {

        private int nextId = -1;

        private Server() {
            super(false);
        }

        public int activate(ModularSyncManager msm) {
            if (++nextId > 100_000) nextId = 0;
            activateInternal(nextId, msm);
            return nextId;
        }

        @Override
        protected void sendPacket(GTNetwork.INetPacket packet, Player player) {
            GTNetwork.sendToPlayer((ServerPlayer) player, packet);
        }

        @Override
        void closeContainer(Player player) {
            ((ServerPlayer) player).closeContainer();
        }

        public void closeContainer(int networkId, boolean dispose, ServerPlayer player) {
            closeContainer(networkId, dispose, player, true);
        }
    }
}
