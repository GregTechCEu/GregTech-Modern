package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHUDProviderItemModule {

    @OnlyIn(Dist.CLIENT)
    default boolean shouldDrawHUD(ModuleContext moduleContext) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    void drawHUD(ModuleContext moduleContext, GuiGraphics graphics);

    @OnlyIn(Dist.CLIENT)
    static void tryDrawHUD(ItemStack stack, GuiGraphics graphics) {
        if (stack.isEmpty()) return;
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem == null) return;
        for (ModuleContext module : modularItem.getAllModuleInstances()) {
            if (module.getModule() instanceof IHUDProviderItemModule hudProvider) {
                if (hudProvider.shouldDrawHUD(module)) hudProvider.drawHUD(module, graphics);
            }
        }
    }
}
