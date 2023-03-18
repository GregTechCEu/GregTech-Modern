package com.lowdragmc.gtceu.api.item.forge;

import com.lowdragmc.gtceu.api.capability.forge.GTCapabilities;
import com.lowdragmc.gtceu.api.item.ComponentItem;
import com.lowdragmc.gtceu.api.item.capability.ElectricItem;
import com.lowdragmc.gtceu.api.item.component.ElectricStats;
import com.lowdragmc.gtceu.api.item.component.IItemComponent;
import com.lowdragmc.gtceu.api.item.component.IRecipeRemainder;
import com.lowdragmc.gtceu.api.item.component.ThermalFluidStats;
import com.lowdragmc.gtceu.api.misc.forge.SimpleThermalFluidHandlerItemStack;
import com.lowdragmc.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ComponentItemImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentItemImpl extends ComponentItem{
    protected int burnTime = -1;

    protected ComponentItemImpl(Properties properties) {
        super(properties);
    }

    public static ComponentItem create(Item.Properties properties) {
        return new ComponentItemImpl(properties);
    }

    public static void onAttach(Item item, IItemComponent components) {
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(itemStack);
            }
        }
        return super.getCraftingRemainingItem(itemStack);
    }

    public <T> LazyOptional<T> getCapability(@Nonnull final ItemStack itemStack, @Nonnull final Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            for (IItemComponent component : components) {
                if (component instanceof ThermalFluidStats fluidStats) {
                    return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, LazyOptional.of(() -> {
                        if (fluidStats.allowPartialFill) {
                            return new ThermalFluidHandlerItemStack(itemStack, fluidStats.capacity, fluidStats.maxFluidTemperature, fluidStats.gasProof, fluidStats.acidProof, fluidStats.cryoProof, fluidStats.plasmaProof);
                        }
                        return new SimpleThermalFluidHandlerItemStack(itemStack, fluidStats.capacity, fluidStats.maxFluidTemperature, fluidStats.gasProof, fluidStats.acidProof, fluidStats.cryoProof, fluidStats.plasmaProof);
                    }));
                }
                if (component instanceof ElectricStats electricStats) {
                    return GTCapabilities.CAPABILITY_ELECTRIC_ITEM.orEmpty(cap, LazyOptional.of(() -> new ElectricItem(itemStack, electricStats.maxCharge, electricStats.tier, electricStats.chargeable, electricStats.dischargeable)));
                }
            }
        } else if (cap == GTCapabilities.CAPABILITY_ELECTRIC_ITEM) {
            for (IItemComponent component : components) {
                if (component instanceof ElectricStats electricStats) {
                    return GTCapabilities.CAPABILITY_ELECTRIC_ITEM.orEmpty(cap, LazyOptional.of(() -> new ElectricItem(itemStack, electricStats.maxCharge, electricStats.tier, electricStats.chargeable, electricStats.dischargeable)));
                }
            }
        }
        return LazyOptional.empty();
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return burnTime;
    }

    public void burnTime(int burnTime) {
        this.burnTime = burnTime;
    }
}
