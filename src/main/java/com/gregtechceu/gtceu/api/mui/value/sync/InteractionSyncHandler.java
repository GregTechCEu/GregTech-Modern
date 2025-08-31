package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.base.value.sync.IServerKeyboardAction;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IServerMouseAction;
import com.gregtechceu.gtceu.api.mui.utils.KeyboardData;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;

import net.minecraft.network.FriendlyByteBuf;

public class InteractionSyncHandler extends SyncHandler {

    private IServerMouseAction mousePressed;
    private IServerMouseAction mouseReleased;
    private IServerMouseAction mouseTapped;
    private IServerMouseAction mouseScroll;
    private IServerKeyboardAction keyPressed;
    private IServerKeyboardAction keyReleased;
    private IServerKeyboardAction keyTapped;

    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) {}

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) {
        if (id < 10) {
            MouseData mouseData = MouseData.readPacket(buf);
            switch (id) {
                case 1: {
                    if (this.mousePressed != null) {
                        this.mousePressed.onServerMouseAction(mouseData);
                    }
                    break;
                }
                case 2: {
                    if (this.mouseReleased != null) {
                        this.mouseReleased.onServerMouseAction(mouseData);
                    }
                    break;
                }
                case 3: {
                    if (this.mouseTapped != null) {
                        this.mouseTapped.onServerMouseAction(mouseData);
                    }
                    break;
                }
                case 4: {
                    if (this.mouseScroll != null) {
                        this.mouseScroll.onServerMouseAction(mouseData);
                    }
                    break;
                }
            }
        } else if (id > 10) {
            KeyboardData keyboardData = KeyboardData.readPacket(buf);
            switch (id) {
                case 11: {
                    if (this.keyPressed != null) {
                        this.keyPressed.onServerKeyboardAction(keyboardData);
                    }
                    break;
                }
                case 12: {
                    if (this.keyReleased != null) {
                        this.keyReleased.onServerKeyboardAction(keyboardData);
                    }
                    break;
                }
                case 13: {
                    if (this.keyTapped != null) {
                        this.keyTapped.onServerKeyboardAction(keyboardData);
                    }
                    break;
                }
            }
        }
    }

    public boolean onMousePressed(int button) {
        if (this.mousePressed == null) return false;
        MouseData mouseData = MouseData.create(button);
        this.mousePressed.onServerMouseAction(mouseData);
        syncToServer(1, mouseData::writeToPacket);
        return true;
    }

    public boolean onMouseReleased(int button) {
        if (this.mouseReleased == null) return false;
        MouseData mouseData = MouseData.create(button);
        this.mouseReleased.onServerMouseAction(mouseData);
        syncToServer(2, mouseData::writeToPacket);
        return true;
    }

    public boolean onMouseTapped(int button) {
        if (this.mouseTapped == null) return false;
        MouseData mouseData = MouseData.create(button);
        this.mouseTapped.onServerMouseAction(mouseData);
        syncToServer(3, mouseData::writeToPacket);
        return true;
    }

    public boolean onMouseScroll(int scroll) {
        if (this.mouseScroll == null) return false;
        MouseData mouseData = MouseData.create(scroll);
        this.mouseScroll.onServerMouseAction(mouseData);
        syncToServer(4, mouseData::writeToPacket);
        return true;
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.keyPressed == null) return false;
        KeyboardData keyboardData = KeyboardData.create(keyCode, scanCode, modifiers);
        this.keyPressed.onServerKeyboardAction(keyboardData);
        syncToServer(11, keyboardData::writeToPacket);
        return true;
    }

    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.keyReleased == null) return false;
        KeyboardData keyboardData = KeyboardData.create(keyCode, scanCode, modifiers);
        this.keyReleased.onServerKeyboardAction(keyboardData);
        syncToServer(12, keyboardData::writeToPacket);
        return true;
    }

    public boolean onKeyTapped(int keyCode, int scanCode, int modifiers) {
        if (this.keyTapped == null) return false;
        KeyboardData keyboardData = KeyboardData.create(keyCode, scanCode, modifiers);
        this.keyTapped.onServerKeyboardAction(keyboardData);
        syncToServer(13, keyboardData::writeToPacket);
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

    public InteractionSyncHandler setOnMouseScroll(IServerMouseAction mouseAction) {
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
