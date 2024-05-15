package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.RotationState;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.forge.compat.EnergyStorageList;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.misc.EnergyInfoProviderList;
import com.gregtechceu.gtceu.api.misc.LaserContainerList;
import com.gregtechceu.gtceu.api.pipenet.longdistance.ILDEndpoint;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance.LDFluidEndpointMachine;
import com.gregtechceu.gtceu.common.pipelike.item.longdistance.LDItemEndpointMachine;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import appeng.api.networking.IInWorldGridNodeHost;
import appeng.capabilities.AppEngCapabilities;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote IMachineBlock
 */
public interface IMachineBlock extends IBlockRendererProvider, EntityBlock {

    default Block self() {
        return (Block) this;
    }

    MachineDefinition getDefinition();

    RotationState getRotationState();

    static int colorTinted(BlockState blockState, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                           int index) {
        if (level != null && pos != null) {
            var machine = MetaMachine.getMachine(level, pos);
            if (machine != null) {
                return machine.tintColor(index);
            }
        }
        return -1;
    }

    @Nullable
    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getDefinition().getBlockEntityType().create(pos, state);
    }

    @Nullable
    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> blockEntityType) {
        if (blockEntityType == getDefinition().getBlockEntityType()) {
            if (state.getValue(BlockProperties.SERVER_TICK) && !level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().serverTick();
                    }
                };
            }
            if (level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().clientTick();
                    }
                };
            }
        }
        return null;
    }

    default void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(GTCapability.CAPABILITY_COVERABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                return machine.getMetaMachine().getCoverContainer();
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_TOOLABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                return machine.getMetaMachine();
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_WORKABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IWorkable workable) {
                    return workable;
                }
                for (MachineTrait trait : machine.getMetaMachine().getTraits()) {
                    if (trait instanceof IWorkable workable) {
                        return workable;
                    }
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_CONTROLLABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IControllable controllable) {
                    return controllable;
                }
                for (MachineTrait trait : machine.getMetaMachine().getTraits()) {
                    if (trait instanceof IControllable controllable) {
                        return controllable;
                    }
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_RECIPE_LOGIC, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                for (MachineTrait trait : machine.getMetaMachine().getTraits()) {
                    if (trait instanceof RecipeLogic recipeLogic) {
                        return recipeLogic;
                    }
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_ENERGY_CONTAINER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IEnergyContainer energyContainer) {
                    return energyContainer;
                }
                var list = machine.getMetaMachine().getTraits().stream().filter(IEnergyContainer.class::isInstance)
                        .filter(t -> t.hasCapability(side)).map(IEnergyContainer.class::cast).toList();
                if (!list.isEmpty()) {
                    return new EnergyContainerList(list);
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IEnergyInfoProvider energyInfoProvider) {
                    return energyInfoProvider;
                }
                var list = machine.getMetaMachine().getTraits().stream().filter(IEnergyInfoProvider.class::isInstance)
                        .filter(t -> t.hasCapability(side)).map(IEnergyInfoProvider.class::cast).toList();
                if (!list.isEmpty()) {
                    return new EnergyInfoProviderList(list);
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_CLEANROOM_RECEIVER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof ICleanroomReceiver cleanroomReceiver) {
                    return cleanroomReceiver;
                }
                for (MachineTrait trait : machine.getMetaMachine().getTraits()) {
                    if (trait instanceof ICleanroomReceiver cleanroomReceiver) {
                        return cleanroomReceiver;
                    }
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_CLEANROOM_RECEIVER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof ICleanroomReceiver workable) {
                    return workable;
                }
                for (MachineTrait trait : machine.getMetaMachine().getTraits()) {
                    if (trait instanceof ICleanroomReceiver workable) {
                        return workable;
                    }
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_MAINTENANCE_MACHINE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IMaintenanceMachine maintenance) {
                    return maintenance;
                }
            }
            return null;
        }, this.self());
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machineBe) {
                MetaMachine machine = machineBe.getMetaMachine();
                if (machine instanceof LDItemEndpointMachine fluidEndpointMachine) {
                    if (machine.getLevel().isClientSide)
                        return null;
                    ILDEndpoint endpoint = fluidEndpointMachine.getLink();
                    if (endpoint == null)
                        return null;
                    Direction outputFacing = fluidEndpointMachine.getOutputFacing();
                    IItemHandler transfer = machine.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,
                            endpoint.getPos().relative(outputFacing), outputFacing.getOpposite());
                    if (transfer != null) {
                        new LDItemEndpointMachine.ItemHandlerWrapper(transfer);
                    }
                }
                return machine.getItemTransferCap(side, true);
            }
            return null;
        }, this.self());
        event.registerBlock(Capabilities.FluidHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machineBe) {
                MetaMachine machine = machineBe.getMetaMachine();
                if (machine instanceof LDFluidEndpointMachine fluidEndpointMachine) {
                    if (machine.getLevel().isClientSide)
                        return null;
                    ILDEndpoint endpoint = fluidEndpointMachine.getLink();
                    if (endpoint == null)
                        return null;
                    Direction outputFacing = fluidEndpointMachine.getOutputFacing();
                    IFluidHandler transfer = machine.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                            endpoint.getPos().relative(outputFacing), outputFacing.getOpposite());
                    if (transfer != null) {
                        return new LDFluidEndpointMachine.FluidHandlerWrapper(transfer);
                    }
                }
                return machine.getFluidTransferCap(side, true);
            }
            return null;
        }, this.self());
        event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IEnergyStorage energyStorage) {
                    return energyStorage;
                }
                var list = machine.getMetaMachine().getTraits().stream().filter(IEnergyStorage.class::isInstance)
                        .filter(t -> t.hasCapability(side)).map(IEnergyStorage.class::cast).toList();
                if (!list.isEmpty()) {
                    return new EnergyStorageList(list);
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_LASER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof ILaserContainer energyContainer) {
                    return energyContainer;
                }
                var list = machine.getMetaMachine().getTraits().stream().filter(ILaserContainer.class::isInstance)
                        .filter(t -> t.hasCapability(side)).map(ILaserContainer.class::cast).toList();
                if (!list.isEmpty()) {
                    return new LaserContainerList(list);
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IOpticalComputationProvider computationProvider) {
                    return computationProvider;
                }
                var list = machine.getMetaMachine().getTraits().stream()
                        .filter(IOpticalComputationProvider.class::isInstance).filter(t -> t.hasCapability(side))
                        .map(IOpticalComputationProvider.class::cast).toList();
                if (!list.isEmpty()) {
                    return list.get(0);
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_DATA_ACCESS, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IDataAccessHatch dataAccess) {
                    return dataAccess;
                }
                var list = machine.getMetaMachine().getTraits().stream().filter(IDataAccessHatch.class::isInstance)
                        .filter(t -> t.hasCapability(side)).map(IDataAccessHatch.class::cast).toList();
                if (!list.isEmpty()) {
                    return list.get(0);
                }
            }
            return null;
        }, this.self());
        if (GTCEu.isAE2Loaded()) {
            event.registerBlock(AppEngCapabilities.IN_WORLD_GRID_NODE_HOST, (level, pos, state, blockEntity, side) -> {
                if (blockEntity instanceof IMachineBlockEntity machine) {
                    if (machine.getMetaMachine() instanceof IInWorldGridNodeHost nodeHost) {
                        return nodeHost;
                    }
                    var list = machine.getMetaMachine().getTraits().stream()
                            .filter(IInWorldGridNodeHost.class::isInstance).filter(t -> t.hasCapability(side))
                            .map(IInWorldGridNodeHost.class::cast).toList();
                    if (!list.isEmpty()) {
                        // TODO wrap list in the future (or not.)
                        return list.get(0);
                    }
                }
                return null;
            }, this.self());
        }
    }
}
