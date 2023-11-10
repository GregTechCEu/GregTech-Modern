package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.editor.EditableUI;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Mth;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote TieredMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TieredEnergyMachine extends TieredMachine implements ITieredMachine, IExplosionMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(TieredEnergyMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);
    @Persisted @DescSynced
    public final NotifiableEnergyContainer energyContainer;
    protected TickableSubscription explosionSubs;
    protected ISubscription energyListener;

    public TieredEnergyMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier);
        energyContainer = createEnergyContainer(args);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        long tierVoltage = GTValues.V[tier];
        if (isEnergyEmitter()) {
            return NotifiableEnergyContainer.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else return NotifiableEnergyContainer.receiverContainer(this,
                tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // if machine need do check explosion conditions
        if (!isRemote() && ConfigHolder.INSTANCE.machines.doTerrainExplosion && shouldWeatherOrTerrainExplosion()) {
            energyListener = energyContainer.addChangedListener(this::updateExplosionSubscription);
            updateExplosionSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energyListener != null) {
            energyListener.unsubscribe();
            energyListener = null;
        }
    }

    //////////////////////////////////////
    //********     Explosion    ********//
    //////////////////////////////////////

    protected void updateExplosionSubscription() {
        if (ConfigHolder.INSTANCE.machines.doTerrainExplosion && shouldWeatherOrTerrainExplosion() && energyContainer.getEnergyStored() > 0) {
            explosionSubs = subscribeServerTick(explosionSubs, this::checkExplosion);
        } else if (explosionSubs != null) {
            explosionSubs.unsubscribe();
            explosionSubs = null;
        }
    }

    protected void checkExplosion() {
        checkWeatherOrTerrainExplosion(tier, tier * 10);
        updateExplosionSubscription();
    }

    //////////////////////////////////////
    //**********     MISC    ***********//
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

    /**
     * Create an energy bar widget.
     */
    protected static EditableUI<ProgressWidget, TieredEnergyMachine> createEnergyBar() {
        return new EditableUI<>("energy_container", ProgressWidget.class, () -> {
            var progressBar = new ProgressWidget(ProgressWidget.JEIProgress, 0, 0, 18, 60,
                    new ProgressTexture(IGuiTexture.EMPTY, GuiTextures.ENERGY_BAR_BASE));
            progressBar.setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP);
            progressBar.setBackground(GuiTextures.ENERGY_BAR_BACKGROUND);
            return progressBar;
        }, (progressBar, machine) -> {
            progressBar.setProgressSupplier(() -> machine.energyContainer.getEnergyStored() * 1d / machine.energyContainer.getEnergyCapacity());
        });
    }

}
