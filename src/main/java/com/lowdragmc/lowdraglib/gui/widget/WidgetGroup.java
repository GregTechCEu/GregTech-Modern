package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WidgetGroup extends Widget implements IGhostIngredientTarget, IIngredientSlot {

    public final List<Widget> widgets = new ArrayList<>();
    private boolean dynamicSized;
    private Layout layout = Layout.NONE;
    private int layoutPadding;
    private boolean allowXEIIngredientOverMouse = true;

    public WidgetGroup() {
        this(0, 0, 0, 0);
    }

    public WidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public WidgetGroup(Position position) {
        this(position, Size.ZERO);
    }

    public WidgetGroup(Position position, Size size) {
        super(position, size);
    }

    public void initTemplate() {}

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public void setLayoutPadding(int layoutPadding) {
        this.layoutPadding = layoutPadding;
    }

    public void setDynamicSized(boolean dynamicSized) {
        this.dynamicSized = dynamicSized;
    }

    @Override
    public WidgetGroup setClientSideWidget() {
        super.setClientSideWidget();
        widgets.forEach(Widget::setClientSideWidget);
        return this;
    }

    public List<Widget> getContainedWidgets(boolean includeHidden) {
        List<Widget> result = new ArrayList<>();
        for (Widget widget : widgets) {
            if (includeHidden || widget.isVisible()) {
                result.add(widget);
                if (widget instanceof WidgetGroup group) {
                    result.addAll(group.getContainedWidgets(includeHidden));
                }
            }
        }
        return result;
    }

    @Override
    public Widget getHoverElement(double mouseX, double mouseY) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i).getHoverElement(mouseX, mouseY);
            if (widget != null) return widget;
        }
        return super.getHoverElement(mouseX, mouseY);
    }

    public Widget getFirstWidgetById(Pattern pattern) {
        return getContainedWidgets(true).stream()
                .filter(widget -> widget.getId() != null && pattern.matcher(widget.getId()).matches())
                .findFirst()
                .orElse(null);
    }

    public List<Widget> getWidgetsById(Pattern pattern) {
        return getContainedWidgets(true).stream()
                .filter(widget -> widget.getId() != null && pattern.matcher(widget.getId()).matches())
                .toList();
    }

    public Widget getFirstWidgetById(String id) {
        return getFirstWidgetById(Pattern.compile(Pattern.quote(id)));
    }

    public List<Widget> getWidgetsById(String id) {
        return getWidgetsById(Pattern.compile(Pattern.quote(id)));
    }

    public <T extends Widget> List<T> getWidgetsByType(Class<T> type) {
        return getContainedWidgets(true).stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    @Override
    public WidgetGroup setVisible(boolean visible) {
        super.setVisible(visible);
        widgets.forEach(widget -> widget.setVisible(visible));
        return this;
    }

    @Override
    public void setGui(ModularUI gui) {
        super.setGui(gui);
        widgets.forEach(widget -> widget.setGui(gui));
    }

    public boolean isChild(Widget widget) {
        return widgets.contains(widget);
    }

    public WidgetGroup addWidget(Widget widget) {
        widgets.add(widget);
        widget.setParent(this);
        widget.setGui(gui);
        asElement().addChild(widget.asElement());
        return this;
    }

    public WidgetGroup addWidgets(Widget... widgets) {
        for (Widget widget : widgets) {
            addWidget(widget);
        }
        return this;
    }

    public <T extends Widget> WidgetGroup addWidget(T widget, java.util.function.Consumer<T> consumer) {
        addWidget(widget);
        consumer.accept(widget);
        return this;
    }

    public WidgetGroup addWidget(int index, Widget widget) {
        widgets.add(index, widget);
        widget.setParent(this);
        widget.setGui(gui);
        asElement().addChildAt(widget.asElement(), index);
        return this;
    }

    public void addWidgetAnima(Widget widget, com.lowdragmc.lowdraglib.gui.animation.Transform transform) {
        addWidget(widget);
        widget.animation(transform);
    }

    public void removeWidgetAnima(Widget widget, com.lowdragmc.lowdraglib.gui.animation.Transform transform) {
        removeWidget(widget);
    }

    public void waitToRemoved(Widget widget) {
        removeWidget(widget);
    }

    public void waitToAdded(Widget widget) {
        addWidget(widget);
    }

    public int getAllWidgetSize() {
        return getContainedWidgets(true).size();
    }

    public void removeWidget(Widget widget) {
        widgets.remove(widget);
        asElement().removeChild(widget.asElement());
        widget.setParent(null);
    }

    public void clearAllWidgets() {
        widgets.clear();
        asElement().clearAllChildren();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        widgets.forEach(Widget::initWidget);
    }

    @Override
    public void writeInitialData(RegistryFriendlyByteBuf buffer) {
        widgets.forEach(widget -> widget.writeInitialData(buffer));
    }

    @Override
    public void readInitialData(RegistryFriendlyByteBuf buffer) {
        widgets.forEach(widget -> widget.readInitialData(buffer));
    }

    @Override
    public List<Target> getPhantomTargets(Object ingredient) {
        return List.of();
    }

    @Override
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        if (!allowXEIIngredientOverMouse) return null;
        for (Widget widget : widgets) {
            if (widget instanceof IIngredientSlot slot) {
                Object ingredient = slot.getXEIIngredientOverMouse(mouseX, mouseY);
                if (ingredient != null) return ingredient;
            }
        }
        return null;
    }

    @Override
    public void detectAndSendChanges() {
        widgets.forEach(Widget::detectAndSendChanges);
    }

    @Override
    public void updateScreen() {
        widgets.forEach(Widget::updateScreen);
    }

    @Override
    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks);
    }

    protected void drawWidgetsForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        widgets.stream().filter(Widget::isVisible)
                .forEach(widget -> widget.drawInForeground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }

    protected void drawWidgetsBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        widgets.stream().filter(Widget::isVisible)
                .forEach(widget -> widget.drawInBackground(graphics, mouseX, mouseY, partialTicks));
    }

    protected void onChildSizeUpdate(Widget child) {
        if (dynamicSized) {
            int width = 0;
            int height = 0;
            for (Widget widget : widgets) {
                width = Math.max(width, widget.getSelfPositionX() + widget.getSizeWidth());
                height = Math.max(height, widget.getSelfPositionY() + widget.getSizeHeight());
            }
            setSize(new Size(width + layoutPadding, height + layoutPadding));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            if (widgets.get(i).mouseClicked(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            if (widgets.get(i).mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            if (widgets.get(i).mouseReleased(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return widgets.stream().anyMatch(widget -> widget.keyPressed(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return widgets.stream().anyMatch(widget -> widget.keyReleased(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return widgets.stream().anyMatch(widget -> widget.charTyped(codePoint, modifiers));
    }

    public CompoundTag serializeInnerNBT(net.minecraft.core.HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    public void deserializeInnerNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag tag) {}

    public boolean isDynamicSized() {
        return dynamicSized;
    }

    public Layout getLayout() {
        return layout;
    }

    public int getLayoutPadding() {
        return layoutPadding;
    }

    public boolean isAllowXEIIngredientOverMouse() {
        return allowXEIIngredientOverMouse;
    }

    public void setAllowXEIIngredientOverMouse(boolean allowXEIIngredientOverMouse) {
        this.allowXEIIngredientOverMouse = allowXEIIngredientOverMouse;
    }

    @Override
    public List<Rect2i> getGuiExtraAreas(Rect2i guiRect, List<Rect2i> list) {
        for (Widget widget : widgets) {
            widget.getGuiExtraAreas(guiRect, list);
        }
        return list;
    }
}
