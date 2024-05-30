package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Provides a drawable HUD for the item
 */
public interface IItemHUDProvider extends IItemComponent {

    /**
     * @return if the HUD needs to be drawn
     */
    @OnlyIn(Dist.CLIENT)
    default boolean shouldDrawHUD() {
        return true;
    }

    /**
     * Draws the HUD
     *
     * @param stack the ItemStack to retrieve information from
     */
    @OnlyIn(Dist.CLIENT)
    default void drawHUD(ItemStack stack, GuiGraphics guiGraphics) {}

    /**
     * Checks and draws the hud for a provider
     *
     * @param provider the provider whose hud to draw
     * @param stack    the stack the provider should use
     */
    @OnlyIn(Dist.CLIENT)
    static void tryDrawHud(@Nonnull IItemHUDProvider provider, @Nonnull ItemStack stack, GuiGraphics guiGraphics) {
        if (provider.shouldDrawHUD()) provider.drawHUD(stack, guiGraphics);
    }
}
