package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.capability.IPropertyFluidFilter;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.pipenet.PipeSegmentProperties;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;

public class FluidPipeSegmentProperties extends PipeSegmentProperties implements IPropertyFluidFilter {

    @Getter
    @Setter
    private int throughput;
    @Getter
    @Setter
    private int channels;
    @Getter
    @Setter
    private int maxFluidTemperature;
    @Getter
    @Setter
    private boolean gasProof;
    @Getter
    @Setter
    private boolean cryoProof;
    @Getter
    @Setter
    private boolean plasmaProof;

    private final Object2BooleanMap<FluidAttribute> containmentPredicate = new Object2BooleanOpenHashMap<>();

    public FluidPipeSegmentProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof, int channels, Object2BooleanMap<FluidAttribute> containmentPredicate) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        if (acidProof) setCanContain(FluidAttributes.ACID, true);
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.channels = channels;
    }

    @Override
    public void appendBlockTooltips(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", getThroughput()));
        tooltip.add(Component.translatable("gtceu.fluid_pipe.max_temperature",
                FormattingUtil.formatTemperature(getMaxFluidTemperature())));

        if (getChannels() > 1) {
            tooltip.add(Component.translatable("gtceu.fluid_pipe.channels", getChannels()));
        }

        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("gtceu.tooltip.fluid_pipe_hold_shift"));
            return;
        }

        if (isGasProof())
            tooltip.add(Component.translatable("gtceu.fluid_pipe.gas_proof"));
        else
            tooltip.add(Component.translatable("gtceu.fluid_pipe.not_gas_proof"));

        if (isAcidProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.acid_proof"));
        if (isCryoProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.cryo_proof"));
        if (isPlasmaProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.plasma_proof"));
    }

    @Override
    public boolean canContain(FluidState state) {
        return switch (state) {
            case LIQUID -> true;
            case GAS -> gasProof;
            case PLASMA -> plasmaProof;
        };
    }

    public boolean isAcidProof() {
        return canContain(FluidAttributes.ACID);
    }

    @Override
    public boolean canContain(FluidAttribute attribute) {
        return containmentPredicate.getBoolean(attribute);
    }

    @Override
    public void setCanContain(FluidAttribute attribute, boolean canContain) {
        containmentPredicate.put(attribute, canContain);
    }

    @Override
    public @UnmodifiableView Collection<FluidAttribute> getContainedAttributes() {
        return containmentPredicate.keySet();
    }
}
