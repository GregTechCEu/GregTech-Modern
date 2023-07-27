package com.gregtechceu.gtceu.api.gui.compass;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author KilaBash
 * @date 2022/8/26
 * @implNote BookTabGroup
 */
public class CompassSectionWidget extends WidgetGroup {

    protected final CompassView compassView;
    protected final CompassSection section;
    protected float xOffset, yOffset;
    protected float scale = 1;
    protected double lastMouseX, lastMouseY;
    protected boolean isDragging = false;

    public CompassSectionWidget(CompassView compassView, CompassSection section) {
        super(0, 0, compassView.getSize().width - CompassView.LIST_WIDTH, compassView.getSize().height);
        this.setBackground(section.getBackgroundTexture().get());
        this.compassView = compassView;
        this.section = section;
        this.resetFitScale();
        addWidget(new ButtonWidget(10, 10, 20, 20,
                new GuiTextureGroup(GuiTextures.BUTTON, GuiTextures.PROGRESS_BAR_RECYCLER.getSubTexture(0,0, 1, 0.5)),
                cd -> resetFitScale()));
    }

    public void resetFitScale() {
        int minX, minY, maxX, maxY;
        minX = minY = Integer.MAX_VALUE;
        maxX = maxY = Integer.MIN_VALUE;
        for (CompassNode node : section.nodes.values()) {
            Position position = node.getPosition();
            minX = Math.min(minX, position.x);
            minY = Math.min(minY, position.y);
            maxX = Math.max(maxX, position.x);
            maxY = Math.max(maxY, position.y);
        }
        minX -= 20;
        minY -= 20;
        maxX += 40;
        maxY += 40;
        this.xOffset = minX;
        this.yOffset = minY;
        var scaleWidth = (float) getSize().width / (maxX - minX);
        var scaleHeight = (float) getSize().height / (maxY - minY);
        this.scale = Math.min(scaleWidth, scaleHeight);
        if (scale < 0.5f) {
            this.scale = 0.5f;
        }
        this.xOffset -= (getSize().width / scale - (maxX - minX)) / 2;
        this.yOffset -= (getSize().height / scale - (maxY - minY)) / 2;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        if (isMouseOverElement(mouseX, mouseY)) {
            isDragging = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        if (isMouseOverElement(mouseX, mouseY)) {
            int newMouseX = (int) ((mouseX - this.getPosition().x) / scale + xOffset);
            int newMouseY = (int) ((mouseY - this.getPosition().y) / scale + yOffset);
            for (CompassNode node : section.nodes.values()) {
                var nodePosition = node.getPosition();
                if (isMouseOver(nodePosition.x - 10, nodePosition.y - 10, 20, 20, newMouseX, newMouseY)) {
                    compassView.openNodeContent(node);
                    return true;
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            xOffset += (float) (lastMouseX - mouseX) / scale;
            yOffset += (float) (lastMouseY - mouseY) / scale;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY)) {
            var newScale = (float) Mth.clamp(scale + wheelDelta * 0.1f, 0.1f, 10f);
            if (newScale != scale) {
                xOffset += (float) (mouseX - this.getPosition().x) / scale - (float) (mouseX - this.getPosition().x) / newScale;
                yOffset += (float) (mouseY - this.getPosition().y) / scale - (float) (mouseY - this.getPosition().y) / newScale;
                scale = newScale;
            }
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInForeground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY)) {
            int newMouseX = (int) ((mouseX - this.getPosition().x) / scale + xOffset);
            int newMouseY = (int) ((mouseY - this.getPosition().y) / scale + yOffset);
            for (CompassNode node : section.nodes.values()) {
                var nodePosition = node.getPosition();
                if (!node.getTooltips().isEmpty() && isMouseOver(nodePosition.x - 10, nodePosition.y - 10, 20, 20, newMouseX, newMouseY)) {
                    gui.getModularUIGui().setHoverTooltip(node.getTooltips(), ItemStack.EMPTY, null, null);
                }
            }
        }
        super.drawInForeground(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInBackground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        drawBackgroundTexture(poseStack, mouseX, mouseY);
        RenderUtils.useScissor(poseStack, this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height, () -> {
            poseStack.pushPose();
            poseStack.translate(this.getPosition().x, this.getPosition().y, 0);
            poseStack.scale(scale, scale, 1);
            poseStack.translate(-xOffset, -yOffset, 0);
            int newMouseX = (int) ((mouseX - this.getPosition().x) / scale + xOffset);
            int newMouseY = (int) ((mouseY - this.getPosition().y) / scale + yOffset);
            // draw lines
            for (CompassNode node : section.nodes.values()) {
                drawChildLines(poseStack, node);
            }
            // draw nodes
            for (CompassNode node : section.nodes.values()) {
                drawNode(poseStack, newMouseX, newMouseY, node);
            }
            poseStack.popPose();
        });
        drawWidgetsBackground(poseStack, mouseX, mouseY, partialTicks);
    }

    @Environment(EnvType.CLIENT)
    protected void drawNode(PoseStack poseStack, int mouseX, int mouseY, CompassNode node) {
        // draw background
        var nodePosition = node.getPosition();
        boolean isHover = isMouseOver(nodePosition.x - 10, nodePosition.y - 10, 20, 20, mouseX, mouseY);
        var texture = isHover ? new ResourceBorderTexture("gtceu:textures/gui/widget/button.png", 32, 32, 2, 2).setColor(0xff337f7f) : GuiTextures.BUTTON;
        texture.draw(poseStack, mouseX, mouseY, nodePosition.x - 10, nodePosition.y - 10, 20, 20);
        node.getButtonTexture().draw(poseStack, mouseX, mouseY, nodePosition.x - 8, nodePosition.y - 8, 16, 16);
    }



    @Environment(EnvType.CLIENT)
    protected void drawChildLines(PoseStack poseStack, CompassNode node) {
        for (var childNode : section.childNodes.getOrDefault(node, new CompassNode[0])) {
            var from = new Vec2(node.getPosition().x, node.getPosition().y);
            var to = new Vec2(childNode.getPosition().x, childNode.getPosition().y);
            DrawerHelper.drawLines(poseStack, List.of(from, to), -1, ColorPattern.T_WHITE.color, 1.5f);
        }
    }

}
