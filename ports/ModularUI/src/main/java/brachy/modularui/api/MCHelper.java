package brachy.modularui.api;

import brachy.modularui.network.ModularNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class MCHelper {

    @SuppressWarnings("DataFlowIssue")
    @Nullable
    public static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    @Nullable
    public static Player getPlayer() {
        return getMc() == null ? null : getMc().player;
    }

    public static void closeScreen() {
        getMc().popGuiLayer();
    }

    public static void popScreen(boolean openParentOnClose, Screen parent) {
        Player player = MCHelper.getPlayer();
        if (player != null) {
            // container should not just be closed here
            // instead they are kept in a stack until all screens are closed
            // prepareCloseContainer(player);
            if (openParentOnClose) {
                Minecraft.getInstance().setScreen(parent);
                ModularNetwork.CLIENT.reopenSyncerOf(parent);
            } else {
                Minecraft.getInstance().setScreen(null);
            }
        } else {
            // we are currently not in a world and want to display the previous screen
            Minecraft.getInstance().setScreen(parent);
        }
    }

    public static void setScreen(Screen screen) {
        if (screen == null) {
            closeScreen();
        } else {
            getMc().setScreen(screen);
        }
    }

    public static Screen getCurrentScreen() {
        return getMc() == null ? null : getMc().screen;
    }

    public static Font getFont() {
        return getMc() == null ? null : getMc().font;
    }

    public static List<Component> getItemToolTip(ItemStack item) {
        return Screen.getTooltipFromItem(getMc(), item);
    }
}
