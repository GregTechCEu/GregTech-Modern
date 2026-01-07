package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.SyncDataHolder;

import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.ModelData;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public abstract class MachineTrait implements ISyncManaged {

    public static class TraitType<T extends MachineTrait> {
        public final Class<T> traitCls;
        public final boolean allowMultiplePerMachine;
        public TraitType(Class<T> cls) {
            traitCls = cls;
            allowMultiplePerMachine = true;
        }

        public TraitType(Class<T> cls, boolean allowMultiplePerMachine) {
            traitCls = cls;
            this.allowMultiplePerMachine = allowMultiplePerMachine;
        }

    }
    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    @Getter
    protected final MetaMachine machine;
    @Setter
    protected Predicate<@Nullable Direction> capabilityValidator;

    public MachineTrait(MetaMachine machine) {
        this.machine = machine;
        this.capabilityValidator = side -> true;
        /// Machine should never be null, unless this trait is a recipe handler instantiated outside a machine for
        /// recipe search.
        if (machine != null) machine.attachTrait(this);
    }

    public abstract TraitType<?> getTraitType();

    public final boolean hasCapability(@Nullable Direction side) {
        return capabilityValidator.test(side);
    }

    @Override
    public void markAsChanged() {
        machine.markAsChanged();
    }

    public void onMachineLoad() {}

    public void onMachineUnload() {}

    public void updateModelData(ModelData.Builder builder) {}

    public MachineRenderState getRenderState() {
        return getMachine().getRenderState();
    }

    public void setRenderState(MachineRenderState state) {
        getMachine().setRenderState(state);
    }

    public void scheduleRenderUpdate() {
        machine.scheduleRenderUpdate();
    }
}
