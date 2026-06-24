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

public class WorkLogic extends MachineTrait implements IFancyTooltip {

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
    private Status status = Status.IDLE;

    @Getter
    @Setter
    @Persisted
    protected boolean suspendAfterFinish = false;

    @Getter
    @Nullable
    @Persisted
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
        if (isSuspend() || !workMachine.isWorkLogicAvailable()) {
            unsubscribeTick();
        } else {
            subscription = getMachine().subscribeServerTick(subscription, this::serverTick);
        }
    }

    public void unsubscribeTick() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    public void serverTick() {
        if (!isSuspend()) {
            workMachine.serverRunningTick();
        }
        if ((isSuspend() || (isIdle() && !workMachine.keepSubscribing()))) {
            unsubscribeTick();
        }
    }

    public void setStatus(Status status) {
        if (this.status != status) {
            Status oldStatus = this.status;
            this.status = status;
            workMachine.notifyWorkStatusChanged(oldStatus, status);
            if (this.status != Status.WAITING) {
                waitingReason = null;
            }
            updateTickSubscription();
        }
    }

    public void setWaiting(@Nullable Component reason) {
        setStatus(Status.WAITING);
        waitingReason = reason;
        onWaiting();
    }

    protected void onWaiting() {}

    public final boolean isWorking() {
        return status == Status.WORKING;
    }

    public final boolean isIdle() {
        return status == Status.IDLE;
    }

    public final boolean isWaiting() {
        return status == Status.WAITING;
    }

    public final boolean isSuspend() {
        return status == Status.SUSPEND;
    }

    public boolean isWorkingEnabled() {
        return !isSuspend();
    }

    public void setWorkingEnabled(boolean isWorkingAllowed) {
        setStatus(isWorkingAllowed ? Status.IDLE : Status.SUSPEND);
        workMachine.notifyWorkingEnabledChanged(!isWorkingAllowed, isWorkingAllowed);
        updateTickSubscription();
    }

    public boolean isActive() {
        return isWorking() || isWaiting();
    }

    public void reset() {
        if (!isSuspend()) {
            setStatus(Status.IDLE);
        }
        updateTickSubscription();
    }

    @OnlyIn(Dist.CLIENT)
    public void updateSound() {
        //TODO : add sound for non-recipe machine
    }

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
