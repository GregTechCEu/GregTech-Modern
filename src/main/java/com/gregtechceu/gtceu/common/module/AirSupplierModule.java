package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.common.item.armor.AdvancedQuarkTechSuite;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

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
    public void onArmorTick(ModuleContext moduleContext, LivingEntity entity) {
        super.onArmorTick(moduleContext, entity);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return;
        AdvancedQuarkTechSuite.supplyAir(electricItem, (Player) entity, 128);
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Item.TooltipContext context, TooltipFlag isAdvanced,
                                List<Component> tooltips) {
        super.appendHoverText(moduleContext, context, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.breath"));
    }
}
