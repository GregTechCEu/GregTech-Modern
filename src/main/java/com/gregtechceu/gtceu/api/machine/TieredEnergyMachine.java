package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.EnvironmentalExplosionTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Mth;

import lombok.Getter;

import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TieredEnergyMachine extends TieredMachine implements ITieredMachine {

    @SaveField
    @SyncToClient
    public final NotifiableEnergyContainer energyContainer;
    @Getter
    protected final EnvironmentalExplosionTrait environmentalExplosionTrait;

    public TieredEnergyMachine(BlockEntityCreationInfo info, int tier,
                               Function<TieredEnergyMachine, NotifiableEnergyContainer> energyContainerSupplier) {
        super(info, tier);
        energyContainer = energyContainerSupplier.apply(this);
        environmentalExplosionTrait = new EnvironmentalExplosionTrait(this, tier, tier * 10,
                () -> energyContainer.getEnergyStored() > 0);
    }

    public TieredEnergyMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);

        long tierVoltage = GTValues.V[tier];
        if (isEnergyEmitter()) {
            energyContainer = NotifiableEnergyContainer.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else {
            energyContainer = NotifiableEnergyContainer.receiverContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        }
        environmentalExplosionTrait = new EnvironmentalExplosionTrait(this, tier, tier * 10,
                () -> energyContainer.getEnergyStored() > 0);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////
    @Override
    public int getAnalogOutputSignal() {
        long energyStored = energyContainer.getEnergyStored();
        long energyCapacity = energyContainer.getEnergyCapacity();
        float f = energyCapacity == 0L ? 0.0f : energyStored / (energyCapacity * 1.0f);
        return Mth.floor(f * 14.0f) + (energyStored > 0 ? 1 : 0);
    }

    /**
     * Determines max input or output amperage used by this meta tile entity
     * if emitter, it determines size of energy packets it will emit at once
     * if receiver, it determines max input energy per request
     *
     * @return max amperage received or emitted by this machine
     */
    protected long getMaxInputOutputAmperage() {
        return 1L;
    }

    /**
     * Determines if this meta tile entity is in energy receiver or emitter mode
     *
     * @return true if machine emits energy to network, false it it accepts energy from network
     */
    protected boolean isEnergyEmitter() {
        return false;
    }
}
