package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class ShutterCover extends CoverBehavior implements IControllable {

    @SaveField
    @Getter
    @Setter
    private boolean workingEnabled = true;

    public ShutterCover(CoverDefinition definition, ICoverable coverableView,
                        Direction attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        return InteractionResult.FAIL;
    }

    @Override
    public boolean canPipePassThrough() {
        return !workingEnabled;
    }

    @Override
    public InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        if (!context.getItemInHand().canPerformAction(GTItemAbilities.MALLET_PAUSE)) {
            return InteractionResult.PASS;
        }
        this.workingEnabled = !this.workingEnabled;
        if (!coverHolder.isRemote()) {
            context.getPlayer().sendSystemMessage(Component.translatable(isWorkingEnabled() ?
                    "cover.shutter.message.enabled" : "cover.shutter.message.disabled"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(IItemHandlerModifiable defaultValue) {
        return isWorkingEnabled() ? null : super.getItemHandlerCap(defaultValue);
    }

    @Override
    public @Nullable IFluidHandlerModifiable getFluidHandlerCap(IFluidHandlerModifiable defaultValue) {
        return isWorkingEnabled() ? null : super.getFluidHandlerCap(defaultValue);
    }
}
