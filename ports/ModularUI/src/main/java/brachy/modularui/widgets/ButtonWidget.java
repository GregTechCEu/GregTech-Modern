package brachy.modularui.widgets;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.api.widget.IGuiAction;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.InteractionSyncHandler;
import brachy.modularui.widget.SingleChildWidget;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ButtonWidget<W extends ButtonWidget<W>> extends SingleChildWidget<W> implements Interactable {

    public static ButtonWidget<?> panelCloseButton() {
        ButtonWidget<?> buttonWidget = new ButtonWidget<>();
        return buttonWidget.widgetTheme(IThemeApi.CLOSE_BUTTON)
                .top(4).right(4)
                .overlay(GuiTextures.CROSS_TINY)
                .onMousePressed((context, button) -> {
                    if (button == 0 || button == 1) {
                        buttonWidget.getPanel().closeIfOpen();
                        return true;
                    }
                    return false;
                });
    }

    @Getter private boolean playClickSound = true;
    @Getter private Runnable clickSound;
    private IGuiAction.MousePressed mousePressed;
    private IGuiAction.MouseReleased mouseReleased;
    private IGuiAction.MousePressed mouseTapped;
    private IGuiAction.MouseScroll mouseScroll;
    private IGuiAction.KeyPressed keyPressed;
    private IGuiAction.KeyReleased keyReleased;
    private IGuiAction.KeyPressed keyTapped;

    private InteractionSyncHandler syncHandler;

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getButtonTheme();
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(InteractionSyncHandler.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castNullable(InteractionSyncHandler.class);
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
    public @NotNull Result onMousePressed(int button) {
        if (this.mousePressed != null && this.mousePressed.press(getContext(), button)) {
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
    public boolean onMouseReleased(int button) {
        return (this.mouseReleased != null && this.mouseReleased.release(getContext(), button)) ||
                (this.syncHandler != null && this.syncHandler.onMouseReleased(button));
    }

    @NotNull
    @Override
    public Result onMouseTapped(int button) {
        if (this.mouseTapped != null && this.mouseTapped.press(getContext(), button)) {
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
        if (this.keyPressed != null && this.keyPressed.press(getContext(), modifiers)) {
            return Result.SUCCESS;
        }
        if (this.syncHandler != null && this.syncHandler.onKeyPressed(keyCode, scanCode, modifiers)) {
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return (this.keyReleased != null && this.keyReleased.release(getContext(), keyCode, scanCode, modifiers)) ||
                (this.syncHandler != null && this.syncHandler.onKeyReleased(keyCode, scanCode, modifiers));
    }

    @NotNull
    @Override
    public Result onKeyTapped(int keyCode, int scanCode, int modifiers) {
        if (this.keyTapped != null && this.keyTapped.press(getContext(), modifiers)) {
            return Result.SUCCESS;
        }
        if (this.syncHandler != null && this.syncHandler.onKeyTapped(keyCode, scanCode, modifiers)) {
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY) {
        return (this.mouseScroll != null && this.mouseScroll.scroll(getContext(), scrollX, scrollY)) ||
                (this.syncHandler != null && this.syncHandler.onMouseScroll((int) scrollX, (int) scrollY));
    }

    @Override
    public @NotNull InteractionSyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised or not synced!");
        }
        return syncHandler;
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
        setSyncOrValue(ISyncOrValue.orEmpty(interactionSyncHandler));
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
