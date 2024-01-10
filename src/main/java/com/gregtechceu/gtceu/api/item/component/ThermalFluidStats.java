package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.misc.forge.SimpleThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ThermalFluidStats
 */
public class ThermalFluidStats implements IItemComponent, IComponentCapability {
    public final int capacity;
    public final int maxFluidTemperature;
    public final boolean gasProof;
    public final boolean acidProof;
    public final boolean cryoProof;
    public final boolean plasmaProof;
    public final boolean allowPartialFill;

    protected ThermalFluidStats(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        this.capacity = capacity;
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.allowPartialFill = allowPartialFill;
    }

    public static ThermalFluidStats create(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        return new ThermalFluidStats(capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof, allowPartialFill);
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
