package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.IItemDecoratorComponent;
import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public abstract class CornerItemDecoratorBehavior implements IItemDecoratorComponent {

    protected enum Corner {

        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        ;

        /// @return {xStart, yStart, widthScaled, heightScaled}
        @ApiStatus.Internal
        public float[] getValues(int xBase, int yBase, float width, float height, float scale) {
            float widthScaled = width * scale;
            float heightScaled = height * scale;
            return switch (this) {
                case TOP_LEFT -> new float[] { xBase, yBase, widthScaled, heightScaled };
                case TOP_RIGHT -> new float[] { xBase + (width - (widthScaled)), yBase, widthScaled, heightScaled };
                case BOTTOM_LEFT -> new float[] { xBase, yBase + (height - (heightScaled)), widthScaled, heightScaled };
                case BOTTOM_RIGHT -> new float[] { xBase + (width - (widthScaled)), yBase + (height - (heightScaled)),
                        widthScaled,
                        heightScaled };
            };
        }
    }

    protected abstract @Nullable ItemStack getItemToRender(ItemStack containerItem);

    /**
     * Whether this renderer should work.
     *
     * @param shiftKeyPressed {@code true} when the Shift key is pressed
     * @return {@code true} to continue the rendering
     */
    protected boolean shouldRender(boolean shiftKeyPressed) {
        return true;
    }

    /**
     * The scaling of the item to be rendered, ranged from <code>(0.0, 1.0]</code>.
     */
    protected float getScaling() {
        return 0.75F;
    }

    protected Corner getCorner() {
        return Corner.BOTTOM_RIGHT;
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int x, int y) {
        if (!shouldRender(Screen.hasShiftDown())) return false;

        ItemStack itemToRender = getItemToRender(itemStack);
        if (itemToRender == null) return false;

        try {
            float scaling = getScaling();
            if (scaling <= 0F || scaling > 1F) {
                scaling = 1.0F;
            }
            Corner corner = getCorner();
            float[] v = corner.getValues(x, y, 16, 16, scaling);
            RenderUtil.renderItem(guiGraphics, itemToRender, (int) v[0], (int) v[1], v[2], v[3]);
        } catch (Exception e) {
            GTCEu.LOGGER.warn("Exception occurred while rendering the CornerItemDecorator for {}", itemToRender);
        }

        return true;
    }
}
