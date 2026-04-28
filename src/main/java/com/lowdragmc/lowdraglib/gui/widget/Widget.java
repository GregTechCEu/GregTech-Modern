package com.lowdragmc.lowdraglib.gui.widget;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.animation.Animation;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.WidgetUIAccess;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.layout.Align;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Rect;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.IGUIContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.platform.InputConstants;
import dev.vfyjxf.taffy.style.TaffyPosition;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Widget {

    protected Position selfPosition;
    protected Position parentPosition = Position.ORIGIN;
    protected Size size;
    protected ModularUI gui;
    protected WidgetGroup parent;
    protected WidgetUIAccess uiAccess;
    protected String id;
    protected Align align = Align.NONE;
    protected boolean visible = true;
    protected boolean active = true;
    protected boolean focus;
    protected boolean isClientSideWidget;
    protected boolean initialized;
    protected List<Component> tooltipTexts = new ArrayList<>();
    public IGuiTexture backgroundTexture = IGuiTexture.EMPTY;
    public IGuiTexture hoverTexture = IGuiTexture.EMPTY;
    public IGuiTexture overlay = IGuiTexture.EMPTY;
    protected Animation animation;
    private final UIElement element = new WidgetElement();

    public Widget(Position position, Size size) {
        this.selfPosition = position;
        this.size = size;
        recomputePosition();
    }

    public Widget(int x, int y, int width, int height) {
        this(new Position(x, y), new Size(width, height));
    }

    public UIElement asElement() {
        return element;
    }

    public Widget setClientSideWidget() {
        isClientSideWidget = true;
        return this;
    }

    public Widget setHoverTooltips(String... tooltips) {
        tooltipTexts = Arrays.stream(tooltips).map(Component::translatable)
                .collect(java.util.stream.Collectors.toList());
        element.style(style -> style.tooltips(tooltipTexts.toArray(Component[]::new)));
        return this;
    }

    public Widget setHoverTooltips(Component... tooltips) {
        tooltipTexts = new ArrayList<>(List.of(tooltips));
        element.style(style -> style.tooltips(tooltips));
        return this;
    }

    public Widget setHoverTooltips(List<Component> tooltips) {
        tooltipTexts = new ArrayList<>(tooltips);
        element.style(style -> style.tooltips(tooltipTexts.toArray(Component[]::new)));
        return this;
    }

    public Widget appendHoverTooltips(String... tooltips) {
        Arrays.stream(tooltips).map(Component::translatable).forEach(tooltipTexts::add);
        element.style(style -> style.tooltips(tooltipTexts.toArray(Component[]::new)));
        return this;
    }

    public Widget appendHoverTooltips(Component... tooltips) {
        tooltipTexts.addAll(List.of(tooltips));
        element.style(style -> style.tooltips(tooltipTexts.toArray(Component[]::new)));
        return this;
    }

    public Widget appendHoverTooltips(List<Component> tooltips) {
        tooltipTexts.addAll(tooltips);
        element.style(style -> style.tooltips(tooltipTexts.toArray(Component[]::new)));
        return this;
    }

    public Widget kjs$setHoverTooltips(Component... tooltips) {
        return setHoverTooltips(tooltips);
    }

    public Widget setBackground(IGuiTexture... textures) {
        backgroundTexture = textures.length == 0 ? IGuiTexture.EMPTY : new GuiTextureGroup(textures);
        element.style(style -> style.backgroundTexture(backgroundTexture));
        return this;
    }

    public Widget setDrawBackgroundWhenHover(boolean drawBackgroundWhenHover) {
        return this;
    }

    public Widget setHoverTexture(IGuiTexture... textures) {
        hoverTexture = textures.length == 0 ? IGuiTexture.EMPTY : new GuiTextureGroup(textures);
        return this;
    }

    public <T> Widget setDraggingProvider(Supplier<T> draggingProvider,
                                          BiFunction<T, Position, IGuiTexture> draggingRenderer) {
        return this;
    }

    public Widget setDraggingConsumer(Predicate<Object> canAccept, Consumer<Object> onAccept,
                                      Consumer<Object> onDragEnter, Consumer<Object> onDragLeave) {
        return this;
    }

    public void animation(Animation animation) {
        this.animation = animation.setWidget(this);
    }

    public boolean inAnimate() {
        return animation != null && !animation.isFinish();
    }

    public void setGui(ModularUI gui) {
        this.gui = gui;
    }

    public void setParentPosition(Position parentPosition) {
        this.parentPosition = parentPosition;
        recomputePosition();
    }

    public Widget setSelfPosition(Position selfPosition) {
        this.selfPosition = selfPosition;
        recomputePosition();
        return this;
    }

    public final void setSelfPosition(int x, int y) {
        setSelfPosition(new Position(x, y));
    }

    public final void setSelfPositionX(int x) {
        setSelfPosition(new Position(x, selfPosition.y));
    }

    public final void setSelfPositionY(int y) {
        setSelfPosition(new Position(selfPosition.x, y));
    }

    public Position addSelfPosition(int x, int y) {
        setSelfPosition(selfPosition.add(x, y));
        return selfPosition;
    }

    public final int getSelfPositionX() {
        return selfPosition.x;
    }

    public final int getSelfPositionY() {
        return selfPosition.y;
    }

    public void setSize(Size size) {
        this.size = size;
        recomputePosition();
        onSizeUpdate();
        if (parent != null) {
            parent.onChildSizeUpdate(this);
        }
    }

    public final void setSize(int width, int height) {
        setSize(new Size(width, height));
    }

    public final void setSizeWidth(int width) {
        setSize(new Size(width, size.height));
    }

    public final void setSizeHeight(int height) {
        setSize(new Size(size.width, height));
    }

    public final int getPositionX() {
        return getPosition().x;
    }

    public final int getPositionY() {
        return getPosition().y;
    }

    public final int getSizeWidth() {
        return size.width;
    }

    public final int getSizeHeight() {
        return size.height;
    }

    public Rect getRect() {
        return new Rect(getPosition(), size);
    }

    public Rect2i toRectangleBox() {
        return new Rect2i(getPositionX(), getPositionY(), size.width, size.height);
    }

    public boolean isMouseOverElement(double mouseX, double mouseY) {
        return isMouseOver(getPositionX(), getPositionY(), size.width, size.height, mouseX, mouseY);
    }

    public Widget getHoverElement(double mouseX, double mouseY) {
        return isMouseOverElement(mouseX, mouseY) ? this : null;
    }

    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public void initWidget() {
        initialized = true;
    }

    public void initTemplate() {}

    public void buildConfigurator(ConfiguratorGroup father) {}

    public void writeInitialData(RegistryFriendlyByteBuf buffer) {}

    public void readInitialData(RegistryFriendlyByteBuf buffer) {}

    public void detectAndSendChanges() {}

    public void updateScreen() {}

    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {}

    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawBackgroundTexture(graphics, mouseX, mouseY);
    }

    protected void drawBackgroundTexture(GuiGraphics graphics, int mouseX, int mouseY) {
        backgroundTexture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), size.width, size.height);
    }

    public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        overlay.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), size.width, size.height);
    }

    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta, double moveDelta) {
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    public boolean mouseMoved(double mouseX, double mouseY) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    public final void setFocus(boolean focus) {
        this.focus = focus;
    }

    public void onFocusChanged(Widget lastFocus, Widget focus) {}

    public void readUpdateInfo(int id, RegistryFriendlyByteBuf buffer) {}

    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {}

    public void writeUpdateInfo(int id, Consumer<RegistryFriendlyByteBuf> writer) {}

    public void writeClientAction(int id, Consumer<RegistryFriendlyByteBuf> writer) {}

    public static void playButtonClickSound() {}

    public static boolean isShiftDown() {
        return ScreenKeyState.isShiftDown();
    }

    public static boolean isCtrlDown() {
        return ScreenKeyState.isCtrlDown();
    }

    public static boolean isAltDown() {
        return ScreenKeyState.isAltDown();
    }

    public static boolean isKeyDown(int keyCode) {
        return ScreenKeyState.isKeyDown(keyCode);
    }

    public boolean isMouseDown(int button) {
        return Minecraft.getInstance().mouseHandler.isLeftPressed();
    }

    public boolean isRemote() {
        return gui != null && gui.holder != null && gui.holder.isRemote();
    }

    public boolean isParent(WidgetGroup parent) {
        return this.parent == parent;
    }

    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {}

    protected void onSizeUpdate() {}

    public List<Rect2i> getGuiExtraAreas(Rect2i guiRect, List<Rect2i> list) {
        return list;
    }

    public ModularUI getGui() {
        return gui;
    }

    public Widget setUiAccess(WidgetUIAccess uiAccess) {
        this.uiAccess = uiAccess;
        return this;
    }

    public Widget setId(String id) {
        this.id = id;
        element.setId(id);
        return this;
    }

    public String getId() {
        return id;
    }

    public Position getParentPosition() {
        return parentPosition;
    }

    public Position getSelfPosition() {
        return selfPosition;
    }

    public Position getPosition() {
        return parentPosition.add(selfPosition);
    }

    public Size getSize() {
        return size;
    }

    public Align getAlign() {
        return align;
    }

    public Widget setAlign(Align align) {
        this.align = align;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public Widget setVisible(boolean visible) {
        this.visible = visible;
        element.setVisible(visible);
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public Widget setActive(boolean active) {
        this.active = active;
        element.setActive(active);
        return this;
    }

    public boolean isFocus() {
        return focus;
    }

    public boolean isClientSideWidget() {
        return isClientSideWidget;
    }

    public List<Component> getTooltipTexts() {
        return tooltipTexts;
    }

    public IGuiTexture getBackgroundTexture() {
        return backgroundTexture;
    }

    public IGuiTexture getOverlay() {
        return overlay;
    }

    public Widget setOverlay(IGuiTexture overlay) {
        this.overlay = overlay;
        element.style(style -> style.overlayTexture(overlay));
        return this;
    }

    public WidgetGroup getParent() {
        return parent;
    }

    public Animation getAnimation() {
        return animation;
    }

    public boolean isInitialized() {
        return initialized;
    }

    protected void setParent(WidgetGroup parent) {
        this.parent = parent;
        setParentPosition(parent == null ? Position.ORIGIN : parent.getPosition());
    }

    public void recomputePosition() {
        element.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(selfPosition.x)
                .top(selfPosition.y)
                .width(size.width)
                .height(size.height));
    }

    private class WidgetElement extends UIElement {

        @Override
        protected void drawBackgroundAdditional(IGUIContext context) {
            if (context instanceof GUIContext guiContext && visible) {
                GuiGraphics graphics = GuiGraphics.from(guiContext.graphics, guiContext.getRenderState(),
                        guiContext.mouseX, guiContext.mouseY);
                Widget.this.drawInBackground(graphics, guiContext.mouseX, guiContext.mouseY, guiContext.partialTick);
                Widget.this.drawOverlay(graphics, guiContext.mouseX, guiContext.mouseY, guiContext.partialTick);
            }
        }
    }

    private static class ScreenKeyState {

        static boolean isShiftDown() {
            var window = Minecraft.getInstance().getWindow();
            return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                    InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        }

        static boolean isCtrlDown() {
            var window = Minecraft.getInstance().getWindow();
            return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                    InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
        }

        static boolean isAltDown() {
            var window = Minecraft.getInstance().getWindow();
            return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT) ||
                    InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
        }

        static boolean isKeyDown(int keyCode) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), keyCode);
        }
    }
}
