package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardEffectTracker;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.material.material.properties.HazardProperty;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines an antidote for a hazard (e.g. poisoning)
 * 
 * @param types        the type of the hazard to remove
 * @param timeToRemove the time to remove from the chosen hazard. -1 for all.
 */
public record AntidoteBehavior(Set<HazardProperty.HazardType> types, int timeToRemove)
        implements IInteractionItem, IAddInformation {

    public AntidoteBehavior(int timeToRemove, HazardProperty.HazardType... types) {
        this(Arrays.stream(types).collect(Collectors.toSet()), timeToRemove);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack itemstack = IInteractionItem.super.finishUsingItem(stack, level, livingEntity);
        IHazardEffectTracker tracker = GTCapabilityHelper.getHazardEffectTracker(livingEntity);
        if (tracker != null) {
            var effectTimes = tracker.getCurrentHazardEffects();
            var typesToEffects = tracker.getTypesToEffects();
            for (var type : this.types) {
                var effects = typesToEffects.getOrDefault(type, Set.of());
                for (HazardProperty.HazardEffect effect : effects) {
                    if (!effectTimes.containsKey(effect)) {
                        continue;
                    }
                    if (timeToRemove == -1) {
                        effectTimes.removeInt(effect);
                    } else {
                        int effectTime = effectTimes.getInt(effect);
                        effectTimes.put(effect, Math.max(0, effectTime - this.timeToRemove));
                        if (effectTimes.getInt(effect) == 0) {
                            effectTimes.removeInt(effect);
                        }
                    }
                }
            }
        }
        return itemstack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled) return;

        if (GTUtil.isShiftDown()) {
            tooltipComponents.add(Component.translatable("gtceu.hazard.antidote.description_shift"));
            for (var type : types) {
                tooltipComponents.add(Component
                        .translatable("gtceu.hazard." + type.name().toLowerCase()));
            }
            Component time = this.timeToRemove == -1 ?
                    Component.translatable("gtceu.hazard.antidote.description.time_removed.all") :
                    Component.literal(Integer.toString(this.timeToRemove / 20));
            tooltipComponents.add(Component.translatable("gtceu.hazard.antidote.description.time_removed", time));
            return;
        }
        tooltipComponents.add(Component.translatable("gtceu.hazard.antidote.description"));
    }
}
