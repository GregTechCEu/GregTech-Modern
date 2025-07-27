package com.gregtechceu.gtceu.api.gui.fancy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.animation.Animation;
import com.lowdragmc.lowdraglib.gui.animation.Transform;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.lowdraglib.utils.interpolate.Eases;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfiguratorPanel extends WidgetGroup {

    @Getter
    protected List<Tab> tabs = new ArrayList<>();
    @Getter
    @Nullable
    protected Tab expanded;
    @Setter
    protected int border = 4;
    @Setter
    protected IGuiTexture texture = GuiTextures.BACKGROUND;

    public ConfiguratorPanel(int x, int y) {
        super(x, y, 24, 0);
    }

    public void clear() {
        clearAllWidgets();
        tabs.clear();
        expanded = null;
    }

    public int getTabSize() {
        return getSize().width;
    }

    public void attachConfigurators(IFancyConfigurator... fancyConfigurators) {
        for (IFancyConfigurator fancyConfigurator : fancyConfigurators) {
            var tab = new Tab(fancyConfigurator);
            tab.setBackground(texture);
            tabs.add(tab);
            addWidgetAnima(tab, new Transform()
                    .scale(0)
                    .duration(getAnimationTime())
                    .ease(Eases.EaseQuadOut));
        }
        setSize(new Size(getSize().width, Math.max(0, tabs.size() * (getTabSize() + 2) - 2)));
    }

    public void expandTab(Tab tab) {
        tab.expand();
        int i = 0;
        for (Tab otherTab : tabs) {
            if (otherTab != tab) {
                otherTab.collapseTo(0, i++ * (getTabSize() + 2));
            }
        }
        expanded = tab;
    }

    public void collapseTab() {
        if (expanded != null) {
            for (int i = 0; i < tabs.size(); i++) {
                tabs.get(i).collapseTo(0, i * (getTabSize() + 2));
            }
            if (expanded instanceof FloatingTab) {
                expanded.collapseTo(0, 0);
            }
        }
        expanded = null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawWidgetsBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        for (Widget widget : widgets) {
            if (widget.isVisible() && widget != expanded) {
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.enableBlend();
                if (widget.inAnimate()) {
                    widget.getAnimation().drawInBackground(graphics, mouseX, mouseY, partialTicks);
                } else {
                    widget.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                }
            }
        }
        if (expanded != null && expanded.isVisible()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 300);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            if (expanded.inAnimate()) {
                expanded.getAnimation().drawInBackground(graphics, mouseX, mouseY, partialTicks);
            } else {
                expanded.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            }
            graphics.pose().popPose();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)

    protected void drawWidgetsForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // remove previous tooltip
        if (isMouseOverElement(mouseX, mouseY)) {
            gui.getModularUIGui().setHoverTooltip(Collections.emptyList(), ItemStack.EMPTY, null, null);
        }
        for (Widget widget : widgets) {
            if (widget.isVisible() && widget != expanded) {
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.enableBlend();
                if (widget.inAnimate()) {
                    widget.getAnimation().drawInForeground(graphics, mouseX, mouseY, partialTicks);
                } else {
                    widget.drawInForeground(graphics, mouseX, mouseY, partialTicks);
                }
            }
        }
        if (expanded != null && expanded.isVisible()) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            if (expanded.inAnimate()) {
                expanded.getAnimation().drawInForeground(graphics, mouseX, mouseY, partialTicks);
            } else {
                expanded.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expanded != null && expanded.isVisible() && expanded.isActive() &&
                expanded.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public FloatingTab createFloatingTab(IFancyConfigurator configurator) {
        return new FloatingTab(configurator);
    }

    public class Tab extends WidgetGroup {

        protected final IFancyConfigurator configurator;
        protected final ButtonWidget button;
        @Nullable
        protected final WidgetGroup view;
        // dragging
        protected double lastDeltaX, lastDeltaY;
        protected int dragOffsetX, dragOffsetY;
        protected boolean isDragging;

        public Tab(IFancyConfigurator configurator) {
            super(0, tabs.size() * (getTabSize() + 2), getTabSize(), getTabSize());
            this.configurator = configurator;
            this.button = new ButtonWidget(0, 0, getTabSize(), getTabSize(), null, this::onClick) {

                @Override
                public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                    if (!(configurator instanceof IFancyCustomMouseWheelAction hasActions)) return false;
                    if (isMouseOverElement(mouseX, mouseY))
                        return hasActions.mouseWheelMove(this::writeClientAction, mouseX, mouseY, wheelDelta);
                    return false;
                }

                @Override
                public void handleClientAction(int id, FriendlyByteBuf buffer) {
                    if (configurator instanceof IFancyCustomClientActionHandler handler && id > 1)
                        handler.handleClientAction(id, buffer);
                    else
                        super.handleClientAction(id, buffer);
                }
            };
            if (configurator instanceof IFancyConfiguratorButton) {
                this.view = null;
                this.addWidget(button);
            } else {
                var widget = configurator.createConfigurator();
                widget.setSelfPosition(new Position(border, getTabSize()));

                this.view = new WidgetGroup(0, 0, 0, 0) {

                    @Override
                    protected void onChildSizeUpdate(Widget child) {
                        super.onChildSizeUpdate(child);
                        if (widget == child) {
                            this.setSize(new Size(widget.getSize().width + border * 2,
                                    widget.getSize().height + getTabSize() + border));
                        }
                    }
                };

                this.view.setVisible(false);
                this.view.setActive(false);
                this.view.setSize(new Size(widget.getSize().width + border * 2,
                        widget.getSize().height + button.getSize().height + border));
                this.view.addWidget(widget);
                this.view.addWidget(new ImageWidget(border + 5, border, widget.getSize().width - getTabSize() - 5,
                        getTabSize() - border,
                        new TextTexture(configurator.getTitle().getString())
                                .setType(TextTexture.TextType.LEFT_HIDE)
                                .setWidth(widget.getSize().width - getTabSize())));
                this.addWidget(button);
                this.addWidget(view);
            }
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            super.writeInitialData(buffer);
            configurator.writeInitialData(buffer);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            super.readInitialData(buffer);
            configurator.readInitialData(buffer);
        }

        @Override
        public void detectAndSendChanges() {
            super.detectAndSendChanges();
            configurator.detectAndSendChange((id, sender) -> writeUpdateInfo(0, buf -> {
                buf.writeVarInt(id);
                sender.accept(buf);
            }));
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == 0) {
                configurator.readUpdateInfo(buffer.readVarInt(), buffer);
            } else {
                super.readUpdateInfo(id, buffer);
            }
        }

        @Override
        protected void onChildSizeUpdate(Widget child) {
            if (this.view != null && this.view == child) {
                if (expanded == this) {
                    var size = view.getSize();
                    animation(new Animation()
                            .duration(getAnimationTime())
                            .position(new Position(dragOffsetX + (-size.width + (tabs.size() > 1 ? -2 : getTabSize())),
                                    dragOffsetY))
                            .size(size)
                            .ease(Eases.EaseQuadOut)
                            .onFinish(() -> {
                                view.setVisible(true);
                                view.setActive(true);
                            }));
                }
            }
        }

        private void onClick(ClickData clickData) {
            if (clickData.button == 2 && configurator instanceof IFancyCustomMiddleClickAction middleAction) {
                middleAction.onMiddleClick(this::writeClientAction);
            } else if (configurator instanceof IFancyConfiguratorButton fancyButton) {
                fancyButton.onClick(clickData);
            } else {
                if (expanded == this) {
                    collapseTab();
                } else {
                    expandTab(this);
                }
            }
        }

        @Override
        public void setSize(Size size) {
            super.setSize(size);
            button.setSelfPosition(new Position(size.width - getTabSize(), 0));
        }

        private void expand() {
            if (view == null) return;
            var size = view.getSize();
            this.dragOffsetX = 0;
            this.dragOffsetY = 0;
            if (isRemote()) {
                if (getParentPosition().x - size.width + (tabs.size() > 1 ? -2 : getTabSize()) < 0) {
                    this.dragOffsetX -= (view.getParentPosition().x - size.width +
                            (tabs.size() > 1 ? -2 : getTabSize()));
                }
                if (getParentPosition().y + size.height > gui.getScreenHeight()) {
                    this.dragOffsetY -= view.getParentPosition().y + size.height - gui.getScreenHeight();
                }
            }
            Position position = new Position(dragOffsetX - size.width + (tabs.size() > 1 ? -2 : getTabSize()),
                    dragOffsetY);

            animation(new Animation()
                    .duration(getAnimationTime())
                    .position(position)
                    .size(size)
                    .ease(Eases.EaseQuadOut)
                    .onFinish(() -> {
                        view.setVisible(true);
                        view.setActive(true);
                    }));
        }

        protected void collapseTo(int x, int y) {
            if (view != null) {
                view.setVisible(false);
                view.setActive(false);
            }
            animation(new Animation()
                    .duration(getAnimationTime())
                    .position(new Position(x, y))
                    .size(new Size(getTabSize(), getTabSize()))
                    .ease(Eases.EaseQuadOut));
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            drawBackgroundTexture(graphics, mouseX, mouseY);
            var position = getPosition();
            var size = getSize();
            if (inAnimate()) {
                graphics.enableScissor(position.x + border - 1, position.y + border - 1,
                        position.x + border - 1 + size.width - (border - 1) * 2,
                        position.y + border - 1 + size.height - (border - 1) * 2);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                graphics.disableScissor();
            } else {
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
            }
            configurator.getIcon().draw(graphics, mouseX, mouseY, position.x + size.width - 20, position.y + 4, 16, 16);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            if (isMouseOver(getPosition().x + getSize().width - 20, getPosition().y + 4, 16, 16, mouseX, mouseY) &&
                    gui != null && gui.getModularUIGui() != null) {
                gui.getModularUIGui().setHoverTooltip(configurator.getTooltips(), ItemStack.EMPTY, null, null);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.lastDeltaX = 0;
            this.lastDeltaY = 0;
            this.isDragging = false;
            if (expanded == this && isMouseOver(getPosition().x, getPosition().y, getSize().width - getTabSize(),
                    getTabSize(), mouseX, mouseY)) {
                isDragging = true;
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button) || isMouseOverElement(mouseX, mouseY);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            double dx = dragX + lastDeltaX;
            double dy = dragY + lastDeltaY;
            dragX = (int) dx;
            dragY = (int) dy;
            lastDeltaX = dx - dragX;
            lastDeltaY = dy - dragY;
            if (isDragging) {
                this.dragOffsetX += (int) dragX;
                this.dragOffsetY += (int) dragY;
                this.addSelfPosition((int) dragX, (int) dragY);
            }
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY) || isMouseOverElement(mouseX, mouseY);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            this.lastDeltaX = 0;
            this.lastDeltaY = 0;
            this.isDragging = false;
            return super.mouseReleased(mouseX, mouseY, button) || isMouseOverElement(mouseX, mouseY);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta) || isMouseOverElement(mouseX, mouseY);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseMoved(double mouseX, double mouseY) {
            return super.mouseMoved(mouseX, mouseY) || isMouseOverElement(mouseX, mouseY);
        }
    }

    public class FloatingTab extends Tab {

        protected Runnable closeCallback = () -> {};

        public FloatingTab(IFancyConfigurator configurator) {
            super(configurator);
            this.view.setBackground(GuiTextures.BACKGROUND);
        }

        @Override
        public void collapseTo(int x, int y) {
            super.collapseTo(x, y);
            ConfiguratorPanel.this.removeWidget(this);
            closeCallback.run();
        }

        public void onClose(Runnable closeCallback) {
            this.closeCallback = closeCallback;
        }
    }

    private static int getAnimationTime() {
        return ConfigHolder.INSTANCE.client.animationTime;
    }
}
