package com.gregtechceu.gtceu.common.mui.widgets;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.widgets.ButtonWidget;
import org.jetbrains.annotations.NotNull;

public class PopupPanel extends ModularPanel<PopupPanel> {

    private boolean deleteCachedPanel;

    public static final int DEFAULT_WIDTH = 176, DEFAULT_HEIGHT = 166;

    public static PopupPanel defaultPopupPanel(String name) {
        return new PopupPanel(name).size(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static PopupPanel createPopupPanel(String name, int width, int height) {
        return new PopupPanel(name).size(width, height);
    }

    private PopupPanel(@NotNull String name) {
        super(name);
        center();
        background(GTGuiTextures.BACKGROUND);
        child(ButtonWidget.panelCloseButton().top(5).right(5)
                .onMousePressed((mouseX, mouseY, button) -> {
                    if (button == 0 || button == 1) {
                        this.closeIfOpen();
                        return true;
                    }
                    return false;
                }));
    }

    @Override
    public void onClose() {
        super.onClose();
        if (deleteCachedPanel && isSynced() && getSyncHandler() instanceof IPanelHandler handler) {
            handler.deleteCachedPanel();
        }
    }

    public PopupPanel deleteCachedPanel(boolean deleteCachedPanel) {
        this.deleteCachedPanel = deleteCachedPanel;
        return this;
    }

    @Override
    public PopupPanel size(int w, int h) {
        super.size(w, h);
        return this;
    }

    @Override
    public PopupPanel size(int val) {
        super.size(val);
        return this;
    }
}
