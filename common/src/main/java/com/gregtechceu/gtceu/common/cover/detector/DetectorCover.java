package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DetectorCover extends CoverBehavior implements IControllable {

    protected IWorkable workable;

    @Persisted @Getter @Setter
    protected boolean isWorkingEnabled = true;
    protected TickableSubscription subscription;

    @Getter
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(DetectorCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced @Getter @Setter
    private boolean isInverted;

    public DetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);

        workable = GTCapabilityHelper.getWorkable(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }

    @Override
    public boolean canAttach() {
        return workable != null;
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
    public InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        if (this.coverHolder.isRemote()) {
            return InteractionResult.SUCCESS;
        }

        String translationKey = isInverted()
                ? "gregtech.cover.detector_base.message_inverted_state"
                : "gregtech.cover.detector_base.message_normal_state";
        playerIn.sendSystemMessage(Component.translatable(translationKey));

        toggleInvertedWithNotification();

        return InteractionResult.SUCCESS;
    }
}

