package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

public class AirSupplierModule extends ItemModule {

    private static final long ENERGY_PER_USE = 128;

    public AirSupplierModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), (double) ENERGY_PER_USE / (double) 100);
    }

    @Override
    public void onArmorTick(ModuleContext moduleContext, LivingEntity entity) {
        if (!isEnabled(moduleContext) || !(entity instanceof Player player) || player.level().isClientSide) return;
        super.onArmorTick(moduleContext, entity);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return;
        supplyAir(electricItem, player, ENERGY_PER_USE);
    }

    public static void supplyAir(@NotNull IElectricItem item, Player player, long energyPerUse) {
        int air = player.getAirSupply();
        if (item.canUse(energyPerUse / 100) && air < 100) {
            player.setAirSupply(air + 200);
            item.discharge(energyPerUse / 100, item.getTier(), true, false, false);
        }
    }
}
