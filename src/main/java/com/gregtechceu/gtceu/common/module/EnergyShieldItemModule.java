package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.ModuleData;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

public class EnergyShieldItemModule extends TieredItemModule {

    @Override
    public Codec<? extends ModuleData> moduleDataCodec() {
        return EnergyShieldModuleData.CODEC;
    }

    @Override
    public Class<? extends ModuleData> moduleDataClass() {
        return EnergyShieldModuleData.class;
    }

    @Override
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack moduleStack) {
        return new EnergyShieldModuleData(slot, module, moduleStack);
    }

    public EnergyShieldItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.damage_block.description", getEnergyPerHP());
    }

    private long getEnergyPerHP() {
        float div = (getTier() - 1) / 4f + 1;
        return (long) (8192 / div);
    }

    private int getMaxDamageReduction(ModuleContext moduleContext) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return 0;
        return (int) (electricItem.getMaxCharge() *
                moduleContext.getData(EnergyShieldModuleData.class).getEnergyPercent() / getEnergyPerHP());
    }

    private int getDamageReduction(ModuleContext moduleContext) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return 0;
        return (int) (electricItem.getCharge() *
                moduleContext.getData(EnergyShieldModuleData.class).getEnergyPercent() / getEnergyPerHP());
    }

    @Override
    public float changeDamage(ModuleContext moduleContext, LivingEntity entity, float amount, DamageSource source) {
        long energyPerHP = getEnergyPerHP();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ||
                source.is(DamageTypeTags.IS_DROWNING) || source.is(DamageTypes.STARVE)) {
            return amount;
        }
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) {
            return amount;
        }
        float damageReduction = Math.min(getDamageReduction(moduleContext), amount);
        damageReduction = Math.toIntExact(electricItem.discharge(
                Math.round(damageReduction) * energyPerHP,
                electricItem.getTier(),
                true, false, false) / energyPerHP);
        return Math.max(amount - damageReduction, 0);
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Level level, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, level, tooltips, isAdvanced);
        tooltips.add(Component.translatable("module.gtceu.damage_block",
                GTValues.VNF[getTier()]));
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        return super.getSettings(moduleContext, psm, id)
                .num(Text.lang("module.gtceu.gui.energy_limit"),
                        () -> moduleContext.getData(EnergyShieldModuleData.class).getEnergyPercent(),
                        d -> moduleContext
                                .setData(moduleContext.getData(EnergyShieldModuleData.class).withEnergyPercent(d)),
                        0, 1,
                        d -> "%.0f%%".formatted(d * 100))
                .progress(Text.lang("module.gtceu.gui.hp"),
                        () -> getDamageReduction(moduleContext) * 1d / getMaxDamageReduction(moduleContext),
                        d -> "%d/%d HP".formatted((int) (d * getMaxDamageReduction(moduleContext)),
                                getMaxDamageReduction(moduleContext)));
    }

    public static class EnergyShieldModuleData extends ModuleData {

        // spotless:off
        public static final Codec<EnergyShieldModuleData> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance).and(
                Codec.DOUBLE.fieldOf("energyPercent").forGetter(EnergyShieldModuleData::getEnergyPercent)
        ).apply(instance, EnergyShieldModuleData::new));
        //spotless:on

        /**
         * A double from 0 to 1. Determines the max percentage of energy the shield will deplete.
         */
        @Getter
        private double energyPercent = 0.75d;

        public EnergyShieldModuleData(int slot, ItemModule module, ItemStack moduleItem) {
            super(slot, module, moduleItem, true);
        }

        public EnergyShieldModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                      double energyPercent) {
            super(slot, module, moduleItem, enabled);
        }

        public EnergyShieldModuleData withEnergyPercent(double percent) {
            return new EnergyShieldModuleData(slot, module, moduleItem, enabled, percent);
        }

        @Override
        public ModuleData copy() {
            return new EnergyShieldModuleData(slot, module, moduleItem.copy(), enabled, energyPercent);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new EnergyShieldModuleData(slot, module, moduleItem, enabled, energyPercent);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EnergyShieldModuleData other)) return false;
            return super.equals(obj) && energyPercent == other.energyPercent;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, energyPercent);
        }
    }
}
