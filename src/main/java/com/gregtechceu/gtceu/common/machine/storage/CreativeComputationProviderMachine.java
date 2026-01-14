package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.Rectangle;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeComputationProviderMachine extends MetaMachine
                                                implements IMuiMachine, IOpticalComputationProvider {

    @SaveField
    private int maxCWUt;
    private int lastRequestedCWUt;
    private int requestedCWUPerSec;
    @SaveField
    @Getter
    private boolean active;
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
        if (active) {
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
                           int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        int requestedCWUt = active ? Math.min(cwut, maxCWUt) : 0;
        if (!simulate) {
            this.requestedCWUPerSec += requestedCWUt;
        }
        return requestedCWUt;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return active ? maxCWUt : 0;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return true;
    }

    public void setActive(boolean active) {
        this.active = active;
        updateComputationSubscription();
    }

    public void setMaxCWUt(int maxCWUt) {
        this.maxCWUt = maxCWUt;
        syncDataHolder.markClientSyncFieldDirty("maxCWUt");
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new ModularPanel(this.getDefinition().getName())
                .height(100)
                .child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176))
                .child(Flow.column()
                        .padding(10)
                        .childPadding(5)
                        .child(Flow.row()
                                .alignX(0)
                                .childPadding(5)
                                .coverChildren()
                                .child(new TextWidget<>(IKey.lang("gtceu.creative.computation.max_usage")))
                                .child(new TextFieldWidget()
                                        .setNumbers(0, Integer.MAX_VALUE)
                                        .value(new IntSyncValue(this::getMaxCWUt, this::setMaxCWUt))))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.95f).marginBottom(4).marginTop(4))
                        .child(Flow.row()
                                .alignX(0)
                                .childPadding(5)
                                .coverChildren()
                                .child(new TextWidget<>(IKey.lang("gtceu.creative.computation.average",
                                        () -> new Object[] { lastRequestedCWUt }))))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.95f).marginBottom(4).marginTop(4))
                        .child(Flow.row()
                                .alignX(0)
                                .childPadding(5)
                                .coverChildren()
                                .child(new ToggleButton()
                                        .value(new BooleanSyncValue(this::isActive, this::setActive))
                                        .selectedBackground(GTGuiTextures.BUTTON_POWER[1])
                                        .background(GTGuiTextures.BUTTON_POWER[0])
                                        .tooltipAutoUpdate(true)
                                        .tooltipBuilder((r) -> r.addLine(IKey.lang(() -> this.isActive() ?
                                                "behaviour.soft_hammer.enabled" :
                                                "behaviour.soft_hammer.disabled"))))
                                .child(new TextWidget<>(IKey
                                        .lang(() -> "gtceu.creative.activity." + (this.isActive() ? "on" : "off"))))));
    }
}
