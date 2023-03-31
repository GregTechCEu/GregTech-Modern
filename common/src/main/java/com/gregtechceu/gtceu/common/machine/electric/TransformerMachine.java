package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/10
 * @implNote TransformerMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TransformerMachine extends TieredEnergyMachine implements IControllable {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(TransformerMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);
    @Persisted @DescSynced @Getter
    private boolean isTransformUp;
    @Persisted @Getter @Setter
    private boolean isWorkingEnabled;

    public TransformerMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);
        this.isWorkingEnabled = true;
        if (isRemote()) {
            addSyncUpdateListener("isTransformUp", this::onTransformUpdated);
        }
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private void onTransformUpdated(String fieldName, boolean newValue, boolean oldValue) {
        scheduleRenderUpdate();
        updateEnergyContainer(newValue);
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        NotifiableEnergyContainer energyContainer;
        long tierVoltage = GTValues.V[getTier()];
        energyContainer = new NotifiableEnergyContainer(this, tierVoltage * 8L, tierVoltage * 4, 1, tierVoltage, 4);
        energyContainer.setSideInputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
        energyContainer.setSideOutputCondition(s -> s != getFrontFacing() && isWorkingEnabled());
        return energyContainer;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateEnergyContainer(isTransformUp);
    }

    public void updateEnergyContainer(boolean isTransformUp) {
        long tierVoltage = GTValues.V[getTier()];
        if (isTransformUp) {
            //storage = 1 amp high; input = tier / 4; amperage = 4; output = tier; amperage = 1
            this.energyContainer.resetBasicInfo(tierVoltage * 8L, tierVoltage, 4, tierVoltage * 4, 1);
            energyContainer.setSideInputCondition(s -> s != getFrontFacing() && isWorkingEnabled());
            energyContainer.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
        } else {
            //storage = 1 amp high; input = tier; amperage = 1; output = tier / 4; amperage = 4
            this.energyContainer.resetBasicInfo(tierVoltage * 8L, tierVoltage * 4, 1, tierVoltage, 4);
            energyContainer.setSideInputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            energyContainer.setSideOutputCondition(s -> s != getFrontFacing() && isWorkingEnabled());
        }
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) { // frontTexture
            return GTValues.VC[getTier() + 1];
        } else if (index == 3) { // otherTexture
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    //////////////////////////////////////
    //******     Interaction     *******//
    //////////////////////////////////////

    public void setTransformUp(boolean isTransformUp) {
        if (this.isTransformUp != isTransformUp && !isRemote()) {
            this.isTransformUp = isTransformUp;
            updateEnergyContainer(isTransformUp);
        }
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!isRemote()) {
            if (isTransformUp()) {
                setTransformUp(false);
                playerIn.sendSystemMessage(Component.translatable("gtceu.machine.transformer.message_transform_down",
                        energyContainer.getInputVoltage(), energyContainer.getInputAmperage(), energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));
            } else {
                setTransformUp(true);
                playerIn.sendSystemMessage(Component.translatable("gtceu.machine.transformer.message_transform_up",
                        energyContainer.getInputVoltage(), energyContainer.getInputAmperage(), energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));
            }
        }
        return InteractionResult.CONSUME;
    }

}
