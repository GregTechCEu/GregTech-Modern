package com.gregtechceu.gtceu.api.gui.fancy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class TabsWidget extends Widget {

    protected final Consumer<IFancyUIProvider> onTabClick;
    protected IFancyUIProvider mainTab;
    protected List<IFancyUIProvider> subTabs;
    @Nullable
    protected IFancyUIProvider selectedTab;
    @Setter
    protected IGuiTexture leftButtonTexture = new GuiTextureGroup(GuiTextures.BUTTON, Icons.LEFT.copy().scale(0.7f)),
            leftButtonHoverTexture = new GuiTextureGroup(GuiTextures.BUTTON,
                    Icons.LEFT.copy().setColor(0xffaaaaaa).scale(0.7f));
    @Setter
    protected IGuiTexture rightButtonTexture = new GuiTextureGroup(GuiTextures.BUTTON, Icons.RIGHT.copy().scale(0.7f)),
            rightButtonHoverTexture = new GuiTextureGroup(GuiTextures.BUTTON,
                    Icons.RIGHT.copy().setColor(0xffaaaaaa).scale(0.7f));
    @Setter
    protected IGuiTexture tabTexture = new ResourceTexture("gtceu:textures/gui/tab/tabs_top.png").getSubTexture(1 / 3f,
            0, 1 / 3f, 0.5f);
    @Setter
    protected IGuiTexture tabHoverTexture = new ResourceTexture("gtceu:textures/gui/tab/tabs_top.png")
            .getSubTexture(1 / 3f, 0.5f, 1 / 3f, 0.5f);
    @Setter
    protected IGuiTexture tabPressedTexture = tabHoverTexture;
    @Getter
    protected int offset;
    /**
     * (old tab, new tab)
     */
    @Setter
    @Nullable
    protected BiConsumer<IFancyUIProvider, IFancyUIProvider> onTabSwitch;

    public TabsWidget(Consumer<IFancyUIProvider> onTabClick) {
        this(onTabClick, 0, -20, 200, 24);
    }

    public TabsWidget(Consumer<IFancyUIProvider> onTabClick, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.subTabs = new ArrayList<>();
        this.onTabClick = onTabClick;
    }

    public void setMainTab(IFancyUIProvider mainTab) {
        this.mainTab = mainTab;
        if (this.selectedTab == null) {
            this.selectedTab = this.mainTab;
        }
    }

    public void clearSubTabs() {
        subTabs.clear();
    }

    public void attachSubTab(IFancyUIProvider subTab) {
        subTabs.add(subTab);
    }

    public boolean hasButton() {
        return (subTabs.size() + 1) * 24 + 16 > getSize().width;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 0) {
            var index = buffer.readVarInt();
            var old = selectedTab;
            if (index < 0) {
                selectedTab = mainTab;
            } else if (index < subTabs.size()) {
                selectedTab = subTabs.get(index);
            } else {
                return;
            }
            if (onTabSwitch != null) {
                onTabSwitch.accept(old, selectedTab);
            }
            onTabClick.accept(selectedTab);
        }
    }

    public int getSubTabsWidth() {
        return getSize().width - 8 - 24 - 4 - 16 - 8 - 16;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            var hoveredTab = getHoveredTab(mouseX, mouseY);
            // click tab
            if (hoveredTab != null && hoveredTab != selectedTab) {
                if (onTabSwitch != null) {
                    onTabSwitch.accept(selectedTab, hoveredTab);
                }
                selectedTab = hoveredTab;
                writeClientAction(0,
                        buf -> buf.writeVarInt(selectedTab == mainTab ? -1 : subTabs.indexOf(selectedTab)));
                onTabClick.accept(selectedTab);
                playButtonClickSound();
            }
            // click button
            if (hasButton()) {
                if (isHoverLeftButton(mouseX, mouseY)) {
                    offset = Mth.clamp(offset - 24, 0, subTabs.size() * 24 - getSubTabsWidth());
                    playButtonClickSound();
                } else if (isHoverRightButton(mouseX, mouseY)) {
                    offset = Mth.clamp(offset + 24, 0, subTabs.size() * 24 - getSubTabsWidth());
                    playButtonClickSound();
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        var sx = getPosition().x + 8 + 24 + 4 + 16;
        if (isMouseOver(sx, getPosition().y, getSubTabsWidth(), 24, mouseX, mouseY)) {
            offset = Mth.clamp(offset + 5 * (wheelDelta > 0 ? -1 : 1), 0, subTabs.size() * 24 - getSubTabsWidth());
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        var position = getPosition();
        var size = getSize();
        var hoveredTab = getHoveredTab(mouseX, mouseY);
        // main tab
        drawTab(mainTab, graphics, mouseX, mouseY, position.x + 8, position.y, 24, 24, hoveredTab);
        // render sub tabs
        if (hasButton()) { // need a scrollable bar
            // render buttons
            if (isHoverLeftButton(mouseX, mouseY)) {
                leftButtonHoverTexture.draw(graphics, mouseX, mouseY, position.x + 8 + 24 + 4, position.y, 16, 24);
            } else {
                leftButtonTexture.draw(graphics, mouseX, mouseY, position.x + 8 + 24 + 4, position.y, 16, 24);
            }
            if (isHoverRightButton(mouseX, mouseY)) {
                rightButtonHoverTexture.draw(graphics, mouseX, mouseY, position.x + size.width - 8 - 16, position.y, 16,
                        24);
            } else {
                rightButtonTexture.draw(graphics, mouseX, mouseY, position.x + size.width - 8 - 16, position.y, 16, 24);
            }
            // render sub tabs
            var sx = position.x + 8 + 24 + 4 + 16;
            graphics.enableScissor(sx, position.y - 1, sx + getSubTabsWidth(), position.y - 1 + 24 + 2);
            for (int i = 0; i < subTabs.size(); i++) {
                drawTab(subTabs.get(i), graphics, mouseX, mouseY, sx + i * 24 - offset, position.y, 24, 24, hoveredTab);
            }
            graphics.disableScissor();
        } else {
            for (int i = subTabs.size() - 1; i >= 0; i--) {
                drawTab(subTabs.get(i), graphics, mouseX, mouseY,
                        position.x + size.width - 8 - 24 * (subTabs.size() - i), position.y, 24, 24, hoveredTab);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var hoveredTab = getHoveredTab(mouseX, mouseY);
        if (hoveredTab != null && gui != null && gui.getModularUIGui() != null) {
            gui.getModularUIGui().setHoverTooltip(hoveredTab.getTabTooltips(), ItemStack.EMPTY, null,
                    hoveredTab.getTabTooltipComponent());
        }
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isHoverLeftButton(double mouseX, double mouseY) {
        return isMouseOver(getPosition().x + 8 + 24 + 4, getPosition().y, 16, 24, mouseX, mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isHoverRightButton(double mouseX, double mouseY) {
        return isMouseOver(getPosition().x + getSize().width - 8 - 16, getPosition().y, 16, 24, mouseX, mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public IFancyUIProvider getHoveredTab(double mouseX, double mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            var position = getPosition();
            var size = getSize();
            // main tab
            if (isMouseOver(position.x + 8, position.y, 24, 24, mouseX, mouseY)) {
                return mainTab;
            }
            // others
            if (hasButton()) { // need a scrollable bar
                var sx = position.x + 8 + 24 + 4 + 16;
                if (isMouseOver(sx, position.y, getSubTabsWidth(), 24, mouseX, mouseY)) {
                    var i = ((int) mouseX - sx + getOffset()) / 24;
                    if (i < subTabs.size()) {
                        return subTabs.get(i);
                    }
                }
            } else {
                int i = (position.x + size.width - 8 - (int) mouseX) / 24;
                if (i < subTabs.size()) {
                    return subTabs.get(subTabs.size() - 1 - i);
                }
            }
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public void drawTab(IFancyUIProvider tab, @NotNull GuiGraphics graphics, int mouseX, int mouseY, int x, int y,
                        int width, int height, IFancyUIProvider hoveredTab) {
        // render background
        if (tab == selectedTab) {
            tabPressedTexture.draw(graphics, mouseX, mouseY, x, y, width, height);
        } else if (tab == hoveredTab) {
            tabHoverTexture.draw(graphics, mouseX, mouseY, x, y, width, height);
        } else {
            tabTexture.draw(graphics, mouseX, mouseY, x, y, width, height);
        }
        // render icon
        tab.getTabIcon().draw(graphics, mouseX, mouseY, x + (width - 16) / 2f, y + (height - 16) / 2f, 16, 16);
    }

    public void selectTab(IFancyUIProvider selectedTab) {
        this.selectedTab = selectedTab;
        this.detectAndSendChanges();
    }
}
