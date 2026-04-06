package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import lombok.Setter;

public abstract class DetectorCover extends CoverBehavior implements IControllable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(DetectorCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted
    @Getter
    @Setter
    protected boolean isWorkingEnabled = true;
    protected TickableSubscription subscription;

    @Persisted
    @DescSynced
    @Getter
    @Setter
    private boolean isInverted;

    public DetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    protected abstract void update();

    private void toggleInvertedWithNotification() {
        setInverted(!isInverted());

        if (!this.coverHolder.isRemote()) {
            this.coverHolder.notifyBlockUpdate();
            this.coverHolder.markDirty();
        }
    }

    @Override
    public ItemInteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, ItemStack held,
                                                    BlockHitResult hitResult) {
        ItemInteractionResult superResult = super.onScrewdriverClick(playerIn, hand, held, hitResult);
        if (superResult.consumesAction()) {
            return superResult;
        }
        if (!held.canPerformAction(GTItemAbilities.SCREWDRIVER_CONFIGURE)) {
            return ItemInteractionResult.FAIL;
        }

        if (!this.coverHolder.isRemote()) {
            toggleInvertedWithNotification();

            String translationKey = isInverted() ? "cover.detector_base.message_inverted_state" :
                    "cover.detector_base.message_normal_state";
            playerIn.sendSystemMessage(Component.translatable(translationKey));
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public boolean canPipePassThrough() {
        return false;
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putBoolean("inverted", isInverted);
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setInverted(tag.getBoolean("inverted"));
        super.pasteConfig(player, tag);
    }
}
