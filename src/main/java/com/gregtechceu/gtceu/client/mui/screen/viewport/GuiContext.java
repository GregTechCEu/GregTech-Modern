package com.gregtechceu.gtceu.client.mui.screen.viewport;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiElement;
import com.gregtechceu.gtceu.api.mui.utils.Stencil;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.ClientScreenHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * A gui context contains various properties like screen size, mouse position, last clicked button etc.
 * It also is a matrix/pose stack.
 * A default instance can be obtained using {@link #getDefault()}, which can be used in {@link IDrawable IDrawables} for
 * example.
 * That instance is automatically updated at all times (except when no UI is currently open).
 */
public class GuiContext extends GuiViewportStack {

    public static GuiContext getDefault() {
        return ClientScreenHandler.getBestContext();
    }

    @Getter
    private final Area screenArea = new Area();
    @Getter
    @Setter(onMethod_ = @ApiStatus.Internal)
    private GuiGraphics graphics = null;
    private @Nullable Font overrideFont = null;
    @Getter
    private final Stencil stencil = new Stencil(this);

    /* Mouse states */
    private int mouseX;
    private int mouseY;
    @Getter
    private int mouseButton;
    @Getter
    private double mouseScrollDelta;

    /* Keyboard states */
    @Getter
    private int keyCode;
    @Getter
    private int scanCode;
    @Getter
    private int modifiers;
    @Getter
    private int codePoint;

    /* Render states */
    @Getter
    private float partialTicks;
    @Getter
    private long tick = 0;
    @Getter
    private int currentDrawingZ = 0;

    public boolean isAbove(IGuiElement widget) {
        return isMouseAbove(widget.getArea());
    }

    /**
     * @return true the mouse is anywhere above the widget
     */
    public boolean isMouseAbove(IGuiElement widget) {
        return isMouseAbove(widget.getArea());
    }

    /**
     * @return true the mouse is anywhere above the area
     */
    public boolean isMouseAbove(Area area) {
        return area.isInside(this.mouseX, this.mouseY);
    }

    @ApiStatus.Internal
    public void updateState(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTicks = partialTicks;
    }

    @ApiStatus.Internal
    public void updateMouseButton(int button) {
        this.mouseButton = button;
    }

    @ApiStatus.Internal
    public void updateMouseWheel(double scrollDelta) {
        this.mouseScrollDelta = scrollDelta;
    }

    @ApiStatus.Internal
    public void updateLatestKey(int keyCode, int scanCode, int modifiers) {
        this.keyCode = keyCode;
        this.scanCode = scanCode;
        this.modifiers = modifiers;
    }

    @ApiStatus.Internal
    public void updateLatestTypedChar(int codePoint, int modifiers) {
        this.codePoint = codePoint;
        this.modifiers = modifiers;
    }

    @ApiStatus.Internal
    public void updateScreenArea(int w, int h) {
        this.screenArea.set(0, 0, w, h);
        this.screenArea.rx = 0;
        this.screenArea.ry = 0;
    }

    public void updateZ(int z) {
        this.currentDrawingZ = z;
    }

    @OnlyIn(Dist.CLIENT)
    public Minecraft getMC() {
        return Minecraft.getInstance();
    }

    @OnlyIn(Dist.CLIENT)
    public Font getFont() {
        if (overrideFont != null) {
            return overrideFont;
        } else {
            return MCHelper.getFont();
        }
    }

    @ApiStatus.Internal
    @OnlyIn(Dist.CLIENT)
    public void setOverrideFont(@Nullable Font overrideFont) {
        this.overrideFont = overrideFont;
    }

    public void tick() {
        this.tick += 1;
    }

    /* Viewport */

    public Matrix4f getLastGraphicsPose() {
        if (graphics == null) return new Matrix4f();
        return graphics.pose().last().pose();
    }

    public PoseStack graphicsPose() {
        if (graphics == null) return new PoseStack();
        return graphics.pose();
    }

    public int getMouseX() {
        return unTransformX(this.mouseX, this.mouseY);
    }

    public int getMouseY() {
        return unTransformY(this.mouseX, this.mouseY);
    }

    /**
     * Get absolute X coordinate of the mouse without the
     * scrolling areas applied
     */
    public int getAbsMouseX() {
        return this.mouseX;
    }

    /**
     * Get absolute Y coordinate of the mouse without the
     * scrolling areas applied
     */
    public int getAbsMouseY() {
        return this.mouseY;
    }

    public int getMouse(GuiAxis axis) {
        return axis.isHorizontal() ? getMouseX() : getMouseY();
    }

    public int getAbsMouse(GuiAxis axis) {
        return axis.isHorizontal() ? getAbsMouseX() : getAbsMouseY();
    }

    public boolean isMuiContext() {
        return false;
    }

    public ModularGuiContext getMuiContext() {
        throw new UnsupportedOperationException("This is not a MuiContext");
    }
}
