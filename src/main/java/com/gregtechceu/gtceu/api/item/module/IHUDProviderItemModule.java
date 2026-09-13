package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHUDProviderItemModule {

    @OnlyIn(Dist.CLIENT)
    default boolean shouldDrawHUD() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    void drawHUD(GuiGraphics graphics);

    @OnlyIn(Dist.CLIENT)
    static void tryDrawHUD(ItemStack stack, GuiGraphics graphics) {
        if (stack == null || stack.isEmpty()) return;
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem == null) return;
        for (ItemModule module : modularItem.getModules()) {
            if (module instanceof IHUDProviderItemModule hudProvider) {
                if (hudProvider.shouldDrawHUD()) hudProvider.drawHUD(graphics);
            }
        }
    }
}
