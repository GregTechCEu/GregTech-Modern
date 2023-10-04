package com.gregtechceu.gtceu.api.blockentity.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.capability.fabric.GTCapability;
import com.gregtechceu.gtceu.api.capability.fabric.GTEnergyHelperImpl;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.pipenet.longdistance.ILDEndpoint;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance.LDFluidEndpointMachine;
import com.gregtechceu.gtceu.common.pipelike.item.longdistance.LDItemEndpointMachine;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.fluid.fabric.FluidTransferHelperImpl;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.fabric.ItemTransferHelperImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote MetaMachineBlockEntity
 */
public class MetaMachineBlockEntityImpl extends MetaMachineBlockEntity {

    public MetaMachineBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static MetaMachineBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new MetaMachineBlockEntityImpl(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<BlockEntity> type) {
        GTCapability.CAPABILITY_COVERABLE.registerForBlockEntity((blockEntity, direction) -> ((IMachineBlockEntity)blockEntity).getMetaMachine().getCoverContainer(), type);
        GTCapability.CAPABILITY_TOOLABLE.registerForBlockEntity((blockEntity, direction) -> ((IMachineBlockEntity)blockEntity).getMetaMachine(), type);
        GTCapability.CAPABILITY_WORKABLE.registerForBlockEntity((blockEntity, direction) -> {
            if (((IMachineBlockEntity)blockEntity).getMetaMachine() instanceof IWorkable workable) {
                return workable;
            }
            for (MachineTrait trait : ((IMachineBlockEntity)blockEntity).getMetaMachine().getTraits()) {
                if (trait instanceof IWorkable workable) {
                    return workable;
                }
            }
            return null;
        }, type);
        GTCapability.CAPABILITY_CONTROLLABLE.registerForBlockEntity((blockEntity, direction) -> {
            if (((IMachineBlockEntity)blockEntity).getMetaMachine() instanceof IControllable controllable) {
                return controllable;
            }
            for (MachineTrait trait : ((IMachineBlockEntity)blockEntity).getMetaMachine().getTraits()) {
                if (trait instanceof IControllable controllable) {
                    return controllable;
                }
            }
            return null;
        }, type);
        GTCapability.CAPABILITY_RECIPE_LOGIC.registerForBlockEntity((blockEntity, direction) -> {
            for (MachineTrait trait : ((IMachineBlockEntity)blockEntity).getMetaMachine().getTraits()) {
                if (trait instanceof RecipeLogic recipeLogic) {
                    return recipeLogic;
                }
            }
            return null;
        }, type);
        GTCapability.CAPABILITY_ENERGY.registerForBlockEntity((blockEntity, side) -> {
            if (((IMachineBlockEntity)blockEntity).getMetaMachine() instanceof IEnergyContainer energyContainer) {
                return  energyContainer;
            }
            var list = ((IMachineBlockEntity)blockEntity).getMetaMachine().getTraits().stream().filter(IEnergyContainer.class::isInstance).filter(t -> t.hasCapability(side)).map(IEnergyContainer.class::cast).toList();
            return list.isEmpty() ? null : list.size() == 1 ? list.get(0) : new EnergyContainerList(list);
        }, type);
        GTCapability.CAPABILITY_CLEANROOM_RECEIVER.registerForBlockEntity((blockEntity, direction) -> {
            if (((IMachineBlockEntity)blockEntity).getMetaMachine() instanceof ICleanroomReceiver cleanroomReceiver) {
                return cleanroomReceiver;
            }
            return null;
        }, type);
        GTCapability.CAPABILITY_MAINTENANCE_MACHINE.registerForBlockEntity((blockEntity, direction) -> {
            if (((IMachineBlockEntity)blockEntity).getMetaMachine() instanceof IMaintenanceMachine maintenanceMachine) {
                return maintenanceMachine;
            }
            return null;
        }, type);
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, side) -> {
            if (((IMachineBlockEntity)blockEntity).getMetaMachine() instanceof LDItemEndpointMachine fluidEndpointMachine) {
                if (fluidEndpointMachine.getLevel().isClientSide) return null;
                ILDEndpoint endpoint = fluidEndpointMachine.getLink();
                if (endpoint == null) return null;
                Direction outputFacing = fluidEndpointMachine.getOutputFacing();
                IItemTransfer transfer = ItemTransferHelperImpl.getItemTransfer(blockEntity.getLevel(), endpoint.getPos().relative(outputFacing), outputFacing.getOpposite());
                if (transfer != null) {
                    return ItemTransferHelperImpl.toItemVariantStorage(new LDItemEndpointMachine.ItemHandlerWrapper(transfer));
                }
            }
            var transfer = ((IMachineBlockEntity)blockEntity).getMetaMachine().getItemTransferCap(side);
            return transfer == null ? null : ItemTransferHelperImpl.toItemVariantStorage(transfer);
        }, type);
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, side) -> {
            if (((IMachineBlockEntity)blockEntity).getMetaMachine() instanceof LDFluidEndpointMachine fluidEndpointMachine) {
                if (fluidEndpointMachine.getLevel().isClientSide) return null;
                ILDEndpoint endpoint = fluidEndpointMachine.getLink();
                if (endpoint == null) return null;
                Direction outputFacing = fluidEndpointMachine.getOutputFacing();
                IFluidTransfer transfer = FluidTransferHelper.getFluidTransfer(blockEntity.getLevel(), endpoint.getPos().relative(outputFacing), outputFacing.getOpposite());
                if (transfer != null) {
                    return FluidTransferHelperImpl.toFluidVariantStorage(new LDFluidEndpointMachine.FluidHandlerWrapper(transfer));
                }
            }
            var transfer = ((IMachineBlockEntity)blockEntity).getMetaMachine().getFluidTransferCap(side);
            return transfer == null ? null : FluidTransferHelperImpl.toFluidVariantStorage(transfer);
        }, type);
        if (GTCEu.isRebornEnergyLoaded()) {
            EnergyStorage.SIDED.registerForBlockEntity((blockEntity, side) -> {
                if (((IMachineBlockEntity)blockEntity).getMetaMachine() instanceof IPlatformEnergyStorage platformEnergyStorage) {
                    return GTEnergyHelperImpl.toEnergyStorage(platformEnergyStorage);
                }
                var list = ((IMachineBlockEntity)blockEntity).getMetaMachine().getTraits().stream().filter(IPlatformEnergyStorage.class::isInstance).filter(t -> t.hasCapability(side)).map(IPlatformEnergyStorage.class::cast).toList();
                // TODO wrap list in the future
                return list.isEmpty() ? null : GTEnergyHelperImpl.toEnergyStorage(list.get(0));
            }, type);
        }
    }

}
