package com.gregtechceu.gtceu.api.item.fabric;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.capability.fabric.GTCapability;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IRecipeRemainder;
import com.gregtechceu.gtceu.api.item.component.ThermalFluidStats;
import com.gregtechceu.gtceu.api.misc.fabric.SimpleThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.misc.fabric.ThermalFluidHandlerItemStack;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ComponentItemImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentItemImpl extends ComponentItem implements FabricItem {
    protected ComponentItemImpl(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack itemStack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(itemStack);
            }
        }
        return super.getRecipeRemainder(itemStack);
    }

    public static ComponentItem create(Properties properties) {
        return new ComponentItemImpl(properties);
    }

    public static void onAttach(Item item, IItemComponent component) {
        if (component instanceof ThermalFluidStats thermalFluidStats) {
            FluidStorage.ITEM.registerForItems((itemStack, context) -> {
                if (thermalFluidStats.allowPartialFill) {
                    return new ThermalFluidHandlerItemStack(context, thermalFluidStats.capacity, thermalFluidStats.maxFluidTemperature, thermalFluidStats.gasProof, thermalFluidStats.acidProof, thermalFluidStats.cryoProof, thermalFluidStats.plasmaProof);
                }
                return new SimpleThermalFluidHandlerItemStack(context, item.asItem().getDefaultInstance(), thermalFluidStats.capacity, thermalFluidStats.maxFluidTemperature, thermalFluidStats.gasProof, thermalFluidStats.acidProof, thermalFluidStats.cryoProof, thermalFluidStats.plasmaProof);
            }, item);
        }
        if (component instanceof ElectricStats electricStats) {
            GTCapability.CAPABILITY_ELECTRIC_ITEM.registerForItems(((itemStack, context) -> new ElectricItem(itemStack, electricStats.maxCharge, electricStats.tier, electricStats.chargeable, electricStats.dischargeable)), item);
        }
    }
}
