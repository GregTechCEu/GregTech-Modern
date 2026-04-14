package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.sync_system.ISyncManaged;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomProviderTrait;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * A machine trait represents a generic capability or behaviour that is attached to a machine.
 * For example, machine traits may provide a recipe handler that can handle specific inputs/outputs of a recipe (e.g.
 * {@link NotifiableItemStackHandler for items}).
 * Machine traits can also attach additional behaviours to a machine (e.g. {@link AutoOutputTrait},
 * {@link CleanroomProviderTrait})
 */
@MethodsReturnNonnullByDefault
public abstract class MachineTrait implements ISyncManaged {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    private @Nullable MetaMachine machine;
    @Setter
    protected Predicate<@Nullable Direction> capabilityValidator = $ -> true;

    public MachineTrait(MetaMachine machine) {
        this.capabilityValidator = side -> true;
        machine.getTraitHolder().attachTrait(this);
    }

    public MachineTrait() {}

    public MetaMachine getMachine() {
        if (machine == null) throw new IllegalStateException("Machine trait not attached to machine.");
        return machine;
    }

    /**
     * A list containing the machine classes which this trait can be attached to.
     * If this trait is being attached to a machine class that does not conform to any of the list elements, an
     * exception is thrown.
     * If this list is empty, the trait can be attached to any machine.
     */
    protected List<Class<?>> validMachineClasses() {
        return List.of();
    }

    public void setMachine(MetaMachine machine) {
        if (this.machine != null) throw new IllegalStateException("Machine trait already attached to a machine.");
        if (!validMachineClasses().isEmpty() &&
                validMachineClasses().stream().noneMatch(cls -> cls.isAssignableFrom(machine.getClass()))) {
            throw new IllegalArgumentException(
                    "Attempted to attach trait to invalid machine class %s".formatted(machine.getClass()));
        }
        this.machine = machine;
    }

    public abstract MachineTraitType<?> getTraitType();

    public @Nullable TickableSubscription subscribeServerTick(@Nullable TickableSubscription last, Runnable runnable) {
        return getMachine().subscribeServerTick(last, runnable);
    }

    public void unsubscribe(TickableSubscription current) {
        getMachine().unsubscribe(current);
    }

    public BlockPos getBlockPos() {
        return getMachine().getBlockPos();
    }

    public Level getLevel() {
        return getMachine().getLevel();
    }

    public boolean isRemote() {
        return getMachine().isRemote();
    }

    public final boolean hasCapability(@Nullable Direction side) {
        return capabilityValidator.test(side);
    }

    @Override
    public void markAsChanged() {
        if (machine == null) return;
        getMachine().markAsChanged();
    }

    public MachineRenderState getRenderState() {
        return getMachine().getRenderState();
    }

    public void setRenderState(MachineRenderState state) {
        getMachine().setRenderState(state);
    }

    public void scheduleRenderUpdate() {
        getMachine().scheduleRenderUpdate();
    }

    public void onMachineLoad() {}

    public void onMachineUnload() {}

    public void onMachineNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {}
}
