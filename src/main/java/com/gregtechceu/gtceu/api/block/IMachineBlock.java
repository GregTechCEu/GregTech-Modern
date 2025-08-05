package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.compat.EnergyStorageList;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.RotationState;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.misc.EnergyInfoProviderList;
import com.gregtechceu.gtceu.api.misc.LaserContainerList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
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

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface IMachineBlock extends EntityBlock {

    default Block self() {
        return (Block) this;
    }

    MachineDefinition getDefinition();

    default RotationState getRotationState() {
        return getDefinition().getRotationState();
    }

    default Direction getFrontFacing(BlockState state) {
        return getRotationState() == RotationState.NONE ? Direction.NORTH : state.getValue(getRotationState().property);
    }

    @Nullable
    default MetaMachine getMachine(BlockGetter level, BlockPos pos) {
        return MetaMachine.getMachine(level, pos);
    }

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
            if (!level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().serverTick();
                    }
                };
            } else {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().clientTick();
                    }
                };
            }
        }
        return null;
    }

    default boolean canConnectRedstone(BlockGetter level, BlockPos pos, Direction side) {
        return getMachine(level, pos).canConnectRedstone(side);
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
                var list = getCapabilitiesFromTraits(machine.getMetaMachine().getTraits(), side,
                        IEnergyContainer.class);
                if (!list.isEmpty()) {
                    return list.size() == 1 ? list.get(0) : new EnergyContainerList(list);
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IEnergyInfoProvider energyInfoProvider) {
                    return energyInfoProvider;
                }
                var list = getCapabilitiesFromTraits(machine.getMetaMachine().getTraits(), side,
                        IEnergyInfoProvider.class);
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
                return machineBe.getMetaMachine().getItemHandlerCap(side, true);
            }
            return null;
        }, this.self());
        event.registerBlock(Capabilities.FluidHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machineBe) {
                return machineBe.getMetaMachine().getFluidHandlerCap(side, true);
            }
            return null;
        }, this.self());
        event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IEnergyStorage energyStorage) {
                    return energyStorage;
                }
                var list = getCapabilitiesFromTraits(machine.getMetaMachine().getTraits(), side, IEnergyStorage.class);
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
                var list = getCapabilitiesFromTraits(machine.getMetaMachine().getTraits(), side, ILaserContainer.class);
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
                var list = getCapabilitiesFromTraits(machine.getMetaMachine().getTraits(), side,
                        IOpticalComputationProvider.class);
                if (!list.isEmpty()) {
                    return list.getFirst();
                }
            }
            return null;
        }, this.self());
        event.registerBlock(GTCapability.CAPABILITY_DATA_ACCESS, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMachineBlockEntity machine) {
                if (machine.getMetaMachine() instanceof IDataAccessHatch dataAccess) {
                    return dataAccess;
                }
                var list = getCapabilitiesFromTraits(machine.getMetaMachine().getTraits(), side,
                        IDataAccessHatch.class);
                if (!list.isEmpty()) {
                    return list.getFirst();
                }
            }
            return null;
        }, this.self());
        if (GTCEu.Mods.isAE2Loaded()) {
            event.registerBlock(AECapabilities.IN_WORLD_GRID_NODE_HOST, (level, pos, state, blockEntity, side) -> {
                if (blockEntity instanceof IMachineBlockEntity machine) {
                    if (machine.getMetaMachine() instanceof IInWorldGridNodeHost nodeHost) {
                        return nodeHost;
                    }
                    var list = getCapabilitiesFromTraits(machine.getMetaMachine().getTraits(), null,
                            IInWorldGridNodeHost.class);
                    if (!list.isEmpty()) {
                        // TODO wrap list in the future (or not.)
                        return list.getFirst();
                    }
                }
                return null;
            }, this.self());
        }
    }

    static <T> List<T> getCapabilitiesFromTraits(List<MachineTrait> traits, @Nullable Direction accessSide,
                                                 Class<T> capability) {
        if (traits.isEmpty()) return Collections.emptyList();
        List<T> list = new ArrayList<>();
        for (MachineTrait trait : traits) {
            if (trait.hasCapability(accessSide) && capability.isInstance(trait)) {
                list.add(capability.cast(trait));
            }
        }
        return list;
    }
}
