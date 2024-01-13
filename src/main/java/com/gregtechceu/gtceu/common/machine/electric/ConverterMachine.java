package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.capability.PlatformEnergyCompat;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.trait.ConverterTrait;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConverterMachine extends TieredEnergyMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ConverterMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    public ConverterMachine(IMachineBlockEntity holder, int tier, int amps, Object... args) {
        super(holder, tier, args, amps);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        if (args.length > 0 && args[args.length - 1] instanceof Integer ampsValue) {
            return new ConverterTrait(this, ampsValue);
        }
        throw new IllegalArgumentException("ConverterMachine need args [amps] for initialization");
    }

    public ConverterTrait getConverterTrait() {
        return (ConverterTrait)energyContainer;
    }

    //////////////////////////////////////
    //******      Interaction     ******//
    //////////////////////////////////////
    @Override
    public InteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, Direction facing, BlockHitResult hitResult) {
        if (!isRemote()) {
            if (getConverterTrait().isFeToEu()) {
                setFeToEu(false);
                playerIn.sendSystemMessage(Component.translatable("gtceu.machine.energy_converter.message_conversion_eu",
                        getConverterTrait().getAmps(), getConverterTrait().getVoltage(),
                        PlatformEnergyCompat.toNativeLong(getConverterTrait().getVoltage() * getConverterTrait().getAmps(), PlatformEnergyCompat.ratio(false))));
            } else {
                setFeToEu(true);
                playerIn.sendSystemMessage(Component.translatable("gtceu.machine.energy_converter.message_conversion_native",
                        PlatformEnergyCompat.toNativeLong(getConverterTrait().getVoltage() * getConverterTrait().getAmps(), PlatformEnergyCompat.ratio(true)),
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
    public ResourceTexture sideTips(Player player, Set<GTToolType> toolTypes, Direction side) {
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            return this.isFeToEu() ? GuiTextures.TOOL_SWITCH_CONVERTER_NATIVE : GuiTextures.TOOL_SWITCH_CONVERTER_EU;
        }
        return super.sideTips(player, toolTypes, side);
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return getConverterTrait().getAmps();
    }

    @Override
    protected boolean isEnergyEmitter() {
        return getConverterTrait().isFeToEu();
    }
}
