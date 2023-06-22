package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.capability.PlatformEnergyCompat;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.trait.ConverterTrait;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class ConverterMachine extends TieredEnergyMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ConverterMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced
    public final ConverterTrait converterTrait;

    protected final int amps;

    public ConverterMachine(IMachineBlockEntity holder, int tier, int amps, Object... args) {
        super(holder, tier, args);
        this.amps = amps;
        this.converterTrait = initializeTrait(amps);
        this.energyContainer = createEnergyContainer();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        if (converterTrait == null) return null;
        return converterTrait.getEnergyEUContainer();
    }

    protected ConverterTrait initializeTrait(int amps) {
        return new ConverterTrait(this, amps, true);
    }

    @Override
    public InteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, Direction facing, BlockHitResult hitResult) {
        if (getLevel().isClientSide) {
            scheduleRenderUpdate();
            return InteractionResult.CONSUME;
        }
        if (converterTrait.isFeToEu()) {
            setFeToEu(false);
            playerIn.sendSystemMessage(Component.translatable("gtceu.machine.energy_converter.message_conversion_eu",
                    converterTrait.getBaseAmps(), converterTrait.getVoltage(),
                    PlatformEnergyCompat.toNative(converterTrait.getVoltage() * converterTrait.getBaseAmps(), PlatformEnergyCompat.ratio(false))));
        } else {
            setFeToEu(true);
            playerIn.sendSystemMessage(Component.translatable("gtceu.machine.energy_converter.message_conversion_native",
                    PlatformEnergyCompat.toNative(converterTrait.getVoltage() * converterTrait.getBaseAmps(), PlatformEnergyCompat.ratio(true)),
                    converterTrait.getBaseAmps(), converterTrait.getVoltage()));
        }
        return InteractionResult.CONSUME;
    }

    public void setFeToEu(boolean feToEu) {
        converterTrait.setFeToEu(feToEu);
        if (!getLevel().isClientSide) {
            notifyBlockUpdate();
            markDirty();
        }
    }

    public boolean isFeToEu() {
        return converterTrait.isFeToEu();
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        return true;
    }

//    @Override
//    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
//        tooltip.add(Component.translatable("gtceu.tool_action.screwdriver.access_covers"));
//        tooltip.add(Component.translatable("gtceu.tool_action.wrench.set_facing"));
//        tooltip.add(Component.translatable("gtceu.tool_action.soft_mallet.toggle_mode"));
//        super.addToolUsages(stack, world, tooltip, advanced);
//    }


    @Override
    public void setFrontFacing(Direction facing) {
        super.setFrontFacing(facing);
        this.converterTrait.onFrontFacingSet(facing);
    }

    @Override
    public ResourceTexture sideTips(Player player, GTToolType toolType, Direction side) {
        if (toolType == GTToolType.SOFT_MALLET) {
            return this.isFeToEu() ? GuiTextures.TOOL_SWITCH_CONVERTER_NATIVE : GuiTextures.TOOL_SWITCH_CONVERTER_EU;
        }
        return super.sideTips(player, toolType, side);
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return converterTrait.getBaseAmps();
    }

    @Override
    protected boolean isEnergyEmitter() {
        return converterTrait.isFeToEu();
    }
}
