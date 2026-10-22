package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.module.ArmorLogicItemModule;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class LiquidFuelJetpackModule extends ArmorLogicItemModule implements ITieredItemModule {

    public LiquidFuelJetpackModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey());
    }

    @Override
    protected @Nullable IArmorLogic getArmorLogic(ModuleContext moduleContext) {
        return new PowerlessJetpack();
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        var modular = GTCapabilityHelper.getModularItem(stack);
        return super.canApplyTo(stack) && modular.getAllModuleInstances().stream()
                .noneMatch(context -> context.getModule() instanceof CreativeFlightModule ||
                        context.getModule() instanceof JetpackModule ||
                        context.getModule() instanceof LiquidFuelJetpackModule);
    }

    @Override
    public int getTier() {
        return GTValues.LV;
    }
}
