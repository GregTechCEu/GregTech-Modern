package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IPassthroughHatch;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;

public class DiodePartMachine extends TieredIOPartMachine implements IPassthroughHatch {

    public static int MAX_AMPS = 16;

    @DescSynced @Persisted
    protected NotifiableEnergyContainer energyContainer;

    @DescSynced @Persisted(key = "amp_mode")
    private int amps;

    public DiodePartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH);
        amps = 1;
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = new NotifiableEnergyContainer(this, tierVoltage * 16, tierVoltage, amps, tierVoltage, amps);
        this.traits.remove(energyContainer);
        reinitializeEnergyContainer();
    }

//    @Override
//    public int getActualComparatorValue() {
//        long energyStored = energyContainer.getEnergyStored();
//        long energyCapacity = energyContainer.getEnergyCapacity();
//        float f = energyCapacity == 0L ? 0.0f : energyStored / (energyCapacity * 1.0f);
//        return MathHelper.floor(f * 14.0f) + (energyStored > 0 ? 1 : 0);
//    }

    private void setAmpMode() {
        amps = amps == getMaxAmperage() ? 1 : amps << 1;
        if (!getLevel().isClientSide) {
            reinitializeEnergyContainer();
            notifyBlockUpdate();
            markDirty();
        }
    }

    /** Change this value (or override) to make the Diode able to handle more amps. Must be a power of 2 */
    protected int getMaxAmperage() {
        return MAX_AMPS;
    }

    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer.resetBasicInfo(tierVoltage * 16, tierVoltage, amps, tierVoltage, amps);
        this.energyContainer.setSideInputCondition(s -> s != getFrontFacing());
        this.energyContainer.setSideOutputCondition(s -> s == getFrontFacing());
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        return true;
    }

    @Override
    protected InteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (getLevel().isClientSide) {
            scheduleRenderUpdate();
            return InteractionResult.CONSUME;
        }
        setAmpMode();
        playerIn.sendSystemMessage(Component.translatable("gtceu.machine.diode.message", amps));
        return InteractionResult.CONSUME;
    }

    @Nonnull
    @Override
    public Class<IEnergyContainer> getPassthroughType() {
        return IEnergyContainer.class;
    }
}
