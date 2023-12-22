package com.gregtechceu.gtceu.api.item.component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Provides a drawable HUD for the item
 */
public interface IItemHUDProvider extends IItemComponent {

    /**
     * @return if the HUD needs to be drawn
     */
    @Environment(EnvType.CLIENT)
    default boolean shouldDrawHUD() {
        return true;
    }

    /**
     * Draws the HUD
     *
     * @param stack the ItemStack to retrieve information from
     */
    @Environment(EnvType.CLIENT)
    default void drawHUD(ItemStack stack) {/**/}

    /**
     * Checks and draws the hud for a provider
     *
     * @param provider the provider whose hud to draw
     * @param stack    the stack the provider should use
     */
    @Environment(EnvType.CLIENT)
    static void tryDrawHud(@Nonnull IItemHUDProvider provider, @Nonnull ItemStack stack) {
        if (provider.shouldDrawHUD()) provider.drawHUD(stack);
    }
}
