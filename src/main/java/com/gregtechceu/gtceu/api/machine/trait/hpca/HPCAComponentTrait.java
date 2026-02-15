package com.gregtechceu.gtceu.api.machine.trait.hpca;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class HPCAComponentTrait extends MachineTrait {

    public static final MachineTraitType<HPCAComponentTrait> TYPE = new MachineTraitType<>(HPCAComponentTrait.class);

    @Getter
    private final int upkeepEUt, maxEUt;
    @Getter
    private final boolean canBeDamaged, allowBridging;
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    @Getter
    private boolean isDamaged;

    public HPCAComponentTrait(MetaMachine machine, int upkeepEUt, int maxEUt, boolean canBeDamaged,
                              boolean allowBridging) {
        super(machine);
        this.upkeepEUt = upkeepEUt;
        this.maxEUt = maxEUt;
        this.canBeDamaged = canBeDamaged;
        this.isDamaged = false;
        this.allowBridging = allowBridging;
    }

    @Override
    public MachineTraitType<HPCAComponentTrait> getTraitType() {
        return TYPE;
    }

    public void setDamaged(boolean damaged) {
        if (!canBeDamaged) return;
        if (isDamaged != damaged) {
            isDamaged = damaged;
            syncDataHolder.markClientSyncFieldDirty("damaged");
            MachineRenderState state = getRenderState();
            if (state.hasProperty(GTMachineModelProperties.IS_HPCA_PART_DAMAGED)) {
                setRenderState(state.setValue(GTMachineModelProperties.IS_HPCA_PART_DAMAGED, damaged));
            }
        }
    }

    public void setActive(boolean active) {
        MachineRenderState state = getRenderState();
        if (state.hasProperty(GTMachineModelProperties.IS_ACTIVE)) {
            setRenderState(state.setValue(GTMachineModelProperties.IS_ACTIVE, active));
        }
    }
}
