package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.misc.forge.FilteredFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.misc.forge.SimpleThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ThermalFluidStats
 */
public class ThermalFluidStats implements IItemComponent, IComponentCapability, IAddInformation {

    public final int capacity;
    public final int maxFluidTemperature;
    public final boolean gasProof;
    public final boolean acidProof;
    public final boolean cryoProof;
    public final boolean plasmaProof;
    public final boolean allowPartialFill;

    @Nullable
    public Predicate<FluidStack> filter;

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

    protected ThermalFluidStats(int capacity, boolean allowPartialFill, Predicate<FluidStack> filter) {
        this.allowPartialFill = allowPartialFill;
        this.capacity = capacity;
        this.filter = filter;

        this.maxFluidTemperature = 0;
        this.gasProof = false;
        this.acidProof = false;
        this.cryoProof = false;
        this.plasmaProof = false;
    }

    public static ThermalFluidStats create(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof,
                                           boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        return new ThermalFluidStats(capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof,
                allowPartialFill);
    }

    public static ThermalFluidStats create(int capacity, boolean allowPartialFill, Predicate<FluidStack> filter) {
        return new ThermalFluidStats(capacity, allowPartialFill, filter);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, LazyOptional.of(() -> {
                if (allowPartialFill) {
                    if (filter != null) {
                        return new FilteredFluidHandlerItemStack(itemStack, capacity, filter);
                    }
                    return new ThermalFluidHandlerItemStack(itemStack, capacity, maxFluidTemperature, gasProof,
                            acidProof, cryoProof, plasmaProof);
                }
                if (filter != null) {
                    return new FilteredFluidHandlerItemStack(itemStack, capacity, filter);
                }
                return new SimpleThermalFluidHandlerItemStack(itemStack, capacity, maxFluidTemperature, gasProof,
                        acidProof, cryoProof, plasmaProof);
            }));
        }
        return LazyOptional.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        if (stack.hasTag()) {
            FluidStack tank = FluidTransferHelper.getFluidContained(stack);
            if (tank != null) {
                tooltipComponents
                        .add(Component.translatable("gtceu.universal.tooltip.fluid_stored", tank.getDisplayName(),
                                tank.getAmount()));
            }
        }
    }
}
