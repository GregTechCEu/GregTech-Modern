package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.SyncDataHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public abstract class MachineTrait implements ISyncManaged {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    public abstract MachineTraitType<?> getTraitType();

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

    public void onMachineNeighborChange(Block block, BlockPos fromPos, boolean isMoving) {}
}
