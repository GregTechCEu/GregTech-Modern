package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.misc.EnergyInfoProviderList;
import com.gregtechceu.gtceu.api.misc.LaserContainerList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.machine.MetaMachine.getCapabilitiesFromTraits;

public class GTCapabilityHelper {

    @Nullable
    public static IElectricItem getElectricItem(ItemStack itemStack) {
        return itemStack.getCapability(GTCapability.CAPABILITY_ELECTRIC_ITEM).resolve().orElse(null);
    }

    @Nullable
    public static IEnergyStorage getForgeEnergyItem(ItemStack itemStack) {
        return itemStack.getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
    }

    @Nullable
    public static IItemHandler getItemHandler(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(ForgeCapabilities.ITEM_HANDLER, level, pos, side);
    }

    @Nullable
    public static IFluidHandler getFluidHandler(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(ForgeCapabilities.FLUID_HANDLER, level, pos, side);
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, level, pos, side);
    }

    @Nullable
    public static IEnergyInfoProvider getEnergyInfoProvider(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER, level, pos, side);
    }

    @Nullable
    public static ICoverable getCoverable(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_COVERABLE, level, pos, side);
    }

    @Nullable
    public static IToolable getToolable(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_TOOLABLE, level, pos, side);
    }

    @Nullable
    public static IWorkable getWorkable(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_WORKABLE, level, pos, side);
    }

    @Nullable
    public static IControllable getControllable(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_CONTROLLABLE, level, pos, side);
    }

    @Nullable
    public static RecipeLogic getRecipeLogic(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_RECIPE_LOGIC, level, pos, side);
    }

    @Nullable
    public static IEnergyStorage getForgeEnergy(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public static ICleanroomReceiver getCleanroomReceiver(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_CLEANROOM_RECEIVER, level, pos, side);
    }

    @Nullable
    public static IMaintenanceMachine getMaintenanceMachine(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_MAINTENANCE_MACHINE, level, pos, side);
    }

    @Nullable
    public static ILaserContainer getLaser(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_LASER, level, pos, side);
    }

    @Nullable
    public static IOpticalComputationProvider getOpticalComputationProvider(Level level, BlockPos pos,
                                                                            @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, level, pos, side);
    }

    @Nullable
    public static IDataAccessHatch getDataAccess(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_DATA_ACCESS, level, pos, side);
    }

    @Nullable
    public static IHazardParticleContainer getHazardContainer(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_HAZARD_CONTAINER, level, pos, side);
    }

    @Nullable
    public static IMonitorComponent getMonitorComponent(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_MONITOR_COMPONENT, level, pos, side);
    }

    @Nullable
    private static <T> T getBlockEntityCapability(Capability<T> capability, Level level, BlockPos pos,
                                                  @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(capability, side).resolve().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public static IMedicalConditionTracker getMedicalConditionTracker(@NotNull Entity entity) {
        return entity.getCapability(GTCapability.CAPABILITY_MEDICAL_CONDITION_TRACKER, null).resolve().orElse(null);
    }

    public static @NotNull <T> LazyOptional<T> getMetaMachineCapability(MetaMachine machine, @NotNull Capability<T> cap,
                                                                        @Nullable Direction side) {
        if (cap == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(machine::getCoverContainer));
        } else if (cap == GTCapability.CAPABILITY_TOOLABLE) {
            return GTCapability.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> machine));
        } else if (cap == GTCapability.CAPABILITY_WORKABLE) {
            if (machine instanceof IWorkable workable) {
                return GTCapability.CAPABILITY_WORKABLE.orEmpty(cap, LazyOptional.of(() -> workable));
            }
            for (MachineTrait trait : machine.getTraits()) {
                if (trait instanceof IWorkable workable) {
                    return GTCapability.CAPABILITY_WORKABLE.orEmpty(cap, LazyOptional.of(() -> workable));
                }
            }
        } else if (cap == GTCapability.CAPABILITY_CONTROLLABLE) {
            if (machine instanceof IControllable controllable) {
                return GTCapability.CAPABILITY_CONTROLLABLE.orEmpty(cap, LazyOptional.of(() -> controllable));
            }
            for (MachineTrait trait : machine.getTraits()) {
                if (trait instanceof IControllable controllable) {
                    return GTCapability.CAPABILITY_CONTROLLABLE.orEmpty(cap, LazyOptional.of(() -> controllable));
                }
            }
        } else if (cap == GTCapability.CAPABILITY_RECIPE_LOGIC) {
            for (MachineTrait trait : machine.getTraits()) {
                if (trait instanceof RecipeLogic recipeLogic) {
                    return GTCapability.CAPABILITY_RECIPE_LOGIC.orEmpty(cap, LazyOptional.of(() -> recipeLogic));
                }
            }
        } else if (cap == GTCapability.CAPABILITY_ENERGY_CONTAINER) {
            if (machine instanceof IEnergyContainer energyContainer) {
                return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(() -> energyContainer));
            }
            var list = getCapabilitiesFromTraits(machine.getTraits(), side, IEnergyContainer.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap,
                        LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new EnergyContainerList(list)));
            }
        } else if (cap == GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER) {
            if (machine instanceof IEnergyInfoProvider energyInfoProvider) {
                return GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER.orEmpty(cap,
                        LazyOptional.of(() -> energyInfoProvider));
            }
            var list = getCapabilitiesFromTraits(machine.getTraits(), side, IEnergyInfoProvider.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER.orEmpty(cap,
                        LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new EnergyInfoProviderList(list)));
            }
        } else if (cap == GTCapability.CAPABILITY_CLEANROOM_RECEIVER) {
            if (machine instanceof ICleanroomReceiver cleanroomReceiver) {
                return GTCapability.CAPABILITY_CLEANROOM_RECEIVER.orEmpty(cap,
                        LazyOptional.of(() -> cleanroomReceiver));
            }
        } else if (cap == GTCapability.CAPABILITY_MAINTENANCE_MACHINE) {
            if (machine instanceof IMaintenanceMachine maintenanceMachine) {
                return GTCapability.CAPABILITY_MAINTENANCE_MACHINE.orEmpty(cap,
                        LazyOptional.of(() -> maintenanceMachine));
            }
        } else if (cap == GTCapability.CAPABILITY_TURBINE_MACHINE) {
            if (machine instanceof ITurbineMachine turbineMachine) {
                return GTCapability.CAPABILITY_TURBINE_MACHINE.orEmpty(cap,
                        LazyOptional.of(() -> turbineMachine));
            }
        } else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            var handler = machine.getItemHandlerCap(side, true);
            if (handler != null) {
                return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
            }
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            var handler = machine.getFluidHandlerCap(side, true);
            if (handler != null) {
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
            }
        } else if (cap == ForgeCapabilities.ENERGY) {
            if (machine instanceof IEnergyStorage energyStorage) {
                return ForgeCapabilities.ENERGY.orEmpty(cap, LazyOptional.of(() -> energyStorage));
            }
            var list = getCapabilitiesFromTraits(machine.getTraits(), side, IEnergyStorage.class);
            if (!list.isEmpty()) {
                // TODO wrap list in the future
                return ForgeCapabilities.ENERGY.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
            }
        } else if (cap == GTCapability.CAPABILITY_LASER) {
            if (machine instanceof ILaserContainer energyContainer) {
                return GTCapability.CAPABILITY_LASER.orEmpty(cap, LazyOptional.of(() -> energyContainer));
            }
            var list = getCapabilitiesFromTraits(machine.getTraits(), side, ILaserContainer.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_LASER.orEmpty(cap,
                        LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new LaserContainerList(list)));
            }
        } else if (cap == GTCapability.CAPABILITY_COMPUTATION_PROVIDER) {
            if (machine instanceof IOpticalComputationProvider computationProvider) {
                return GTCapability.CAPABILITY_COMPUTATION_PROVIDER.orEmpty(cap,
                        LazyOptional.of(() -> computationProvider));
            }
            var list = getCapabilitiesFromTraits(machine.getTraits(), side, IOpticalComputationProvider.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_COMPUTATION_PROVIDER.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
            }
        } else if (cap == GTCapability.CAPABILITY_DATA_ACCESS) {
            if (machine instanceof IDataAccessHatch computationProvider) {
                return GTCapability.CAPABILITY_DATA_ACCESS.orEmpty(cap, LazyOptional.of(() -> computationProvider));
            }
            var list = getCapabilitiesFromTraits(machine.getTraits(), side, IDataAccessHatch.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_DATA_ACCESS.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
            }
        } else if (cap == GTCapability.CAPABILITY_MONITOR_COMPONENT) {
            if (machine instanceof IMonitorComponent monitorComponent) {
                return GTCapability.CAPABILITY_MONITOR_COMPONENT.orEmpty(cap, LazyOptional.of(() -> monitorComponent));
            }
            var list = getCapabilitiesFromTraits(machine.getTraits(), side, IMonitorComponent.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_MONITOR_COMPONENT.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
            }
        } else if (cap == GTCapability.CAPABILITY_CENTRAL_MONITOR) {
            if (machine instanceof ICentralMonitor centralMonitor) {
                return GTCapability.CAPABILITY_CENTRAL_MONITOR.orEmpty(cap, LazyOptional.of(() -> centralMonitor));
            }
            var list = getCapabilitiesFromTraits(machine.getTraits(), side, ICentralMonitor.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_CENTRAL_MONITOR.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
            }
        }
        if (GTCEu.Mods.isAE2Loaded()) {
            LazyOptional<?> opt = MetaMachine.AE2CallWrapper.getGridNodeHostCapability(cap, machine, side);
            if (opt.isPresent()) {
                // noinspection unchecked
                return (LazyOptional<T>) opt;
            }
        }
        return LazyOptional.empty();
    }
}
