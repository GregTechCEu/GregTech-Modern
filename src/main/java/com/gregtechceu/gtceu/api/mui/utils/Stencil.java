package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class Stencil {

    // Stores a stack of areas that are transformed, so it represents the actual area
    private static final ObjectArrayList<Area> stencils = new ObjectArrayList<>();
    // Stores a stack of stencilShapes which is used to mark and remove the stencil shape area
    private static final ObjectArrayList<Runnable> stencilShapes = new ObjectArrayList<>();
    // the current highest stencil value
    private static int stencilValue = 0;

    private final GuiContext context;

    @ApiStatus.Internal
    public Stencil(GuiContext context) {
        this.context = context;
    }

    /**
     * Resets all stencil values
     */
    public static void reset() {
        RenderSystem.assertOnRenderThread();
        stencils.clear();
        stencilShapes.clear();
        stencilValue = 0;
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0x00);
    }

    public void push(@NotNull Rectangle area) {
        push(area.x, area.y, area.width, area.height);
    }

    public void pushAtZero(@NotNull Rectangle area) {
        push(0, 0, area.width, area.height);
    }

    /**
     * Scissor a transformed part of the screen. OpenGL's transformations do effect these values. If the context is not
     * null, it's viewport
     * transformations are applied to the area that will be stored in the stack, but not to the actual stencil.
     */
    public void push(int x, int y, int w, int h) {
        push(() -> drawRectangleStencilShape(context.getGraphics(), x, y, w, h), x, y, w, h);
    }

    // should not be used inside GUI'S
    public void push(Runnable stencilShape, boolean hideStencilShape) {
        push(stencilShape, 0, 0, 0, 0, hideStencilShape);
    }

    public void push(Runnable stencilShape, int x, int y, int w, int h) {
        push(stencilShape, x, y, w, h, true);
    }

    public void push(Runnable stencilShape, int x, int y, int w, int h, boolean hideStencilShape) {
        RenderSystem.assertOnRenderThread();
        Area scissor = new Area(x, y, w, h);
        scissor.transformAndRectanglerize(this.context);
        if (!stencils.isEmpty()) {
            stencils.top().clamp(scissor);
        }
        applyShape(stencilShape, hideStencilShape);
        stencils.add(scissor);
        stencilShapes.add(stencilShape);
    }

    private void applyShape(Runnable stencilShape, boolean hideStencilShape) {
        // TODO: figure out exactly what this does, why its needed and why it sometimes causes issues
        this.context.getGraphics().flush();
        // increase stencil values in the area
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        setStencilValue(stencilShape, stencilValue, false, hideStencilShape);
        stencilValue++;
        RenderSystem.stencilFunc(GL11.GL_LEQUAL, stencilValue, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0x00);
    }

    private void setStencilValue(Runnable stencilShape, int stencilValue, boolean remove, boolean hideStencilShape) {
        // Set stencil func
        int mode = remove ? GL11.GL_DECR : GL11.GL_INCR;
        RenderSystem.stencilFunc(GL11.GL_EQUAL, stencilValue, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, mode, mode);
        RenderSystem.stencilMask(0xFF);

        if (hideStencilShape) {
            // disable colors and depth
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.depthMask(false);
        }
        stencilShape.run();
        if (hideStencilShape) {
            // Re-enable drawing to color buffer + depth buffer
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
        }
    }

    private static void drawRectangleStencilShape(GuiGraphics graphics, int x, int y, int w, int h) {
        RenderSystem.assertOnRenderThread();
        ShaderInstance lastShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        Matrix4f pose = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        float x0 = x, x1 = x + w, y0 = y, y1 = y + h;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(pose, x0, y0, 0.0f).endVertex();
        bufferbuilder.vertex(pose, x0, y1, 0.0f).endVertex();
        bufferbuilder.vertex(pose, x1, y1, 0.0f).endVertex();
        bufferbuilder.vertex(pose, x1, y0, 0.0f).endVertex();
        tesselator.end();
        RenderSystem.setShader(() -> lastShader);
    }

    /**
     * Removes the top most stencil
     */
    public void pop() {
        RenderSystem.assertOnRenderThread();
        if (stencils.isEmpty()) {
            throw new IllegalStateException("Tried to pop an empty stencil stack!");
        }
        stencils.pop();
        Runnable stencilShape = stencilShapes.pop();
        if (stencils.isEmpty()) {
            reset();
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            this.context.getGraphics().flush();
            return;
        }
        setStencilValue(stencilShape, stencilValue, true, true);
        stencilValue--;
        RenderSystem.stencilFunc(GL11.GL_LEQUAL, stencilValue, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0x00);
    }

    public static boolean isInsideScissorArea(Area area, IViewportStack stack) {
        if (stencils.isEmpty()) return true;
        Area.SHARED.set(0, 0, area.width, area.height);
        Area.SHARED.transformAndRectanglerize(stack);
        return stencils.top().intersects(Area.SHARED);
    }
}
