package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class MetaMachineBlockEntity extends BlockEntity implements IMachineBlockEntity, IManaged {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MetaMachineBlockEntity.class);

    public final MultiManagedStorage managedStorage = new MultiManagedStorage();
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    @Getter
    public final MetaMachine metaMachine;
    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    private MachineRenderState renderState;
    private final long offset = GTValues.RNG.nextInt(20);

    public MetaMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.renderState = getDefinition().defaultRenderState();
        this.metaMachine = getDefinition().createMetaMachine(this);

        this.getRootStorage().attach(getSyncStorage());
    }

    @Override
    public MultiManagedStorage getRootStorage() {
        return managedStorage;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        var level = getLevel();
        if (level != null && !level.isClientSide && level.getServer() != null) {
            level.getServer().execute(this::setChanged);
        }
    }

    @Override
    public boolean triggerEvent(int id, int para) {
        if (id == 1) { // chunk re render
            if (level != null && level.isClientSide) {
                scheduleRenderUpdate();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        metaMachine.applyImplicitComponents(new ExDataComponentInput() {

            @Override
            public @Nullable <T> T get(DataComponentType<T> component) {
                return componentInput.get(component);
            }

            @Override
            public <T> T getOrDefault(DataComponentType<? extends T> component, T defaultValue) {
                return componentInput.getOrDefault(component, defaultValue);
            }
        });
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        metaMachine.collectImplicitComponents(components);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        metaMachine.removeItemComponentsFromTag(tag);
    }

    @Override
    public void setRenderState(MachineRenderState state) {
        this.renderState = state;
        scheduleRenderUpdate();
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        metaMachine.onUnload();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        metaMachine.onLoad();
    }

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        return metaMachine.shouldRenderGrid(player, pos, state, held, toolTypes);
    }

    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              ItemStack held, Direction side) {
        return metaMachine.sideTips(player, pos, state, toolTypes, held, side);
    }

    @Override
    public void setChanged() {
        if (getLevel() != null) {
            getLevel().blockEntityChanged(getBlockPos());
        }
    }

    // public static <T> LazyOptional<T> getCapability(MetaMachine machine, @NotNull Capability<T> cap,
    // @Nullable Direction side) {
    // if (cap == GTCapability.CAPABILITY_COVERABLE) {
    // return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(machine::getCoverContainer));
    // } else if (cap == GTCapability.CAPABILITY_TOOLABLE) {
    // return GTCapability.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> machine));
    // } else if (cap == GTCapability.CAPABILITY_WORKABLE) {
    // if (machine instanceof IWorkable workable) {
    // return GTCapability.CAPABILITY_WORKABLE.orEmpty(cap, LazyOptional.of(() -> workable));
    // }
    // for (MachineTrait trait : machine.getTraits()) {
    // if (trait instanceof IWorkable workable) {
    // return GTCapability.CAPABILITY_WORKABLE.orEmpty(cap, LazyOptional.of(() -> workable));
    // }
    // }
    // } else if (cap == GTCapability.CAPABILITY_CONTROLLABLE) {
    // if (machine instanceof IControllable controllable) {
    // return GTCapability.CAPABILITY_CONTROLLABLE.orEmpty(cap, LazyOptional.of(() -> controllable));
    // }
    // for (MachineTrait trait : machine.getTraits()) {
    // if (trait instanceof IControllable controllable) {
    // return GTCapability.CAPABILITY_CONTROLLABLE.orEmpty(cap, LazyOptional.of(() -> controllable));
    // }
    // }
    // } else if (cap == GTCapability.CAPABILITY_RECIPE_LOGIC) {
    // for (MachineTrait trait : machine.getTraits()) {
    // if (trait instanceof RecipeLogic recipeLogic) {
    // return GTCapability.CAPABILITY_RECIPE_LOGIC.orEmpty(cap, LazyOptional.of(() -> recipeLogic));
    // }
    // }
    // } else if (cap == GTCapability.CAPABILITY_ENERGY_CONTAINER) {
    // if (machine instanceof IEnergyContainer energyContainer) {
    // return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(() -> energyContainer));
    // }
    // var list = getCapabilitiesFromTraits(machine.getTraits(), side, IEnergyContainer.class);
    // if (!list.isEmpty()) {
    // return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap,
    // LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new EnergyContainerList(list)));
    // }
    // } else if (cap == GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER) {
    // if (machine instanceof IEnergyInfoProvider energyInfoProvider) {
    // return GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER.orEmpty(cap,
    // LazyOptional.of(() -> energyInfoProvider));
    // }
    // var list = getCapabilitiesFromTraits(machine.getTraits(), side, IEnergyInfoProvider.class);
    // if (!list.isEmpty()) {
    // return GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER.orEmpty(cap,
    // LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new EnergyInfoProviderList(list)));
    // }
    // } else if (cap == GTCapability.CAPABILITY_CLEANROOM_RECEIVER) {
    // if (machine instanceof ICleanroomReceiver cleanroomReceiver) {
    // return GTCapability.CAPABILITY_CLEANROOM_RECEIVER.orEmpty(cap,
    // LazyOptional.of(() -> cleanroomReceiver));
    // }
    // } else if (cap == GTCapability.CAPABILITY_MAINTENANCE_MACHINE) {
    // if (machine instanceof IMaintenanceMachine maintenanceMachine) {
    // return GTCapability.CAPABILITY_MAINTENANCE_MACHINE.orEmpty(cap,
    // LazyOptional.of(() -> maintenanceMachine));
    // }
    // } else if (cap == GTCapability.CAPABILITY_TURBINE_MACHINE) {
    // if (machine instanceof ITurbineMachine turbineMachine) {
    // return GTCapability.CAPABILITY_TURBINE_MACHINE.orEmpty(cap,
    // LazyOptional.of(() -> turbineMachine));
    // }
    // } else if (cap == ForgeCapabilities.ITEM_HANDLER) {
    // var handler = machine.getItemHandlerCap(side, true);
    // if (handler != null) {
    // return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
    // }
    // } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
    // var handler = machine.getFluidHandlerCap(side, true);
    // if (handler != null) {
    // return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
    // }
    // } else if (cap == ForgeCapabilities.ENERGY) {
    // if (machine instanceof IEnergyStorage energyStorage) {
    // return ForgeCapabilities.ENERGY.orEmpty(cap, LazyOptional.of(() -> energyStorage));
    // }
    // var list = getCapabilitiesFromTraits(machine.getTraits(), side, IEnergyStorage.class);
    // if (!list.isEmpty()) {
    // // TODO wrap list in the future
    // return ForgeCapabilities.ENERGY.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
    // }
    // } else if (cap == GTCapability.CAPABILITY_LASER) {
    // if (machine instanceof ILaserContainer energyContainer) {
    // return GTCapability.CAPABILITY_LASER.orEmpty(cap, LazyOptional.of(() -> energyContainer));
    // }
    // var list = getCapabilitiesFromTraits(machine.getTraits(), side, ILaserContainer.class);
    // if (!list.isEmpty()) {
    // return GTCapability.CAPABILITY_LASER.orEmpty(cap,
    // LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new LaserContainerList(list)));
    // }
    // } else if (cap == GTCapability.CAPABILITY_COMPUTATION_PROVIDER) {
    // if (machine instanceof IOpticalComputationProvider computationProvider) {
    // return GTCapability.CAPABILITY_COMPUTATION_PROVIDER.orEmpty(cap,
    // LazyOptional.of(() -> computationProvider));
    // }
    // var list = getCapabilitiesFromTraits(machine.getTraits(), side, IOpticalComputationProvider.class);
    // if (!list.isEmpty()) {
    // return GTCapability.CAPABILITY_COMPUTATION_PROVIDER.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
    // }
    // } else if (cap == GTCapability.CAPABILITY_DATA_ACCESS) {
    // if (machine instanceof IDataAccessHatch computationProvider) {
    // return GTCapability.CAPABILITY_DATA_ACCESS.orEmpty(cap, LazyOptional.of(() -> computationProvider));
    // }
    // var list = getCapabilitiesFromTraits(machine.getTraits(), side, IDataAccessHatch.class);
    // if (!list.isEmpty()) {
    // return GTCapability.CAPABILITY_DATA_ACCESS.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
    // }
    // } else if (cap == GTCapability.CAPABILITY_MONITOR_COMPONENT) {
    // if (machine instanceof IMonitorComponent monitorComponent) {
    // return GTCapability.CAPABILITY_MONITOR_COMPONENT.orEmpty(cap, LazyOptional.of(() -> monitorComponent));
    // }
    // var list = getCapabilitiesFromTraits(machine.getTraits(), side, IMonitorComponent.class);
    // if (!list.isEmpty()) {
    // return GTCapability.CAPABILITY_MONITOR_COMPONENT.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
    // }
    // } else if (cap == GTCapability.CAPABILITY_CENTRAL_MONITOR) {
    // if (machine instanceof ICentralMonitor centralMonitor) {
    // return GTCapability.CAPABILITY_CENTRAL_MONITOR.orEmpty(cap, LazyOptional.of(() -> centralMonitor));
    // }
    // var list = getCapabilitiesFromTraits(machine.getTraits(), side, ICentralMonitor.class);
    // if (!list.isEmpty()) {
    // return GTCapability.CAPABILITY_CENTRAL_MONITOR.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
    // }
    // }
    // if (GTCEu.Mods.isAE2Loaded()) {
    // LazyOptional<?> opt = AE2CallWrapper.getGridNodeHostCapability(cap, machine, side);
    // if (opt.isPresent()) {
    // // noinspection unchecked
    // return (LazyOptional<T>) opt;
    // }
    // }
    // return LazyOptional.empty();
    // }

    // public static <T> List<T> getCapabilitiesFromTraits(List<MachineTrait> traits, Direction accessSide,
    // Class<T> capability) {
    // if (traits.isEmpty()) return Collections.emptyList();
    // List<T> list = new ArrayList<>();
    // for (MachineTrait trait : traits) {
    // if (trait.hasCapability(accessSide) && capability.isInstance(trait)) {
    // list.add(capability.cast(trait));
    // }
    // }
    // return list;
    // }

    // >>>>>>> v7.1.0-1.20.1
    /**
     * Extending interface to make {@link BlockEntity.DataComponentInput} public as it's protected by default.
     */
    public interface ExDataComponentInput extends BlockEntity.DataComponentInput {}
}
