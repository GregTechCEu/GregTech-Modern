package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TransformerMachine extends TieredEnergyMachine implements IControllable {

    public static final BooleanProperty TRANSFORM_UP_PROPERTY = GTMachineModelProperties.IS_TRANSFORM_UP;

    @SaveField
    @SyncToClient
    @Getter
    private boolean isTransformUp;
    @SaveField
    @Getter
    @Setter
    private boolean isWorkingEnabled;
    @Getter
    private final int baseAmp;

    public TransformerMachine(BlockEntityCreationInfo info, int tier, int amps) {
        super(info, tier, (TieredEnergyMachine machine) -> {
            NotifiableEnergyContainer energyContainer;
            long tierVoltage = GTValues.V[machine.getTier()];
            energyContainer = new NotifiableEnergyContainer(machine, tierVoltage * 8L, tierVoltage * 4, amps,
                    tierVoltage,
                    4L * amps);
            return energyContainer;
        });

        energyContainer.setSideInputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
        energyContainer.setSideOutputCondition(s -> s != getFrontFacing() && isWorkingEnabled());
        this.isWorkingEnabled = true;
        this.baseAmp = amps;
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @SuppressWarnings("unused")
    @ClientFieldChangeListener(fieldName = "isTransformUp")
    private void onTransformUpdated() {
        updateEnergyContainer(isTransformUp);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateEnergyContainer(isTransformUp);
    }

    public void updateEnergyContainer(boolean isTransformUp) {
        long tierVoltage = GTValues.V[getTier()];
        int lowAmperage = baseAmp * 4;
        if (isTransformUp) {
            // storage = n amp high; input = tier / 4; amperage = 4n; output = tier; amperage = n
            this.energyContainer.resetBasicInfo(tierVoltage * 8L * lowAmperage, tierVoltage, lowAmperage,
                    tierVoltage * 4, baseAmp);
            energyContainer.setSideInputCondition(s -> s != getFrontFacing() && isWorkingEnabled());
            energyContainer.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
        } else {
            // storage = n amp high; input = tier; amperage = n; output = tier / 4; amperage = 4n
            this.energyContainer.resetBasicInfo(tierVoltage * 8L * lowAmperage, tierVoltage * 4, baseAmp, tierVoltage,
                    lowAmperage);
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
    // ****** Interaction *******//
    //////////////////////////////////////

    public void setTransformUp(boolean isTransformUp) {
        if (this.isTransformUp != isTransformUp && !isRemote()) {
            this.isTransformUp = isTransformUp;
            syncDataHolder.markClientSyncFieldDirty("isTransformUp");
            updateEnergyContainer(isTransformUp);
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_TRANSFORM_UP, isTransformUp));
        }
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (!isRemote()) {
            setTransformUp(!isTransformUp());
            playerIn.sendSystemMessage(Component.translatable(
                    isTransformUp() ? "gtceu.machine.transformer.message_transform_up" :
                            "gtceu.machine.transformer.message_transform_down",
                    energyContainer.getInputVoltage(), energyContainer.getInputAmperage(),
                    energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));
        }
        return InteractionResult.CONSUME;
    }
}
