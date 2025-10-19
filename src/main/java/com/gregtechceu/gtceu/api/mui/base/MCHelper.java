package com.gregtechceu.gtceu.api.mui.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
