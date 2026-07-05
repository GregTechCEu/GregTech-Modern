package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the properties for a specific pipe segment.<br>
 * Pipe segments may have persisted data.
 */
public abstract class PipeSegmentProperties implements ISyncManaged {

    public PipeSegmentProperties() {}


    /**
     * Adds tooltips describing these properties to a block item.
     */
    public void appendBlockTooltips(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {}
}
