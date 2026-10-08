package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IHUDProviderItemModule {

    @OnlyIn(Dist.CLIENT)
    default boolean shouldDrawHUD(ModuleContext moduleContext) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    void drawHUD(ModuleContext moduleContext, ArmorUtils.ModularHUD hud, GuiGraphics graphics);

    @OnlyIn(Dist.CLIENT)
    static void tryDrawHUD(ItemStack stack, ArmorUtils.ModularHUD hud, GuiGraphics graphics) {
        if (stack.isEmpty()) return;
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem == null) return;
        for (ModuleContext module : modularItem.getAllModuleInstances()) {
            if (module.getModule() instanceof IHUDProviderItemModule hudProvider) {
                if (hudProvider.shouldDrawHUD(module)) hudProvider.drawHUD(module, hud, graphics);
                hud.newLine(CommonComponents.EMPTY);
            }
        }
    }
}
