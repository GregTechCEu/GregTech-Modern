package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;

import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRenderer;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererBuilder;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
    protected CoverRenderer buildRenderer() {
        return new CoverRendererBuilder(GTCEu.id("block/cover/overlay_shutter"), null).build();
    }

    @Override
    public InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.FAIL;
    }

    @Override
    public boolean canPipePassThrough() {
        return !workingEnabled;
    }

    @Override
    public boolean forcePipeRenderConnection() {
        return false;
    }

    @Override
    public InteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        this.workingEnabled = !this.workingEnabled;
        if (!playerIn.level().isClientSide) {
            playerIn.sendSystemMessage(Component.translatable(isWorkingEnabled() ?
                    "cover.shutter.message.enabled" : "cover.shutter.message.disabled"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
