package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DetectorCover extends CoverBehavior implements IControllable {

    @SaveField
    @Getter
    @Setter
    protected boolean isWorkingEnabled = true;
    protected TickableSubscription subscription;

    @SaveField
    @SyncToClient
    @Getter
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

    public void setInverted(boolean inverted) {
        isInverted = inverted;
        syncDataHolder.markClientSyncFieldDirty("isInverted");
    }

    protected abstract void update();

    private void toggleInvertedWithNotification() {
        setInverted(!isInverted());

        if (!this.coverHolder.isRemote()) {
            this.coverHolder.notifyBlockUpdate();
        }
    }

    @Override
    public InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult superResult = super.onScrewdriverClick(playerIn, hand, hitResult);
        if (superResult != InteractionResult.PASS) {
            return superResult;
        }

        if (!this.coverHolder.isRemote()) {
            toggleInvertedWithNotification();

            String translationKey = isInverted() ? "cover.detector_base.message_inverted_state" :
                    "cover.detector_base.message_normal_state";
            playerIn.sendSystemMessage(Component.translatable(translationKey));
        }

        return InteractionResult.SUCCESS;
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
