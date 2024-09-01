package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.common.machine.trait.ConverterTrait;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConverterMachine extends TieredMachine implements IExplosionMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ConverterMachine.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    protected final ConverterTrait converterTrait;

    public ConverterMachine(IMachineBlockEntity holder, int tier, int amps) {
        super(holder, tier);
        this.converterTrait = initializeTrait(amps);
    }

    protected ConverterTrait initializeTrait(int amps) {
        return new ConverterTrait(this, amps, true);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    // ****** Interaction ******//
    //////////////////////////////////////
    @Override
    public ItemInteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, Direction facing,
                                                   BlockHitResult hitResult) {
        if (!isRemote()) {
            if (getConverterTrait().isFeToEu()) {
                setFeToEu(false);
                playerIn.sendSystemMessage(
                        Component.translatable("gtceu.machine.energy_converter.message_conversion_eu",
                                getConverterTrait().getAmps(), getConverterTrait().getVoltage(),
                                FeCompat.toFeLong(
                                        getConverterTrait().getVoltage() * getConverterTrait().getAmps(),
                                        FeCompat.ratio(false))));
            } else {
                setFeToEu(true);
                playerIn.sendSystemMessage(
                        Component.translatable("gtceu.machine.energy_converter.message_conversion_native",
                                FeCompat.toFeLong(
                                        getConverterTrait().getVoltage() * getConverterTrait().getAmps(),
                                        FeCompat.ratio(true)),
                                getConverterTrait().getAmps(), getConverterTrait().getVoltage()));
            }
        }
        return ItemInteractionResult.CONSUME;
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
    public ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                    Direction side) {
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            return this.isFeToEu() ? GuiTextures.TOOL_SWITCH_CONVERTER_NATIVE : GuiTextures.TOOL_SWITCH_CONVERTER_EU;
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }
}
