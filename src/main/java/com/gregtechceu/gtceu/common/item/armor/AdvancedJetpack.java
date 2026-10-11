package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.jetpack.IJetpackLogic.JetpackStats;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class AdvancedJetpack extends Jetpack {

    public AdvancedJetpack(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier);
    }

    @Override
    protected JetpackStats getJetpackStats() {
        return new JetpackStats(0.04, 0.48, 0.13, 0.15, 0.008, 1.49,
                getEnergyPerUse(), Mth.ceil(getEnergyPerUse() * 2.5),
                ParticleTypes.CLOUD);
    }

    @Override
    protected JetpackStats getHoverJetpackStats() {
        return new JetpackStats(0.04, 0.136, 0.13, 0.4, 0.008, 1.49,
                getEnergyPerUse(), Mth.ceil(getEnergyPerUse() * 2.5),
                ParticleTypes.CLOUD);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return GTCEu.id("textures/armor/advanced_jetpack.png");
    }
}
