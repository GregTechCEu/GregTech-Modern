package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.SyncDataHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

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

    @Getter
    protected final MetaMachine machine;
    @Setter
    protected Predicate<@Nullable Direction> capabilityValidator;

    public MachineTrait(MetaMachine machine) {
        this.machine = machine;
        this.capabilityValidator = side -> true;
        // Machine should never be null, unless this trait is a recipe handler instantiated outside a machine for
        // recipe search.
        if (machine != null) machine.getTraitHolder().attachTrait(this);
    }

    public abstract MachineTraitType<?> getTraitType();

    public Level getLevel() {
        return machine.getLevel();
    }

    public final boolean hasCapability(@Nullable Direction side) {
        return capabilityValidator.test(side);
    }

    @Override
    public void markAsChanged() {
        machine.markAsChanged();
    }

    public MachineRenderState getRenderState() {
        return getMachine().getRenderState();
    }

    public void setRenderState(MachineRenderState state) {
        getMachine().setRenderState(state);
    }

    public void scheduleRenderUpdate() {
        machine.scheduleRenderUpdate();
    }

    public void onMachineLoad() {}

    public void onMachineUnload() {}

    public void onMachineNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {}
}
