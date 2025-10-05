package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.client.mui.screen.XeiSettingsImpl;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

/**
 * Helper class to open client only GUIs. This class is safe to use inside a Modular GUI.
 * Direct calls to {@link net.minecraft.client.Minecraft#setScreen(Screen)} are redirected to this class if
 * the current screen is a Modular GUI.
 */
@OnlyIn(Dist.CLIENT)
public class ClientGUI {

    private ClientGUI() {}

    /**
     * Opens a modular screen on the next client tick with default jei settings.
     *
     * @param screen new modular screen
     */
    public static void open(@NotNull ModularScreen screen) {
        open(screen, new UISettings());
    }

    /**
     * Opens a modular screen on the next client tick with custom jei settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen      new modular screen
     * @param jeiSettings custom jei settings
     */
    public static void open(@NotNull ModularScreen screen, @NotNull XeiSettingsImpl jeiSettings) {
        GuiManager.openScreen(screen, new UISettings(jeiSettings));
    }

    /**
     * Opens a modular screen on the next client tick with custom jei settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen    new modular screen
     * @param container custom container
     */
    public static void open(@NotNull ModularScreen screen, @Nullable IntFunction<ModularContainerMenu> container) {
        UISettings settings = new UISettings();
        settings.customContainer(container);
        GuiManager.openScreen(screen, settings);
    }

    /**
     * Opens a modular screen on the next client tick with custom jei settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen      new modular screen
     * @param jeiSettings custom jei settings
     * @param container   custom container
     */
    public static void open(@NotNull ModularScreen screen, @NotNull XeiSettingsImpl jeiSettings,
                            @Nullable IntFunction<ModularContainerMenu> container) {
        UISettings settings = new UISettings(jeiSettings);
        settings.customContainer(container);
        GuiManager.openScreen(screen, settings);
    }

    /**
     * Opens a modular screen on the next client tick with custom jei settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen   new modular screen
     * @param settings ui settings
     */
    public static void open(@NotNull ModularScreen screen, @NotNull UISettings settings) {
        GuiManager.openScreen(screen, settings);
    }

    /**
     * Opens a {@link Screen} on the next client tick.
     *
     * @param screen screen to open
     */
    public static void open(Screen screen) {
        MCHelper.setScreen(screen);
    }

    /**
     * Closes any GUI that is open in this tick.
     */
    public static void close() {
        MCHelper.setScreen(null);
    }
}
