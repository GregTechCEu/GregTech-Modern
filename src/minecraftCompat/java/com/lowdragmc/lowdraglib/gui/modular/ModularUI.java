package com.lowdragmc.lowdraglib.gui.modular;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.lowdraglib2.gui.ui.UI;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ModularUI {

    public final WidgetGroup mainGroup;
    public final IUIHolder holder;
    public final Player entityPlayer;
    private final com.lowdragmc.lowdraglib2.gui.ui.ModularUI delegate;
    private ModularUIContainer container;
    private ModularUIGuiContainer gui;
    private final List<Runnable> closeListeners = new ArrayList<>();

    public ModularUI(Size size, IUIHolder holder, Player entityPlayer) {
        this(new WidgetGroup(0, 0, size.width, size.height), holder, entityPlayer);
    }

    public ModularUI(int width, int height, IUIHolder holder, Player entityPlayer) {
        this(new Size(width, height), holder, entityPlayer);
    }

    public ModularUI(WidgetGroup mainGroup, IUIHolder holder, Player entityPlayer) {
        this.mainGroup = mainGroup;
        this.holder = holder;
        this.entityPlayer = entityPlayer;
        this.delegate = new com.lowdragmc.lowdraglib2.gui.ui.ModularUI(UI.of(mainGroup.asElement()), entityPlayer);
        this.mainGroup.setGui(this);
    }

    public ModularUI(IUIHolder holder, Player entityPlayer) {
        this(new WidgetGroup(0, 0, 176, 166), holder, entityPlayer);
    }

    public ModularUI setFullScreen() {
        return this;
    }

    public ModularUI widget(Widget widget) {
        mainGroup.addWidget(widget);
        return this;
    }

    public ModularUI background(IGuiTexture... textures) {
        mainGroup.setBackground(textures);
        return this;
    }

    public Widget getFirstWidgetById(String id) {
        return mainGroup.getFirstWidgetById(id);
    }

    public Widget getFirstWidgetById(Pattern pattern) {
        return mainGroup.getFirstWidgetById(pattern);
    }

    public <T extends Widget> List<T> getWidgetsByType(Class<T> type) {
        return mainGroup.getWidgetsByType(type);
    }

    public Collection<Widget> getFlatWidgetCollection() {
        return mainGroup.getContainedWidgets(true);
    }

    public Map<Object, SlotWidget> getSlotMap() {
        Map<Object, SlotWidget> slots = new LinkedHashMap<>();
        for (Widget widget : mainGroup.getContainedWidgets(true)) {
            if (widget instanceof SlotWidget slot && slot.getHandler() != null) {
                slots.put(slot.getHandler(), slot);
            }
        }
        return slots;
    }

    public void addCloseListener(Runnable listener) {
        if (listener != null) {
            closeListeners.add(listener);
        }
    }

    public void registerCloseListener(Runnable listener) {
        addCloseListener(listener);
    }

    public void setSize(Size size) {
        mainGroup.setSize(size);
    }

    public void setSize(int width, int height) {
        setSize(new Size(width, height));
    }

    public void updateScreenSize(int screenWidth, int screenHeight) {
        mainGroup.onScreenSizeUpdate(screenWidth, screenHeight);
    }

    public int getWidth() {
        return mainGroup.getSize().width;
    }

    public int getHeight() {
        return mainGroup.getSize().height;
    }

    public int getScreenWidth() {
        return delegate.getScreenWidth();
    }

    public int getScreenHeight() {
        return delegate.getScreenHeight();
    }

    public int getGuiLeft() {
        return (int) delegate.getLeftPos();
    }

    public int getGuiTop() {
        return (int) delegate.getTopPos();
    }

    public int getTickCount() {
        return entityPlayer == null ? 0 : entityPlayer.tickCount;
    }

    public Position toScreenCoords(Position position) {
        return position.add(getGuiLeft(), getGuiTop());
    }

    public ModularUIContainer getModularUIContainer() {
        if (container == null) {
            container = new ModularUIContainer(this, 0);
        }
        return container;
    }

    public ModularUIGuiContainer getModularUIGui() {
        return gui;
    }

    void setModularUIGui(ModularUIGuiContainer gui) {
        this.gui = gui;
    }

    public Screen getScreen() {
        return gui;
    }

    public com.lowdragmc.lowdraglib2.gui.ui.ModularUI toLDLib2() {
        return delegate;
    }

    public List<Widget> collectWidgets(Consumer<Widget> visitor) {
        List<Widget> widgets = new ArrayList<>(mainGroup.getContainedWidgets(true));
        widgets.forEach(visitor);
        return widgets;
    }

    public void notifyCloseListeners() {
        closeListeners.forEach(Runnable::run);
    }
}
