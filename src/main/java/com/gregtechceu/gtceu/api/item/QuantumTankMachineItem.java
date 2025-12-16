package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.misc.forge.QuantumFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.Nullable;

public class QuantumTankMachineItem extends MetaMachineItem {

    public QuantumTankMachineItem(IMachineBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (!QuantumTankMachine.TANK_CAPACITY.containsKey(getDefinition())) {
            GTCEu.LOGGER
                    .error("Quantum tank " + getDefinition().getName() + " does not have a registered TANK_CAPACITY," +
                            " will have capacity 0.");
        }
        return new QuantumFluidHandlerItemStack(stack, QuantumTankMachine.TANK_CAPACITY.getLong(getDefinition()));
    }
}
