package com.gregtechceu.gtceu.api.mui.overlay;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.client.mui.screen.ClientScreenHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.client.mui.screen.OpenScreenEvent;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.MinecraftForge;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ApiStatus.Internal
public class OverlayStack {

    private static final List<ModularScreen> overlay = new ArrayList<>();

    public static void foreach(Consumer<ModularScreen> function, boolean topToBottom) {
        if (topToBottom) {
            for (int i = overlay.size() - 1; i >= 0; i--) {
                function.accept(overlay.get(i));
            }
        } else {
            for (ModularScreen screen : overlay) {
                function.accept(screen);
            }
        }
    }

    public static boolean interact(Predicate<ModularScreen> function, boolean topToBottom) {
        if (topToBottom) {
            for (int i = overlay.size() - 1; i >= 0; i--) {
                // overlay.get(i).getContext().updateEventState();
                if (function.test(overlay.get(i))) {
                    return true;
                }
            }
        } else {
            for (ModularScreen screen : overlay) {
                // screen.getContext().updateEventState();
                if (function.test(screen)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        ModularScreen hovered = null;
        ModularScreen fallback = null;
        for (ModularScreen screen : overlay) {
            screen.getContext().setGraphics(graphics);
            screen.getContext().updateState(mouseX, mouseY, partialTicks);
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            screen.render(graphics, mouseX, mouseY, partialTicks);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            screen.drawForeground(graphics, partialTicks);
            if (screen.getContext().isHovered()) hovered = screen;
            fallback = screen;
        }
        ClientScreenHandler.drawDebugScreen(graphics, hovered, fallback);
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
    }

    public static void open(ModularScreen screen) {
        int i = overlay.indexOf(screen);
        if (i >= 0 && i < overlay.size() - 1) {
            overlay.remove(i);
        }
        overlay.add(screen);
        screen.onOpen();
    }

    public static void close(ModularScreen screen) {
        if (overlay.remove(screen)) {
            // TODO: Maybe not always dispose similar to normal screens
            screen.getPanelManager().closeAll();
            screen.getPanelManager().dispose();
        }
    }

    static void closeAll() {
        for (int i = overlay.size() - 1; i >= 0; i--) {
            ModularScreen screen = overlay.remove(i);
            screen.getPanelManager().closeAll();
            screen.getPanelManager().dispose();
        }
    }

    public static void onTick() {
        foreach(ModularScreen::onUpdate, true);
    }

    @Nullable
    public static IWidget getHoveredElement() {
        for (int i = overlay.size() - 1; i >= 0; i--) {
            ModularScreen screen = overlay.get(i);
            IWidget hovered = screen.getContext().getTopHovered();
            if (hovered == null) continue;
            return hovered;
        }
        return null;
    }

    public static boolean isHoveringOverlay() {
        return getHoveredElement() != null;
    }

    public static void onOpenScreen(Screen newScreen) {
        closeAll();
        if (newScreen != null) {

            OpenScreenEvent event = new OpenScreenEvent(newScreen);
            MinecraftForge.EVENT_BUS.post(event);
            for (ModularScreen overlay : event.getOverlays()) {
                overlay.constructOverlay(newScreen);
                open(overlay);
            }
            if (ConfigHolder.INSTANCE.dev.debugUI && newScreen instanceof IMuiScreen muiScreen) {
                ModularScreen overlay = new DebugOverlay(muiScreen);
                overlay.constructOverlay(newScreen);
                open(overlay);
            }
        }
    }
}
