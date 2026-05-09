package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.trait.EnvironmentalExplosionTrait;

import net.minecraft.util.Mth;

import lombok.Getter;

/**
 * A singleblock tiered machine with an energy container.
 */
public class TieredEnergyMachine extends TieredMachine {

    @SaveField
    @SyncToClient
    public final NotifiableEnergyContainer energyContainer;

    @Getter
    protected final EnvironmentalExplosionTrait environmentalExplosionTrait;

    /**
     * Creates a {@link TieredEnergyMachine} using the given energy container.<br>
     *
     *
     * @param info {@link BlockEntityCreationInfo}
     * @param tier Machine/voltage tier
     * @param energyContainer The enegery container to attach
     * @see NotifiableEnergyContainer
     */
    public TieredEnergyMachine(BlockEntityCreationInfo info, int tier,
                               NotifiableEnergyContainer energyContainer) {
        super(info, tier);
        this.energyContainer = attachTrait(energyContainer);
        environmentalExplosionTrait = attachTrait(new EnvironmentalExplosionTrait(tier, tier * 10,
                () -> energyContainer.getEnergyStored() > 0));
    }

    /**
     * Creates a {@link TieredEnergyMachine} with a default energy container.<br>
     * Defaults to a container with a capacity of 64A @ the given voltage tier, and a max input/output amperage of 1A @ the given voltage tier.
     *
     * @param info {@link BlockEntityCreationInfo}
     * @param tier Machine/voltage tier
     * @param emitsEnergy If the energy container should receive or emit energy.
     * @see NotifiableEnergyContainer
     */
    public TieredEnergyMachine(BlockEntityCreationInfo info, int tier, boolean emitsEnergy) {
        this(info, tier, emitsEnergy ? NotifiableEnergyContainer.emitterContainer(GTValues.V[tier] * 64L, GTValues.V[tier],
                1) : NotifiableEnergyContainer.receiverContainer(GTValues.V[tier] * 64L, GTValues.V[tier],
                1));
    }

    @Override
    public int getAnalogOutputSignal() {
        long energyStored = energyContainer.getEnergyStored();
        long energyCapacity = energyContainer.getEnergyCapacity();
        float f = energyCapacity == 0L ? 0.0f : energyStored / (energyCapacity * 1.0f);
        return Mth.floor(f * 14.0f) + (energyStored > 0 ? 1 : 0);
    }
}
