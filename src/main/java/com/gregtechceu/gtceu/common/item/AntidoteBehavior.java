package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
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
public record AntidoteBehavior(Set<HazardProperty.HazardTrigger> types, int removePercent)
        implements IInteractionItem, IAddInformation {

    public AntidoteBehavior(int timeToRemove, HazardProperty.HazardTrigger... types) {
        this(new HashSet<>(), timeToRemove);
        this.types.addAll(Arrays.asList(types));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        //TODO look at #1329
    }

    //TODO needs pretty much an entire rewrite
}
