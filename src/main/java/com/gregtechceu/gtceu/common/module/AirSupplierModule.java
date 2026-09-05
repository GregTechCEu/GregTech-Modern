package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.common.item.armor.AdvancedQuarkTechSuite;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class AirSupplierModule extends ItemModule {

    public AirSupplierModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("metaarmor.tooltip.breath");
    }

    @Override
    public void onArmorTick(LivingEntity entity, AppliedItemModule module) {
        super.onArmorTick(entity, module);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getAppliedTo());
        if (electricItem == null) return;
        AdvancedQuarkTechSuite.supplyAir(electricItem, (Player) entity, 128);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.breath"));
    }
}
