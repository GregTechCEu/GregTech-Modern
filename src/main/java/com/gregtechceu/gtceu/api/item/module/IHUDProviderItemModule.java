package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHUDProviderItemModule {

    @OnlyIn(Dist.CLIENT)
    default boolean shouldDrawHUD(AppliedItemModule module) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    void drawHUD(AppliedItemModule module, GuiGraphics graphics);

    @OnlyIn(Dist.CLIENT)
    static void tryDrawHUD(ItemStack stack, GuiGraphics graphics) {
        if (stack == null || stack.isEmpty()) return;
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem == null) return;
        for (AppliedItemModule module : modularItem.getAppliedModules()) {
            if (module.getModule() instanceof IHUDProviderItemModule hudProvider) {
                if (hudProvider.shouldDrawHUD(module)) hudProvider.drawHUD(module, graphics);
            }
        }
    }
}
