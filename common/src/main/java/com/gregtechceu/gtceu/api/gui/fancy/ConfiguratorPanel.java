package com.gregtechceu.gtceu.api.gui.fancy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
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
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/6/27
 * @implNote ConfiguratorPanel
 */
public class ConfiguratorPanel extends WidgetGroup {

    @Getter
    protected List<Tab> tabs = new ArrayList<>();
    @Getter @Nullable
    protected Tab expanded;
    @Setter
    protected int border = 4;
    @Setter
    protected IGuiTexture texture = GuiTextures.BACKGROUND;

    public ConfiguratorPanel() {
        super(-(24 + 2), 2, 24, 0);
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
                    .duration(500)
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
        }
        expanded = null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected void drawWidgetsBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
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
    @Environment(EnvType.CLIENT)

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
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expanded != null && expanded.isVisible() && expanded.isActive() && expanded.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public class Tab extends WidgetGroup {
        private final IFancyConfigurator configurator;
        private final ButtonWidget button;
        @Nullable
        private final WidgetGroup view;
        // dragging
        private double lastDeltaX, lastDeltaY;
        private int dragOffsetX, dragOffsetY;
        private boolean isDragging;

        public Tab(IFancyConfigurator configurator) {
            super(0, tabs.size() * (getTabSize() + 2), getTabSize(), getTabSize());
            this.configurator = configurator;
            this.button = new ButtonWidget(0, 0, getTabSize(), getTabSize(), null, this::onClick);
            if (configurator instanceof IFancyConfiguratorButton) {
                this.view = null;
                this.addWidget(button);
            } else{
                var widget = configurator.createConfigurator();
                widget.setSelfPosition(new Position(border, getTabSize()));

                this.view = new WidgetGroup(0, 0, 0, 0) {
                    @Override
                    protected void onChildSizeUpdate(Widget child) {
                        super.onChildSizeUpdate(child);
                        if (widget == child) {
                            this.setSize(new Size(widget.getSize().width + border * 2, widget.getSize().height + getTabSize() + border));
                        }
                    }
                };

                this.view.setVisible(false);
                this.view.setActive(false);
                this.view.setSize(new Size(widget.getSize().width + border * 2, widget.getSize().height + getTabSize() + border));
                this.view.addWidget(widget);
                this.view.addWidget(new ImageWidget(border + 5, border, widget.getSize().width - getTabSize() - 5, getTabSize() - border,
                        new TextTexture(configurator.getTitle())
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
                            .duration(500)
                            .position(new Position(dragOffsetX + (- size.width + (tabs.size() > 1 ?  - 2 : getTabSize())), dragOffsetY))
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
            if (configurator instanceof IFancyConfiguratorButton fancyBUTTON) {
                fancyBUTTON.onClick(clickData);
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
                if (getParentPosition().x - size.width + (tabs.size() > 1 ?  - 2 : getTabSize()) < 0) {
                    this.dragOffsetX -= (view.getParentPosition().x - size.width + (tabs.size() > 1 ?  - 2 : getTabSize()));
                }
                if (getParentPosition().y + size.height > gui.getScreenHeight()) {
                    this.dragOffsetY -= view.getParentPosition().y + size.height - gui.getScreenHeight();
                }
            }
            animation(new Animation()
                    .duration(500)
                    .position(new Position(dragOffsetX - size.width + (tabs.size() > 1 ?  - 2 : getTabSize()), dragOffsetY))
                    .size(size)
                    .ease(Eases.EaseQuadOut)
                    .onFinish(() -> {
                        view.setVisible(true);
                        view.setActive(true);
                    }));
        }

        private void collapseTo(int x, int y) {
            if (view != null) {
                view.setVisible(false);
                view.setActive(false);
            }
            animation(new Animation()
                    .duration(500)
                    .position(new Position(x, y))
                    .size(new Size(getTabSize(), getTabSize()))
                    .ease(Eases.EaseQuadOut));
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            drawBackgroundTexture(graphics, mouseX, mouseY);
            var position = getPosition();
            var size = getSize();
            if (inAnimate()) {
                graphics.enableScissor(position.x + border - 1, position.y + border - 1, position.x + border - 1 + size.width - (border - 1) * 2, position.y + border - 1 + size.height - (border - 1) * 2);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                graphics.disableScissor();
            } else {
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
            }
            configurator.getIcon().draw(graphics, mouseX, mouseY, position.x + size.width - 20, position.y + 4, 16, 16);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            if (isMouseOver(getPosition().x + getSize().width - 20, getPosition().y + 4, 16, 16, mouseX, mouseY) && gui != null && gui.getModularUIGui() != null) {
                gui.getModularUIGui().setHoverTooltip(configurator.getTooltips(), ItemStack.EMPTY, null, null);
            }
        }

        @Override
        @Environment(EnvType.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.lastDeltaX = 0;
            this.lastDeltaY = 0;
            this.isDragging = false;
            if (expanded == this && isMouseOver(getPosition().x, getPosition().y, getSize().width - getTabSize(), getTabSize(), mouseX, mouseY)) {
                isDragging = true;
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button) || isMouseOverElement(mouseX, mouseY);
        }

        @Override
        @Environment(EnvType.CLIENT)
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
        @Environment(EnvType.CLIENT)
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            this.lastDeltaX = 0;
            this.lastDeltaY = 0;
            this.isDragging = false;
            return super.mouseReleased(mouseX, mouseY, button) || isMouseOverElement(mouseX, mouseY);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta) || isMouseOverElement(mouseX, mouseY);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public boolean mouseMoved(double mouseX, double mouseY) {
            return super.mouseMoved(mouseX, mouseY) || isMouseOverElement(mouseX, mouseY);
        }
    }
}
