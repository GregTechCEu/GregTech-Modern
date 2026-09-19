package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.module.ArmorLogicItemModule;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LiquidFuelJetpackModule extends ArmorLogicItemModule implements ITieredItemModule {

    private static final PowerlessJetpack JETPACK = new PowerlessJetpack();

    public LiquidFuelJetpackModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.liquid_fuel_jetpack");
    }

    @Override
    protected @Nullable IArmorLogic getArmorLogic(ModuleContext moduleContext) {
        return JETPACK;
    }

    @Override
    public int getTier() {
        return GTValues.LV;
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Item.TooltipContext context, TooltipFlag isAdvanced,
                                List<Component> tooltips) {
        super.appendHoverText(moduleContext, context, isAdvanced, tooltips);
        tooltips.add(
                Component.translatable("metaarmor.tooltip.modifier.jetpack",
                        moduleContext.getModuleItem().getHoverName()));
    }
}
