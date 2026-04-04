package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.MethodsReturnNonnullByDefault;

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeComputationProviderMachine extends MetaMachine
                                                implements IMuiMachine, IOpticalComputationProvider, IControllable {

    @SaveField
    private int maxCWUt;
    private int lastRequestedCWUt;
    private int requestedCWUPerSec;
    @SaveField
    @Getter
    private boolean workingEnabled;
    @Nullable
    private TickableSubscription computationSubs;

    public CreativeComputationProviderMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateComputationSubscription();
    }

    protected void updateComputationSubscription() {
        if (workingEnabled) {
            this.computationSubs = subscribeServerTick(this::updateComputationTick);
        } else if (computationSubs != null) {
            computationSubs.unsubscribe();
            this.computationSubs = null;
            this.lastRequestedCWUt = 0;
            this.requestedCWUPerSec = 0;
        }
    }

    protected void updateComputationTick() {
        if (getOffsetTimer() % 20 == 0) {
            this.lastRequestedCWUt = requestedCWUPerSec / 20;
            this.requestedCWUPerSec = 0;
        }
    }

    @Override
    public int requestCWUt(
                           int cwut, boolean simulate, Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        int requestedCWUt = workingEnabled ? Math.min(cwut, maxCWUt) : 0;
        if (!simulate) {
            this.requestedCWUPerSec += requestedCWUt;
        }
        return requestedCWUt;
    }

    @Override
    public int getMaxCWUt(Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return workingEnabled ? maxCWUt : 0;
    }

    @Override
    public boolean canBridge(Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return true;
    }

    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        updateComputationSubscription();
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.defaultPanelBuilder(this).attachInventory(false);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget
                .child(Flow.column()
                        .padding(10)
                        .childPadding(5)
                        .child(Flow.row()
                                .leftRel(0)
                                .childPadding(5)
                                .coverChildren()
                                .child(new TextWidget<>(IKey.lang("gtceu.creative.computation.max_usage")))
                                .child(new TextFieldWidget()
                                        .setNumbers(0, Integer.MAX_VALUE)
                                        .value(new IntSyncValue(() -> maxCWUt, (v) -> maxCWUt = v))))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.95f).marginBottom(4).marginTop(4))
                        .child(Flow.row()
                                .leftRel(0)
                                .childPadding(5)
                                .coverChildren()
                                .child(new TextWidget<>(IKey.lang("gtceu.creative.computation.average",
                                        () -> new Object[] { lastRequestedCWUt })))));
    }
}
