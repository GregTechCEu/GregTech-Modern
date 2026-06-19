package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IWorkLogicMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class WorkLogic extends MachineTrait implements IWorkable, IFancyTooltip {

    public enum Status implements StringRepresentable {

        IDLE("idle"),
        WORKING("working"),
        WAITING("waiting"),
        SUSPEND("suspend");

        @Getter
        private final String serializedName;

        Status(String name) {
            this.serializedName = name;
        }
    }

    public static final EnumProperty<WorkLogic.Status> STATUS_PROPERTY = GTMachineModelProperties.RECIPE_LOGIC_STATUS;

    @Getter
    public final IWorkLogicMachine workMachine;

    @Getter
    @Persisted
    @DescSynced
    protected Status status = Status.IDLE;

    @Persisted
    @DescSynced
    protected boolean workingEnabled = true;

    @Getter
    @Setter
    @Persisted
    protected boolean suspendAfterFinish = false;

    @Getter
    @Nullable
    @Persisted
    @DescSynced
    protected Component waitingReason = null;

    protected TickableSubscription subscription;

    public WorkLogic(IWorkLogicMachine machine) {
        super(machine.self());
        this.workMachine = machine;
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        updateTickSubscription();
    }

    public void updateTickSubscription() {
        if (isSuspend()) {
            unsubscribeTick();
        } else {
            subscription = getMachine().subscribeServerTick(subscription, this::serverTick);
        }
    }

    protected void unsubscribeTick() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    public void serverTick() {
        if (!isSuspend()) {
            onServerTick();
        }
        if ((isSuspend() || (isIdle() && !workMachine.keepSubscribing())) && subscription != null) {
            unsubscribeTick();
        }
    }

    protected void onServerTick() {}

    public void setStatus(Status status) {
        if (this.status != status) {
            Status oldStatus = this.status;
            this.status = status;
            workMachine.notifyWorkStatusChanged(oldStatus, status);
            scheduleRenderUpdate();
            updateTickSubscription();
            if (this.status != Status.WAITING) {
                waitingReason = null;
            }
        }
    }

    public void setWaiting(@Nullable Component reason) {
        setStatus(Status.WAITING);
        waitingReason = reason;
        onWaiting();
    }

    protected void onWaiting() {}

    public boolean isWorking() {
        return status == Status.WORKING;
    }

    public boolean isIdle() {
        return status == Status.IDLE;
    }

    public boolean isWaiting() {
        return status == Status.WAITING;
    }

    public boolean isSuspend() {
        return status == Status.SUSPEND;
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.workingEnabled != isWorkingAllowed) {
            this.workingEnabled = isWorkingAllowed;
            if (!isWorkingAllowed) {
                setStatus(Status.SUSPEND);
            } else if (isSuspend()) {
                setStatus(Status.IDLE);
            }
            workMachine.notifyWorkingEnabledChanged(!workingEnabled, isWorkingAllowed);
            updateTickSubscription();
        }
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public int getMaxProgress() {
        return 0;
    }

    public double getProgressPercent() {
        int maxProgress = getMaxProgress();
        return maxProgress == 0 ? 0.0 : getProgress() / (maxProgress * 1.0);
    }

    @Override
    public boolean isActive() {
        return isWorking() || isWaiting();
    }

    public boolean hasCustomProgressLine() {
        return false;
    }

    public @Nullable Component getCustomProgressLine() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public void updateSound() {}

    @Override
    public IGuiTexture getFancyTooltipIcon() {
        if (showFancyTooltip()) {
            return GuiTextures.INSUFFICIENT_INPUT;
        }
        return IGuiTexture.EMPTY;
    }

    @Override
    public List<Component> getFancyTooltip() {
        if (isWaiting() && waitingReason != null) {
            return List.of(waitingReason);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean showFancyTooltip() {
        return waitingReason != null;
    }

}
