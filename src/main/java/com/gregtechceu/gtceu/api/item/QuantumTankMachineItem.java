package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.misc.forge.QuantumTankFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class QuantumTankMachineItem extends MetaMachineItem {


    public QuantumTankMachineItem(IMachineBlock block, Properties properties) {
        super(block, properties);
    }

    public static QuantumTankMachineItem create(IMachineBlock block, Properties properties) {
        return new QuantumTankMachineItem(block, properties);
    }

    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        if(cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, LazyOptional.of(
                    () -> new QuantumTankFluidHandlerItemStack(itemStack, QuantumTankMachine.TANK_CAPACITY.getLong(getDefinition()))
            ));
        }
        return LazyOptional.empty();
    }
}
