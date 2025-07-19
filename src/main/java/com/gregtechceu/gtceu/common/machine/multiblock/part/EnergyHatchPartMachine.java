package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.syncdata.annotations.SaveField;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnergyHatchPartMachine extends TieredIOPartMachine implements IExplosionMachine {

    @SaveField
    public final NotifiableEnergyContainer energyContainer;
    protected TickableSubscription explosionSubs;
    @Nullable
    protected ISubscription energyListener;
    @Getter
    protected int amperage;

    public EnergyHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int amperage, Object... args) {
        super(holder, tier, io);
        this.amperage = amperage;
        this.energyContainer = createEnergyContainer(args);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        NotifiableEnergyContainer container;
        if (io == IO.OUT) {
            container = NotifiableEnergyContainer.emitterContainer(this, GTValues.V[tier] * 64L * amperage,
                    GTValues.V[tier], amperage);
            container.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        } else {
            container = NotifiableEnergyContainer.receiverContainer(this, GTValues.V[tier] * 16L * amperage,
                    GTValues.V[tier], amperage);
            container.setSideInputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        }
        return container;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // if machine need do check explosion conditions
        if (ConfigHolder.INSTANCE.machines.shouldWeatherOrTerrainExplosion && shouldWeatherOrTerrainExplosion()) {
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
    // ******** Explosion ********//
    //////////////////////////////////////

    protected void updateExplosionSubscription() {
        if (ConfigHolder.INSTANCE.machines.shouldWeatherOrTerrainExplosion && shouldWeatherOrTerrainExplosion() &&
                energyContainer.getEnergyStored() > 0) {
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
    // ********** Misc **********//
    //////////////////////////////////////

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    public static long getHatchEnergyCapacity(int tier, int amperage) {
        return GTValues.V[tier] * 64L * amperage;
    }
}
