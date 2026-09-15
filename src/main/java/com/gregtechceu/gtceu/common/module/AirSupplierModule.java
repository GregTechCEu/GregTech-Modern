package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.common.item.armor.AdvancedQuarkTechSuite;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;

import java.util.List;

public class AirSupplierModule extends ItemModule {

    public static final Codec<AirSupplierModule> CODEC = simpleCodec(AirSupplierModule::new);

    public AirSupplierModule(boolean isEnabled, ItemStack moduleItem) {
        super(isEnabled, moduleItem);
    }

    public AirSupplierModule(ItemStack moduleItem) {
        super(moduleItem);
    }

    @Override
    public ItemModuleType<AirSupplierModule> type() {
        return GTItemModules.AIR_SUPPLIER;
    }

    @Override
    public Component getInfo() {
        return Component.translatable("metaarmor.tooltip.breath");
    }

    @Override
    public void onArmorTick(LivingEntity entity) {
        super.onArmorTick(entity);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getAppliedTo());
        if (electricItem == null) return;
        AdvancedQuarkTechSuite.supplyAir(electricItem, (Player) entity, 128);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.breath"));
    }
}
