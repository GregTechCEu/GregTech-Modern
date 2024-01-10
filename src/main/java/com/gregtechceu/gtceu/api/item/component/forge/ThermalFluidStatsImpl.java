package com.gregtechceu.gtceu.api.item.component.forge;

import com.gregtechceu.gtceu.api.item.component.ThermalFluidStats;
import com.gregtechceu.gtceu.api.misc.forge.SimpleThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2023/3/19
 * @implNote ThermalFluidStatsImpl
 */
public class ThermalFluidStatsImpl extends ThermalFluidStats implements IComponentCapability {
    protected ThermalFluidStatsImpl(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        super(capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof, allowPartialFill);
    }

    public static ThermalFluidStats create(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        return new ThermalFluidStatsImpl(capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof, allowPartialFill);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, LazyOptional.of(() -> {
                if (allowPartialFill) {
                    return new ThermalFluidHandlerItemStack(itemStack, capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof);
                }
                return new SimpleThermalFluidHandlerItemStack(itemStack, capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof);
            }));
        }
        return LazyOptional.empty();
    }
}
