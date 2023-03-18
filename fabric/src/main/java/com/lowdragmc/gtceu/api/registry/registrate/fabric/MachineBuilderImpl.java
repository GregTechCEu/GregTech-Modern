package com.lowdragmc.gtceu.api.registry.registrate.fabric;

import com.lowdragmc.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.lowdragmc.gtceu.api.blockentity.MetaMachineBlockEntityImpl;
import com.lowdragmc.gtceu.api.capability.IControllable;
import com.lowdragmc.gtceu.api.capability.IEnergyContainer;
import com.lowdragmc.gtceu.api.capability.IWorkable;
import com.lowdragmc.gtceu.api.capability.fabric.GTCapability;
import com.lowdragmc.gtceu.api.machine.trait.MachineTrait;
import com.lowdragmc.gtceu.api.misc.EnergyContainerList;
import com.lowdragmc.gtceu.api.machine.trait.RecipeLogic;
import com.lowdragmc.lowdraglib.side.fluid.fabric.FluidTransferHelperImpl;
import com.lowdragmc.lowdraglib.side.item.fabric.ItemTransferHelperImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote MachineBuilderImpl
 */
public class MachineBuilderImpl {
    public static MetaMachineBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new MetaMachineBlockEntityImpl(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<MetaMachineBlockEntity> type) {
        GTCapability.CAPABILITY_COVERABLE.registerForBlockEntity((blockEntity, direction) -> blockEntity.getMetaMachine().getCoverContainer(), type);
        GTCapability.CAPABILITY_TOOLABLE.registerForBlockEntity((blockEntity, direction) -> blockEntity.getMetaMachine(), type);
        GTCapability.CAPABILITY_WORKABLE.registerForBlockEntity((blockEntity, direction) -> {
            if (blockEntity.getMetaMachine() instanceof IWorkable workable) {
                return workable;
            }
            for (MachineTrait trait : blockEntity.getMetaMachine().getTraits()) {
                if (trait instanceof IWorkable workable) {
                    return workable;
                }
            }
            return null;
        }, type);
        GTCapability.CAPABILITY_CONTROLLABLE.registerForBlockEntity((blockEntity, direction) -> {
            if (blockEntity.getMetaMachine() instanceof IControllable controllable) {
                return controllable;
            }
            for (MachineTrait trait : blockEntity.getMetaMachine().getTraits()) {
                if (trait instanceof IControllable controllable) {
                    return controllable;
                }
            }
            return null;
        }, type);
        GTCapability.CAPABILITY_RECIPE_LOGIC.registerForBlockEntity((blockEntity, direction) -> {
            for (MachineTrait trait : blockEntity.getMetaMachine().getTraits()) {
                if (trait instanceof RecipeLogic recipeLogic) {
                    return recipeLogic;
                }
            }
            return null;
        }, type);
        GTCapability.CAPABILITY_ENERGY.registerForBlockEntity((blockEntity, side) -> {
            if (blockEntity.getMetaMachine() instanceof IEnergyContainer energyContainer) {
                return  energyContainer;
            }
            var list = blockEntity.getMetaMachine().getTraits().stream().filter(IEnergyContainer.class::isInstance).filter(t -> t.hasCapability(side)).map(IEnergyContainer.class::cast).toList();
            return list.isEmpty() ? null : list.size() == 1 ? list.get(0) : new EnergyContainerList(list);
        }, type);
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, side) -> {
            var transfer = blockEntity.getMetaMachine().getItemTransferCap(side);
            return transfer == null ? null : ItemTransferHelperImpl.toItemVariantStorage(transfer);
        }, type);
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, side) -> {
            var transfer = blockEntity.getMetaMachine().getFluidTransferCap(side);
            return transfer == null ? null : FluidTransferHelperImpl.toFluidVariantStorage(transfer);
        }, type);
    }
}
