package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.api.pipenet.PipeSegmentProperties;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DuctSegmentProperties extends PipeSegmentProperties {

    /**
     * rate in stacks per sec
     */
    @Getter
    @Setter
    private float transferRate;

    public DuctSegmentProperties(float transferRate) {
        this.transferRate = transferRate;
    }


    @Override
    public void appendBlockTooltips(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gtceu.duct_pipe.transfer_rate",
                getTransferRate()));

    }
}
