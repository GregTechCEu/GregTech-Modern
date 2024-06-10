package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardEffectTracker;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
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
public record AntidoteBehavior(Set<HazardProperty.HazardType> types, int removePercent)
        implements IInteractionItem, IAddInformation {

    public AntidoteBehavior(int timeToRemove, HazardProperty.HazardType... types) {
        this(new HashSet<>(), timeToRemove);
        this.types.addAll(Arrays.asList(types));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack itemstack = IInteractionItem.super.finishUsingItem(stack, level, livingEntity);
        IHazardEffectTracker tracker = null;
        if (livingEntity instanceof Player player) {
            tracker = GTCapabilityHelper.getHazardEffectTracker(player);
        }
        if (tracker == null) {
            return itemstack;
        }
        var iterator = tracker.getCurrentHazards().object2IntEntrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getKey() == null) {
                continue;
            }
            HazardProperty.HazardType type = getHazardTypeFromMaterial(entry.getKey());
            if (type == null || !this.types.contains(type)) {
                continue;
            }
            if (removePercent == -1) {
                iterator.remove();
            } else {
                int time = entry.getIntValue();
                float timeToRemove = time * (removePercent / 100.0f);
                if (timeToRemove > 0.05f * time) {
                    iterator.remove();
                    continue;
                }
                entry.setValue((int) (time - timeToRemove));
            }
        }
        return itemstack;
    }

    @Nullable
    public static HazardProperty.HazardType getHazardTypeFromMaterial(@NotNull Material material) {
        HazardProperty property = material.getProperty(PropertyKey.HAZARD);
        if (property == null) {
            return null;
        }
        return property.getHazardType();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext context,
                                List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled) return;

        if (GTUtil.isShiftDown()) {
            tooltipComponents.add(Component.translatable("gtceu.hazard.antidote.description_shift"));
            for (var type : types) {
                tooltipComponents.add(Component
                        .translatable("gtceu.hazard." + type.getSerializedName()));
            }
            if (removePercent == -1) {
                tooltipComponents.add(Component.translatable("gtceu.hazard.antidote.description.effect_removed.all"));
            } else {
                tooltipComponents
                        .add(Component.translatable("gtceu.hazard.antidote.description.effect_removed", removePercent));
            }
            return;
        }
        tooltipComponents.add(Component.translatable("gtceu.hazard.antidote.description"));
    }
}
