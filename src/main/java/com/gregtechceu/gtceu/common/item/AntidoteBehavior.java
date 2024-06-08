package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines an antidote for a hazard (e.g. poisoning)
 *
 * @param types         the type of the hazard to remove
 * @param removePercent the time to remove from the chosen hazard, as a percentage of the current time [0, 100].
 *                      -1 for all.
 */
public record AntidoteBehavior(Set<MedicalCondition> types, int removePercent)
        implements IInteractionItem, IAddInformation {

    public AntidoteBehavior(int timeToRemove, MedicalCondition... types) {
        this(new HashSet<>(), timeToRemove);
        this.types.addAll(Arrays.asList(types));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack itemstack = IInteractionItem.super.finishUsingItem(stack, level, livingEntity);
        IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(livingEntity);
        if (tracker == null) {
            return itemstack;
        }
        for (var entry : tracker.getMedicalConditions().object2FloatEntrySet()) {
            MedicalCondition condition = entry.getKey();
            if (condition == null) {
                continue;
            }
            if (!this.types.contains(condition)) {
                continue;
            }
            if (removePercent == -1) {
                tracker.removeMedicalCondition(condition);
            } else {
                float time = entry.getFloatValue();
                float timeToRemove = time * (removePercent / 100.0f);
                if (timeToRemove > 0.05f * time) {
                    tracker.removeMedicalCondition(condition);
                    continue;
                }
                tracker.heal(condition, (int) timeToRemove);
            }
        }
        return itemstack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled) return;

        if (GTUtil.isShiftDown()) {
            tooltipComponents.add(Component.translatable("gtceu.medical_condition.antidote.description_shift"));
            for (var type : types) {
                tooltipComponents.add(Component.translatable("gtceu.medical_condition." + type.name));
            }
            if (removePercent == -1) {
                tooltipComponents
                        .add(Component.translatable("gtceu.medical_condition.antidote.description.effect_removed.all"));
            } else {
                tooltipComponents
                        .add(Component.translatable("gtceu.medical_condition.antidote.description.effect_removed",
                                removePercent));
            }
            return;
        }
        tooltipComponents.add(Component.translatable("gtceu.medical_condition.antidote.description"));
    }
}
