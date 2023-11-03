package com.gregtechceu.gtceu.integration.ae2.util;

import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.util.IConfigurableSlot;
import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawGradientRect;

/**
 * @Author GlodBlock
 * @Description A configurable slot
 * @Date 2023/4/22-0:30
 */
public class AEConfigSlot extends Widget implements IGhostIngredientTarget {

    protected AEConfigWidget parentWidget;
    protected int index;
    protected final static int REMOVE_ID = 1000;
    protected final static int UPDATE_ID = 1001;
    protected final static int AMOUNT_CHANGE_ID = 1002;
    protected final static int PICK_UP_ID = 1003;
    protected boolean select = false;

    public AEConfigSlot(Position pos, Size size, AEConfigWidget widget, int index) {
        super(pos, size);
        this.parentWidget = widget;
        this.index = index;
    }

    @Override
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        if (slot.getConfig() == null && mouseOverConfig(mouseX, mouseY)) {
            List<Component> hoverStringList = new ArrayList<>();
            hoverStringList.add(Component.translatable("gtceu.gui.config_slot"));
            hoverStringList.add(Component.translatable("gtceu.gui.config_slot.set"));
            hoverStringList.add(Component.translatable("gtceu.gui.config_slot.scroll"));
            hoverStringList.add(Component.translatable("gtceu.gui.config_slot.remove"));
            graphics.renderTooltip(Minecraft.getInstance().font, hoverStringList, Optional.empty(), mouseX, mouseY);
        }
    }

    public void setSelect(boolean val) {
        this.select = val;
    }

    protected boolean mouseOverConfig(double mouseX, double mouseY) {
        Position position = getPosition();
        return isMouseOver(position.x, position.y, 18, 18, mouseX, mouseY);
    }

    protected boolean mouseOverStock(double mouseX, double mouseY) {
        Position position = getPosition();
        return isMouseOver(position.x, position.y + 18, 18, 18, mouseX, mouseY);
    }

    @Override
    public List<Target> getPhantomTargets(Object ingredient) {
        return Collections.emptyList();
    }

    @Environment(EnvType.CLIENT)
    public static void drawSelectionOverlay(GuiGraphics graphics, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        drawGradientRect(graphics, x, y, width, height, -2130706433, -2130706433);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
    }

}