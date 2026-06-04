package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.pipenet.PipeSegmentProperties;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemPipeSegmentProperties extends PipeSegmentProperties {

    /**
     * Items will try to take the path with the lowest priority.
     */
    @Getter
    @Setter
    private int priority;

    /**
     * Transfer rate in stacks per sec
     */
    @Getter
    @Setter
    private float transferRate;

    public ItemPipeSegmentProperties(int priority, float transferRate) {
        this.priority = priority;
        this.transferRate = transferRate;
    }

    @Override
    public void appendBlockTooltips(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {

        if (getTransferRate() % 1 != 0) {
            tooltip.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate",
                    (int) ((getTransferRate() * 64) + 0.5)));
        } else {
            tooltip.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks",
                    (int) getTransferRate()));
        }

        tooltip.add(Component.translatable("gtceu.item_pipe.priority", getPriority()));

    }
}
