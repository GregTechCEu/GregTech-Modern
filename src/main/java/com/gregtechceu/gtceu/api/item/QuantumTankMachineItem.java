package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.misc.forge.QuantumTankFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumTankMachineItem extends MetaMachineItem {

    public QuantumTankMachineItem(IMachineBlock block, Properties properties) {
        super(block, properties);
    }

    public static QuantumTankMachineItem create(IMachineBlock block, Properties properties) {
        return new QuantumTankMachineItem(block, properties);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack itemStack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                              @Nullable Direction direction) {
                if (capability == ForgeCapabilities.FLUID_HANDLER_ITEM) {
                    return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(capability, LazyOptional.of(
                            () -> new QuantumTankFluidHandlerItemStack(itemStack,
                                    QuantumTankMachine.TANK_CAPACITY.getLong(getDefinition()))));
                }
                return LazyOptional.empty();
            }
        };
    }
}
