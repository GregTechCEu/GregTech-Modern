package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the properties for a specific pipe segment
 */
public abstract class PipeSegmentProperties {

    public PipeSegmentProperties() {}


    /**
     * Adds tooltips describing these properties to a block item.
     */
    public void appendBlockTooltips(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {}
}
