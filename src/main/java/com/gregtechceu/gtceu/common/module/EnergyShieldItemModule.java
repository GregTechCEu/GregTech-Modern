package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public class EnergyShieldItemModule extends TieredItemModule {

    /**
     * A double from 0 to 1. Determines the max percentage of energy the shield will deplete.
     */
    private static final String PERCENTAGE_KEY = "usable_energy";

    public EnergyShieldItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public void onAttach(ModuleContext moduleContext) {
        super.onAttach(moduleContext);
        moduleContext.getData().getTag().putDouble(PERCENTAGE_KEY, 0.75f);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.damage_block", getEnergyPerHP());
    }

    private long getEnergyPerHP() {
        float div = (getTier() - 1) / 4f + 1;
        return (long) (8192 / div);
    }

    private int getMaxDamageReduction(ModuleContext moduleContext) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return 0;
        return (int) (electricItem.getMaxCharge() * moduleContext.getData().getTag().getDouble(PERCENTAGE_KEY) / getEnergyPerHP());
    }

    private int getDamageReduction(ModuleContext moduleContext) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return 0;
        return (int) (electricItem.getCharge() * moduleContext.getData().getTag().getDouble(PERCENTAGE_KEY) / getEnergyPerHP());
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
    public void appendHoverText(ModuleContext moduleContext, Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(moduleContext, level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.damage_block",
                GTValues.VNF[getTier()]));
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        return super.getSettings(moduleContext, psm, id)
                .num(Text.lang("gtceu.module.gui.energy_limit"),
                        () -> moduleContext.getData().getTag().getDouble(PERCENTAGE_KEY),
                        d -> moduleContext.getData().getTag().putDouble(PERCENTAGE_KEY, d),
                        0, 1,
                        d -> "%.0f%%".formatted(d * 100))
                .progress(Text.lang("gtceu.module.gui.hp"),
                        () -> getDamageReduction(moduleContext) * 1d / getMaxDamageReduction(moduleContext),
                        d -> "%d/%d HP".formatted((int) (d * getMaxDamageReduction(moduleContext)),
                                getMaxDamageReduction(moduleContext)));
    }
}
