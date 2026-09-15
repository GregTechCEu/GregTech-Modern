package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.module.ArmorLogicItemModule;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LiquidFuelJetpackModule extends ArmorLogicItemModule implements ITieredItemModule {

    private static final PowerlessJetpack JETPACK = new PowerlessJetpack();

    public static final Codec<LiquidFuelJetpackModule> CODEC = simpleCodec(LiquidFuelJetpackModule::new);

    public LiquidFuelJetpackModule(boolean isEnabled, ItemStack moduleItem) {
        super(isEnabled, moduleItem);
    }

    public LiquidFuelJetpackModule(ItemStack moduleItem) {
        super(moduleItem);
    }

    @Override
    public ItemModuleType<LiquidFuelJetpackModule> type() {
        return GTItemModules.LIQUID_FUEL_JETPACK;
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.liquid_fuel_jetpack");
    }

    @Override
    protected @Nullable IArmorLogic getArmorLogic() {
        return JETPACK;
    }

    @Override
    public int getTier() {
        return GTValues.LV;
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(
                Component.translatable("metaarmor.tooltip.modifier.jetpack", getModuleItem().getHoverName()));
    }
}
