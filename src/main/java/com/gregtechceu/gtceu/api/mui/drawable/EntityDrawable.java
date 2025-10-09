package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.world.entity.LivingEntity;

public class EntityDrawable implements IDrawable {

    private LivingEntity entity;

    public EntityDrawable(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawLivingEntity(context.getGraphics(), this.entity, x, y, width, height, context.getCurrentDrawingZ());
    }
}
