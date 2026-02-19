package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.material.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.misc.forge.SimpleThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ThermalFluidStats implements IItemComponent, IComponentCapability, IAddInformation {

    public final int capacity;
    public final int maxFluidTemperature;
    public final boolean gasProof;
    public final boolean acidProof;
    public final boolean cryoProof;
    public final boolean plasmaProof;
    public final boolean allowPartialFill;

    protected ThermalFluidStats(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof,
                                boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        this.capacity = capacity;
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.allowPartialFill = allowPartialFill;
    }

    public static ThermalFluidStats create(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof,
                                           boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        return new ThermalFluidStats(capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof,
                allowPartialFill);
    }

    public static ThermalFluidStats create(int capacity, @NotNull FluidPipeProperties properties,
                                           boolean allowPartialFill) {
        return new ThermalFluidStats(capacity, properties.getMaxFluidTemperature(), properties.isGasProof(),
                properties.isAcidProof(), properties.isCryoProof(), properties.isPlasmaProof(), allowPartialFill);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, unused) -> {
            if (allowPartialFill) {
                return new ThermalFluidHandlerItemStack(stack, capacity, maxFluidTemperature, gasProof, acidProof,
                        cryoProof, plasmaProof);
            }
            return new SimpleThermalFluidHandlerItemStack(stack, capacity, maxFluidTemperature, gasProof, acidProof,
                    cryoProof, plasmaProof);
        }, item);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        FluidUtil.getFluidContained(stack).ifPresentOrElse(tank -> {
            tooltipComponents
                    .add(Component.translatable("gtceu.universal.tooltip.fluid_stored", tank.getHoverName(),
                            tank.getAmount()));
            TooltipsHandler.appendFluidTooltips(tank, tooltipComponents::add, isAdvanced, context);
        }, () -> {
            tooltipComponents.add(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                    FormattingUtil.formatNumbers(capacity)));
        });
        if (GTUtil.isShiftDown()) {
            tooltipComponents.add(Component.translatable("gtceu.fluid_pipe.max_temperature",
                    FormattingUtil.formatTemperature(maxFluidTemperature)));
            if (gasProof) tooltipComponents.add(Component.translatable("gtceu.fluid_pipe.gas_proof"));
            else tooltipComponents.add(Component.translatable("gtceu.fluid_pipe.not_gas_proof"));
            if (plasmaProof) tooltipComponents.add(Component.translatable("gtceu.fluid_pipe.plasma_proof"));
            if (cryoProof) tooltipComponents.add(Component.translatable("gtceu.fluid_pipe.cryo_proof"));
            if (acidProof) tooltipComponents.add(Component.translatable("gtceu.fluid_pipe.acid_proof"));
        } else if (gasProof || cryoProof || plasmaProof || acidProof) {
            tooltipComponents.add(Component.translatable("gtceu.tooltip.fluid_pipe_hold_shift"));
        }
    }
}
