package com.gregtechceu.gtceu.api.mui.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class MCHelper {

    public static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static Player getPlayer() {
        return getMc().player;
    }

    public static boolean closeScreen() {
        Player player = getMc().player;
        if (player != null) {
            player.closeContainer();
            return true;
        }
        getMc().popGuiLayer();
        return false;
    }

    public static void popScreen(boolean openParentOnClose, Screen parent) {
        Player player = MCHelper.getPlayer();
        if (player != null) {
            prepareCloseContainer(player);
            if (openParentOnClose) {
                Minecraft.getInstance().setScreen(parent);
            } else {
                Minecraft.getInstance().setScreen(null);
            }
        } else {
            // we are currently not in a world and want to display the previous screen
            Minecraft.getInstance().setScreen(parent);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void prepareCloseContainer(Player currentPlayer) {
        LocalPlayer player = (LocalPlayer) currentPlayer;
        player.connection.send(new ServerboundContainerClosePacket(player.containerMenu.containerId));
        player.containerMenu = player.inventoryMenu;
        player.inventoryMenu.setCarried(ItemStack.EMPTY);
    }

    public static void setScreen(Screen screen) {
        if (screen == null) {
            closeScreen();
        } else {
            getMc().setScreen(screen);
        }
    }

    public static Screen getCurrentScreen() {
        return getMc().screen;
    }

    public static Font getFont() {
        return getMc().font;
    }

    public static List<Component> getItemToolTip(ItemStack item) {
        return Screen.getTooltipFromItem(getMc(), item);
    }
}
