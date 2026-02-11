package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

@Accessors(fluent = true, chain = true)
public class EntityDrawable<T extends Entity> implements IDrawable {

    @Getter
    protected final T entity;
    @Setter
    protected @Nullable BiConsumer<GuiGraphics, T> preDraw;
    @Setter
    protected @Nullable BiConsumer<GuiGraphics, T> postDraw;

    @Setter
    private boolean followMouse;
    private float lookTargetX = 0.0f;
    private float lookTargetY = 0.0f;

    public EntityDrawable(T entity) {
        this(entity, null, null);
    }

    public EntityDrawable(T entity, @Nullable BiConsumer<GuiGraphics, T> preDraw,
                          @Nullable BiConsumer<GuiGraphics, T> postDraw) {
        this.entity = entity;
        this.preDraw = preDraw;
        this.postDraw = postDraw;
    }

    public EntityDrawable<T> followMouse() {
        return followMouse(true);
    }

    public EntityDrawable<T> lookTowardAngle(float xAngle, float yAngle) {
        this.followMouse = false;
        this.lookTargetX = xAngle;
        this.lookTargetY = yAngle;

        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.followMouse) {
            GuiDraw.drawEntityLookingAtMouse(context.getGraphics(), this.entity, x, y, width, height,
                    context.getCurrentDrawingZ(), context.getMouseX(), context.getMouseY(),
                    this.preDraw, this.postDraw);
        } else {
            GuiDraw.drawEntityLookingAtAngle(context.getGraphics(), this.entity, x, y, width, height,
                    context.getCurrentDrawingZ(), this.lookTargetX, this.lookTargetY,
                    this.preDraw, this.postDraw);
        }
    }
}
