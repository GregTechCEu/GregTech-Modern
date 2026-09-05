package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.ArmorLogicItemModule;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

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
    protected @Nullable IArmorLogic getArmorLogic(AppliedItemModule module) {
        return JETPACK;
    }

    @Override
    public int getTier() {
        return GTValues.LV;
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(
                Component.translatable("metaarmor.tooltip.modifier.jetpack", module.getModuleItem().getHoverName()));
    }
}
