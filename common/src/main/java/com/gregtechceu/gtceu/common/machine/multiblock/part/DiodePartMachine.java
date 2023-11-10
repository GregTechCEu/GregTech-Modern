package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DiodePartMachine extends TieredIOPartMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(DiodePartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    public static int MAX_AMPS = 16;

    @Persisted
    protected NotifiableEnergyContainer energyContainer;

    @Persisted(key = "amp_mode")
    private int amps;

    public DiodePartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH);
        amps = 1;
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = new NotifiableEnergyContainer(this, tierVoltage * 16, tierVoltage, amps, tierVoltage, amps);
        reinitializeEnergyContainer();
    }

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

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
