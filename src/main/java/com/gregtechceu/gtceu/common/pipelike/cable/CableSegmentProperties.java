package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.pipenet.PipeSegmentProperties;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CableSegmentProperties extends PipeSegmentProperties {

    @Getter
    private long voltage;
    @Getter
    private int amperage;
    @Getter
    private int lossPerBlock;
    @Getter
    private final boolean isSuperconductor;

    public CableSegmentProperties(long voltage, int amperage, int lossPerBlock, boolean isSuperconductor) {
        this.voltage = voltage;
        this.amperage = amperage;
        this.lossPerBlock = lossPerBlock;
        this.isSuperconductor = isSuperconductor;
    }

    @Override
    public void appendBlockTooltips(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        int tier = GTUtil.getTierByVoltage(getVoltage());

        if (isSuperconductor)
            tooltip.add(Component.translatable("gtceu.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(Component.translatable("gtceu.cable.voltage",
                FormattingUtil.formatNumbers(getVoltage()), GTValues.VNF[tier]));
        tooltip.add(Component.translatable("gtceu.cable.amperage",
                FormattingUtil.formatNumbers(getAmperage())));
        tooltip.add(Component.translatable("gtceu.cable.loss_per_block",
                FormattingUtil.formatNumbers(getLossPerBlock())));
    }
}
