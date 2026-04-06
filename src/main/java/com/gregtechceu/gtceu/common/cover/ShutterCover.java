package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShutterCover extends CoverBehavior implements IControllable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ShutterCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    @Setter
    private boolean workingEnabled = true;

    public ShutterCover(@NotNull CoverDefinition definition, @NotNull ICoverable coverableView,
                        @NotNull Direction attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public ItemInteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, ItemStack held,
                                                    BlockHitResult hitResult) {
        return ItemInteractionResult.FAIL;
    }

    @Override
    public boolean canPipePassThrough() {
        return !workingEnabled;
    }

    @Override
    public ItemInteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, ItemStack held,
                                                   BlockHitResult hitResult) {
        if (!held.canPerformAction(GTItemAbilities.MALLET_PAUSE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        this.workingEnabled = !this.workingEnabled;
        if (!playerIn.level().isClientSide) {
            playerIn.sendSystemMessage(Component.translatable(isWorkingEnabled() ?
                    "cover.shutter.message.enabled" : "cover.shutter.message.disabled"));
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(IItemHandlerModifiable defaultValue) {
        return isWorkingEnabled() ? null : super.getItemHandlerCap(defaultValue);
    }

    @Override
    public @Nullable IFluidHandlerModifiable getFluidHandlerCap(IFluidHandlerModifiable defaultValue) {
        return isWorkingEnabled() ? null : super.getFluidHandlerCap(defaultValue);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
