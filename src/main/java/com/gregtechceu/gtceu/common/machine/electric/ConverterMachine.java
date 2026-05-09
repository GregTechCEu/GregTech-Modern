package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.trait.ConverterTrait;
import com.gregtechceu.gtceu.common.machine.trait.EnvironmentalExplosionTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.drawable.UITexture;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConverterMachine extends TieredMachine {

    @SaveField
    @SyncToClient
    public final NotifiableEnergyContainer energyContainer;

    @Getter
    protected final EnvironmentalExplosionTrait environmentalExplosionTrait;

    public ConverterMachine(BlockEntityCreationInfo info, int tier, int amps) {
        super(info, tier);

        energyContainer = attachTrait(new ConverterTrait(this, tier, amps));
        environmentalExplosionTrait = attachTrait(new EnvironmentalExplosionTrait(tier, tier * 10,
                () -> energyContainer.getEnergyStored() > 0));

    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    public ConverterTrait getConverterTrait() {
        return (ConverterTrait) energyContainer;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    //////////////////////////////////////
    // ****** Interaction ******//
    //////////////////////////////////////
    @Override
    public InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            if (getConverterTrait().isFeToEu()) {
                setFeToEu(false);
                context.getPlayer().sendSystemMessage(
                        Component.translatable("gtceu.machine.energy_converter.message_conversion_eu",
                                getConverterTrait().getAmps(), getConverterTrait().getVoltage(),
                                FeCompat.toFeLong(
                                        getConverterTrait().getVoltage() * getConverterTrait().getAmps(),
                                        FeCompat.ratio(false))));
            } else {
                setFeToEu(true);
                context.getPlayer().sendSystemMessage(
                        Component.translatable("gtceu.machine.energy_converter.message_conversion_native",
                                FeCompat.toFeLong(
                                        getConverterTrait().getVoltage() * getConverterTrait().getAmps(),
                                        FeCompat.ratio(true)),
                                getConverterTrait().getAmps(), getConverterTrait().getVoltage()));
            }
        }
        return InteractionResult.CONSUME;
    }

    public void setFeToEu(boolean feToEu) {
        getConverterTrait().setFeToEu(feToEu);
    }

    public boolean isFeToEu() {
        return getConverterTrait().isFeToEu();
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        return true;
    }

    @Override
    public @Nullable UITexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                        Direction side) {
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            return this.isFeToEu() ? GTGuiTextures.TOOL_SWITCH_CONVERTER_NATIVE :
                    GTGuiTextures.TOOL_SWITCH_CONVERTER_EU;
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }
}
