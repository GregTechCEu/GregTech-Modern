package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSettingsBuilder;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.common.data.GTItemModules;

import net.minecraft.network.chat.Component;
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
import org.jetbrains.annotations.Range;

import java.util.List;

public class EnergyShieldItemModule extends TieredItemModule {

    // spotless:off
    public static final Codec<EnergyShieldItemModule> CODEC = RecordCodecBuilder.create(instance -> tieredBaseCodec(instance).and(
            Codec.DOUBLE.fieldOf("energy_percentage").forGetter(EnergyShieldItemModule::getEnergyPercentage)
    ).apply(instance, EnergyShieldItemModule::new));
    //spotless:on

    @Getter
    @Range(from = 0, to = 1)
    private double energyPercentage = 0.75f;

    public EnergyShieldItemModule(boolean isEnabled, ItemStack moduleItem, int tier, double energyPercentage) {
        super(isEnabled, moduleItem, tier);
    }

    public EnergyShieldItemModule(ItemStack attachItem, int tier) {
        super(attachItem, tier);
    }

    @Override
    public ItemModuleType<EnergyShieldItemModule> type() {
        return GTItemModules.DAMAGE_BLOCK[getTier()];
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.damage_block", getEnergyPerHP());
    }

    public void setEnergyPercentage(double energyPercentage) {
        this.energyPercentage = energyPercentage;
        getModularItemStack().saveModuleData();
    }

    private long getEnergyPerHP() {
        float div = (getTier() - 1) / 4f + 1;
        return (long) (8192 / div);
    }

    private int getMaxDamageReduction() {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getAppliedTo());
        if (electricItem == null) return 0;
        return (int) (electricItem.getMaxCharge() * energyPercentage / getEnergyPerHP());
    }

    private int getDamageReduction() {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getAppliedTo());
        if (electricItem == null) return 0;
        return (int) (electricItem.getCharge() * energyPercentage / getEnergyPerHP());
    }

    @Override
    public float changeDamage(LivingEntity entity, float amount, DamageSource source) {
        long energyPerHP = getEnergyPerHP();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ||
                source.is(DamageTypeTags.IS_DROWNING) || source.is(DamageTypes.STARVE)) {
            return amount;
        }
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getAppliedTo());
        if (electricItem == null) {
            return amount;
        }
        float damageReduction = Math.min(getDamageReduction(), amount);
        damageReduction = Math.toIntExact(electricItem.discharge(
                Math.round(damageReduction) * energyPerHP,
                electricItem.getTier(),
                true, false, false) / energyPerHP);
        return Math.max(amount - damageReduction, 0);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.damage_block",
                GTValues.VNF[getTier()]));
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(PanelSyncManager psm, int id) {
        return super.getSettings(psm, id)
                .num(Text.lang("gtceu.module.gui.energy_limit"),
                        this::getEnergyPercentage,
                        this::setEnergyPercentage,
                        0, 1,
                        d -> "%.0f%%".formatted(d * 100))
                .progress(Text.lang("gtceu.module.gui.hp"),
                        () -> getDamageReduction() * 1d / getMaxDamageReduction(),
                        d -> "%d/%d HP".formatted((int) (d * getMaxDamageReduction()),
                                getMaxDamageReduction()));
    }
}
