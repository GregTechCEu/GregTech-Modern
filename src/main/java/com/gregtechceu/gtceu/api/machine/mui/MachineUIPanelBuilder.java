package com.gregtechceu.gtceu.api.machine.mui;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IHasBatterySlot;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.feature.IAttachConfiguratorsTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.widgets.SteamDialWidget;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Color;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

@Accessors(fluent = true)
@Setter
public class MachineUIPanelBuilder {

    /**
     * Should the GregTech logo be drawn in the bottom right corner of the panel.
     */
    private boolean drawGTLogo = false;
    /**
     * The texture to use for the GregTech logo.
     */
    private UITexture gtLogoTexture = GTGuiTextures.GREGTECH_LOGO;
    /**
     * Should the player inventory be attached to the bottom of the panel.
     */
    private boolean attachInventory = true;
    /**
     * Should a fancy title bar be created for this panel.
     */
    private boolean addTitleBar = true;
    /**
     * If machine traits should be allowed to attach configurators to the sides of the panel.
     */
    private boolean addTraitConfigurators = true;
    /**
     * If the default configurators (circuit slot, battery slot, power button) should be added to this machine, provided
     * the machine supports them.
     */
    private boolean addDefaultConfigurators = true;
    private final MetaMachine machine;

    private Consumer<Flow> leftConfigurators = (f) -> {};
    private Consumer<Flow> rightConfigurators = (f) -> {};
    private Consumer<ParentWidget<?>> mainContents = (p) -> {};

    protected MachineUIPanelBuilder(MetaMachine machine) {
        this.machine = machine;
    }

    public MachineUIPanel build(PanelSyncManager syncManager, UISettings settings) {
        var panel = new MachineUIPanel(machine, settings, attachInventory, addTitleBar, drawGTLogo, gtLogoTexture);

        var attachLeft = panel.getLeftConfiguratorPanel();
        var attachRight = panel.getRightConfiguratorPanel();
        var attachMain = panel.getMainContents();

        if (addDefaultConfigurators) {
            if (machine instanceof IHasCircuitSlot circuitSlot && circuitSlot.isCircuitSlotEnabled()) {
                attachLeft.child(GTMuiWidgets.createCircuitSlotPanel(circuitSlot, panel, syncManager));
            }

            if (machine instanceof IControllable controllable) {
                attachRight.child(GTMuiWidgets.createPowerButton(controllable));
            }
            if (machine instanceof IHasBatterySlot batterySlot) {
                attachRight.child(GTMuiWidgets.createBatterySlot(batterySlot, syncManager));
            }
            if (machine instanceof IVoidable voidable && machine instanceof WorkableMultiblockMachine) {
                attachRight.child(GTMuiWidgets.createVoidingButton(voidable));
            }
            if (machine instanceof IDistinctPart distinctPart) {
                attachRight.child(GTMuiWidgets.createDistinctnessButton(distinctPart));
            }
        }

        leftConfigurators.accept(attachLeft);
        rightConfigurators.accept(attachRight);
        mainContents.accept(attachMain);

        if (addTraitConfigurators) {
            for (var trait : machine.getTraitHolder().getAllTraits()) {
                if (trait instanceof IAttachConfiguratorsTrait attachConfiguratorsTrait) {
                    attachConfiguratorsTrait.attachLeftConfigurators(attachLeft, panel, syncManager);
                    attachConfiguratorsTrait.attachRightConfigurators(attachRight, panel, syncManager);
                }
            }
        }

        if (machine instanceof SimpleSteamMachine steamMachine) {
            IntSyncValue steamAmount = syncManager.getOrCreateSyncHandler("steamTank", IntSyncValue.class,
                    () -> new IntSyncValue(() -> steamMachine.steamTank.getFluidInTank(0).getAmount()));
            IntSyncValue steamCapacity = syncManager.getOrCreateSyncHandler("steamCapacity", IntSyncValue.class,
                    () -> new IntSyncValue(() -> steamMachine.steamTank.getTankCapacity(0)));

            DoubleSyncValue steamProgress = syncManager.getOrCreateSyncHandler("steamTankRatio", DoubleSyncValue.class,
                    () -> new DoubleSyncValue(() -> steamMachine.steamTank.getFluidInTank(0).getAmount() /
                            (float) steamMachine.steamTank.getTankCapacity(0)));
            final int dialWidth = 4;
            final int dialHeight = 12;
            UITexture background = steamMachine.isHighPressure() ? GTGuiTextures.STEAM_DIAL_STEEL :
                    GTGuiTextures.STEAM_DIAL_BRONZE;
            attachMain.child(new ParentWidget<>()
                    .child(background.asWidget()
                            .size(32, 32)
                            .tooltipAutoUpdate(true)
                            .tooltipDynamic(r -> r.addLine(Component.translatable("gtceu.multiblock.steam.steam_stored",
                                    FormattingUtil.formatNumbers(steamAmount.getIntValue()),
                                    FormattingUtil.formatNumbers(steamCapacity.getIntValue())))))
                    .child(new SteamDialWidget(steamProgress)
                            .setMinAngle((float) Math.PI)
                            .setMaxAngle((float) 0.0f)
                            .setColor(Color.BLACK.brighterSafe(4))
                            .asWidget().decoration()

                            .size(dialHeight, dialWidth)
                            .left(16)
                            .top(16)
                            .tooltipAutoUpdate(true)
                            .tooltipDynamic(r -> r.addLine(Component.translatable("gtceu.multiblock.steam.steam_stored",
                                    FormattingUtil.formatNumbers(steamAmount.getIntValue()),
                                    FormattingUtil.formatNumbers(steamCapacity.getIntValue())))))
                    .leftRel(0.0f).left(-36).top(4));
        }

        for (var cover : machine.getCoverContainer().getCovers()) {
            attachLeft.child(new ButtonWidget<>()
                    .overlay(new ItemDrawable(cover.getAttachItem()))
                    .onMousePressed((context, button) -> {
                        return true;
                    }));
        }

        return panel;
    }

    public static MachineUIPanelBuilder panelBuilder(MetaMachine machine) {
        return new MachineUIPanelBuilder(machine);
    }

    public static MachineUIPanelBuilder defaultSteamMachinePanelBuilder(MetaMachine machine) {
        return new MachineUIPanelBuilder(machine).addDefaultConfigurators(false)
                .addTraitConfigurators(false);
    }
}
