package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class GTGuis {

    public static final int DEFAULT_WIDTH = 176, DEFAULT_HEIGHT = 166;

    public static ModularPanel createPanel(String name, int width, int height) {
        return ModularPanel.defaultPanel(name, width, height);
    }

    public static ModularPanel createPanel(MetaMachine mte, int width, int height) {
        return createPanel(mte.getDefinition().getId().getPath(), width, height);
    }

    public static ModularPanel createPanel(CoverBehavior cover, int width, int height) {
        return createPanel(cover.coverDefinition.getId().getPath(), width, height);
    }

    public static ModularPanel createPanel(ItemStack stack, int width, int height) {
        return createPanel(stack.getDescriptionId(), width, height);
    }

    public static ModularPanel createPanel(String name) {
        return ModularPanel.defaultPanel(name, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static ModularPanel defaultPanel(MetaMachine machine) {
        return createPanel(machine.getDefinition().getId().getPath());
    }

    public static ModularPanel defaultPanel(CoverBehavior cover) {
        return createPanel(cover.coverDefinition.getId().getPath());
    }

    public static ModularPanel defaultPanel(ItemStack stack) {
        return createPanel(stack, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static ModularPanel defaultPanel(Item item) {
        return createPanel(item.getDescriptionId());
    }

    public static PopupPanel createPopupPanel(String name, int width, int height) {
        return defaultPopupPanel(name)
                .size(width, height);
    }

    public static PopupPanel createPopupPanel(String name, int width, int height, boolean deleteCachedPanel) {
        return createPopupPanel(name, width, height)
                .deleteCachedPanel(deleteCachedPanel);
    }

    public static PopupPanel defaultPopupPanel(String name) {
        return new PopupPanel(name)
                .size(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static PopupPanel defaultPopupPanel(String name, boolean disableBelow,
                                               boolean closeOnOutsideClick, boolean deleteCachedPanel) {
        return defaultPopupPanel(name)
                .disablePanelsBelow(disableBelow)
                .closeOnOutOfBoundsClick(closeOnOutsideClick)
                .deleteCachedPanel(deleteCachedPanel);
    }

    public static class PopupPanel extends ModularPanel {

        private boolean disableBelow;
        private boolean closeOnOutsideClick;
        private boolean deleteCachedPanel;

        private PopupPanel(@NotNull String name) {
            super(name);
            align(Alignment.Center);
            background(GTGuiTextures.BACKGROUND_POPUP);
            child(ButtonWidget.panelCloseButton().top(5).right(5)
                    .onMousePressed((mouseX, mouseY, button) -> {
                        if (button == 0 || button == 1) {
                            this.closeIfOpen(true);
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

        public PopupPanel disablePanelsBelow(boolean disableBelow) {
            this.disableBelow = disableBelow;
            return this;
        }

        public PopupPanel closeOnOutOfBoundsClick(boolean closeOnOutsideClick) {
            this.closeOnOutsideClick = closeOnOutsideClick;
            return this;
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

        @Override
        public boolean disablePanelsBelow() {
            return disableBelow;
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return closeOnOutsideClick;
        }
    }
}
