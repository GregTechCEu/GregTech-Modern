package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.common.data.GTItemModules;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;

import java.util.List;

public class SensorItemModule extends TieredItemModule {

    // spotless:off
    public static final Codec<SensorItemModule> CODEC = tieredSimpleCodec(SensorItemModule::new);
    //spotless:on

    public SensorItemModule(boolean isEnabled, ItemStack moduleItem, int tier) {
        super(isEnabled, moduleItem, tier);
    }

    public SensorItemModule(ItemStack attachItem, int tier) {
        super(attachItem, tier);
    }

    @Override
    public ItemModuleType<SensorItemModule> type() {
        return GTItemModules.SENSOR[getTier()];
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.sensor");
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.sensor", GTValues.VNF[getTier()]));
    }
}
