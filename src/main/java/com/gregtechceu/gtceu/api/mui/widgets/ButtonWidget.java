package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiAction;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiTextures;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.value.sync.InteractionSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.SingleChildWidget;

import org.jetbrains.annotations.NotNull;

public class ButtonWidget<W extends ButtonWidget<W>> extends SingleChildWidget<W> implements Interactable {

    public static ButtonWidget<?> panelCloseButton() {
        ButtonWidget<?> buttonWidget = new ButtonWidget<>();
        return buttonWidget.overlay(GuiTextures.CROSS_TINY)
                .size(10).top(4).right(4)
                .onMousePressed((mouseX, mouseY, button) -> {
                    if (button == 0 || button == 1) {
                        buttonWidget.getPanel().closeIfOpen(true);
                        return true;
                    }
                    return false;
                });
    }

    private boolean playClickSound = true;
    private Runnable clickSound;
    private IGuiAction.MousePressed mousePressed;
    private IGuiAction.MouseReleased mouseReleased;
    private IGuiAction.MousePressed mouseTapped;
    private IGuiAction.MouseScroll mouseScroll;
    private IGuiAction.KeyPressed keyPressed;
    private IGuiAction.KeyReleased keyReleased;
    private IGuiAction.KeyPressed keyTapped;

    private InteractionSyncHandler syncHandler;

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, InteractionSyncHandler.class);
        return this.syncHandler != null;
    }

    @Override
    public WidgetTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getButtonTheme();
    }

    public void playClickSound() {
        if (this.playClickSound) {
            if (this.clickSound != null) {
                this.clickSound.run();
            } else {
                Interactable.playButtonClickSound();
            }
        }
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        if (this.mousePressed != null && this.mousePressed.press(mouseX, mouseY, button)) {
            playClickSound();
            return Result.SUCCESS;
        }
        if (this.syncHandler != null && this.syncHandler.onMousePressed(button)) {
            playClickSound();
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        return (this.mouseReleased != null && this.mouseReleased.release(mouseX, mouseY, button)) ||
                (this.syncHandler != null && this.syncHandler.onMouseReleased(button));
    }

    @NotNull
    @Override
    public Result onMouseTapped(double mouseX, double mouseY, int button) {
        if (this.mouseTapped != null && this.mouseTapped.press(mouseX, mouseY, button)) {
            playClickSound();
            return Result.SUCCESS;
        }
        if (this.syncHandler != null && this.syncHandler.onMouseTapped(button)) {
            playClickSound();
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public @NotNull Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.keyPressed != null && this.keyPressed.press(keyCode, scanCode, modifiers)) {
            return Result.SUCCESS;
        }
        if (this.syncHandler != null && this.syncHandler.onKeyPressed(keyCode, scanCode, modifiers)) {
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return (this.keyReleased != null && this.keyReleased.release(keyCode, scanCode, modifiers)) ||
                (this.syncHandler != null && this.syncHandler.onKeyReleased(keyCode, scanCode, modifiers));
    }

    @NotNull
    @Override
    public Result onKeyTapped(int keyCode, int scanCode, int modifiers) {
        if (this.keyTapped != null && this.keyTapped.press(keyCode, scanCode, modifiers)) {
            return Result.SUCCESS;
        }
        if (this.syncHandler != null && this.syncHandler.onKeyTapped(keyCode, scanCode, modifiers)) {
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        return (this.mouseScroll != null && this.mouseScroll.scroll(mouseX, mouseY, delta)) ||
                (this.syncHandler != null && this.syncHandler.onMouseScroll((int) delta));
    }

    public W onMousePressed(IGuiAction.MousePressed mousePressed) {
        this.mousePressed = mousePressed;
        return getThis();
    }

    public W onMouseReleased(IGuiAction.MouseReleased mouseReleased) {
        this.mouseReleased = mouseReleased;
        return getThis();
    }

    public W onMouseTapped(IGuiAction.MousePressed mouseTapped) {
        this.mouseTapped = mouseTapped;
        return getThis();
    }

    public W onMouseScrolled(IGuiAction.MouseScroll mouseScroll) {
        this.mouseScroll = mouseScroll;
        return getThis();
    }

    public W onKeyPressed(IGuiAction.KeyPressed keyPressed) {
        this.keyPressed = keyPressed;
        return getThis();
    }

    public W onKeyReleased(IGuiAction.KeyReleased keyReleased) {
        this.keyReleased = keyReleased;
        return getThis();
    }

    public W onKeyTapped(IGuiAction.KeyPressed keyTapped) {
        this.keyTapped = keyTapped;
        return getThis();
    }

    public W syncHandler(InteractionSyncHandler interactionSyncHandler) {
        this.syncHandler = interactionSyncHandler;
        setSyncHandler(interactionSyncHandler);
        return getThis();
    }

    public W playClickSound(boolean play) {
        this.playClickSound = play;
        return getThis();
    }

    public W clickSound(Runnable clickSound) {
        this.clickSound = clickSound;
        return getThis();
    }
}
