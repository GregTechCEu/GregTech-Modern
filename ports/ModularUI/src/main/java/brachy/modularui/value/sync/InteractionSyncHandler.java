package brachy.modularui.value.sync;

import brachy.modularui.api.value.sync.IServerKeyboardAction;
import brachy.modularui.api.value.sync.IServerMouseAction;
import brachy.modularui.api.value.sync.IServerMouseScrollAction;
import brachy.modularui.utils.KeyboardData;
import brachy.modularui.utils.MouseData;

import net.minecraft.network.RegistryFriendlyByteBuf;

public class InteractionSyncHandler extends SyncHandler<InteractionSyncHandler> {

    private static final int MOUSE_PRESSED = 1;
    private static final int MOUSE_RELEASED = 2;
    private static final int MOUSE_TAPPED = 3;
    private static final int MOUSE_SCROLL = 4;

    private static final int KEY_ACTIONS = 10;
    private static final int KEY_PRESSED = 11;
    private static final int KEY_RELEASED = 12;
    private static final int KEY_TAPPED = 13;

    private IServerMouseAction mousePressed;
    private IServerMouseAction mouseReleased;
    private IServerMouseAction mouseTapped;
    private IServerMouseScrollAction mouseScroll;
    private IServerKeyboardAction keyPressed;
    private IServerKeyboardAction keyReleased;
    private IServerKeyboardAction keyTapped;

    public InteractionSyncHandler() {
        allowC2S();
    }

    @Override
    public void readOnClient(int id, RegistryFriendlyByteBuf buf) {}

    @Override
    public void readOnServer(int id, RegistryFriendlyByteBuf buf) {
        if (id < KEY_ACTIONS) {
            MouseData mouseData = MouseData.readPacket(buf);
            switch (id) {
                case MOUSE_PRESSED -> {
                    if (this.mousePressed != null) {
                        this.mousePressed.onServerMouseAction(mouseData);
                    }
                }
                case MOUSE_RELEASED -> {
                    if (this.mouseReleased != null) {
                        this.mouseReleased.onServerMouseAction(mouseData);
                    }
                }
                case MOUSE_TAPPED -> {
                    if (this.mouseTapped != null) {
                        this.mouseTapped.onServerMouseAction(mouseData);
                    }
                }
                case MOUSE_SCROLL -> {
                    if (this.mouseScroll != null) {
                        this.mouseScroll.onServerMouseAction(mouseData, buf.readDouble(), buf.readDouble());
                    }
                }
            }
        } else {
            KeyboardData keyboardData = KeyboardData.readPacket(buf);
            switch (id) {
                case KEY_PRESSED -> {
                    if (this.keyPressed != null) {
                        this.keyPressed.onServerKeyboardAction(keyboardData);
                    }
                }
                case KEY_RELEASED -> {
                    if (this.keyReleased != null) {
                        this.keyReleased.onServerKeyboardAction(keyboardData);
                    }
                }
                case KEY_TAPPED -> {
                    if (this.keyTapped != null) {
                        this.keyTapped.onServerKeyboardAction(keyboardData);
                    }
                }
            }
        }
    }

    public boolean onMousePressed(int button) {
        if (this.mousePressed == null) return false;
        MouseData mouseData = MouseData.create(button);
        this.mousePressed.onServerMouseAction(mouseData);
        syncToServer(MOUSE_PRESSED, mouseData::writeToPacket);
        return true;
    }

    public boolean onMouseReleased(int button) {
        if (this.mouseReleased == null) return false;
        MouseData mouseData = MouseData.create(button);
        this.mouseReleased.onServerMouseAction(mouseData);
        syncToServer(MOUSE_RELEASED, mouseData::writeToPacket);
        return true;
    }

    public boolean onMouseTapped(int button) {
        if (this.mouseTapped == null) return false;
        MouseData mouseData = MouseData.create(button);
        this.mouseTapped.onServerMouseAction(mouseData);
        syncToServer(MOUSE_TAPPED, mouseData::writeToPacket);
        return true;
    }

    public boolean onMouseScroll(double scrollX, double scrollY) {
        if (this.mouseScroll == null) return false;
        MouseData mouseData = MouseData.create((int) scrollY);
        this.mouseScroll.onServerMouseAction(mouseData, scrollX, scrollY);
        syncToServer(MOUSE_SCROLL, buf -> {
            mouseData.writeToPacket(buf);
            buf.writeDouble(scrollX);
            buf.writeDouble(scrollY);
        });
        return true;
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.keyPressed == null) return false;
        KeyboardData keyboardData = KeyboardData.create(keyCode, scanCode, modifiers);
        this.keyPressed.onServerKeyboardAction(keyboardData);
        syncToServer(KEY_PRESSED, keyboardData::writeToPacket);
        return true;
    }

    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.keyReleased == null) return false;
        KeyboardData keyboardData = KeyboardData.create(keyCode, scanCode, modifiers);
        this.keyReleased.onServerKeyboardAction(keyboardData);
        syncToServer(KEY_RELEASED, keyboardData::writeToPacket);
        return true;
    }

    public boolean onKeyTapped(int keyCode, int scanCode, int modifiers) {
        if (this.keyTapped == null) return false;
        KeyboardData keyboardData = KeyboardData.create(keyCode, scanCode, modifiers);
        this.keyTapped.onServerKeyboardAction(keyboardData);
        syncToServer(KEY_TAPPED, keyboardData::writeToPacket);
        return true;
    }

    public InteractionSyncHandler setOnMousePressed(IServerMouseAction mouseAction) {
        this.mousePressed = mouseAction;
        return this;
    }

    public InteractionSyncHandler setOnMouseReleased(IServerMouseAction mouseAction) {
        this.mouseReleased = mouseAction;
        return this;
    }

    public InteractionSyncHandler setOnMouseTapped(IServerMouseAction mouseAction) {
        this.mouseTapped = mouseAction;
        return this;
    }

    public InteractionSyncHandler setOnMouseScroll(IServerMouseScrollAction mouseAction) {
        this.mouseScroll = mouseAction;
        return this;
    }

    public InteractionSyncHandler setOnKeyPressed(IServerKeyboardAction keyboardAction) {
        this.keyPressed = keyboardAction;
        return this;
    }

    public InteractionSyncHandler setOnKeyReleased(IServerKeyboardAction keyboardAction) {
        this.keyReleased = keyboardAction;
        return this;
    }

    public InteractionSyncHandler setOnKeyTapped(IServerKeyboardAction keyboardAction) {
        this.keyTapped = keyboardAction;
        return this;
    }
}
