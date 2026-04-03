package com.gregtechceu.gtceu.api.machine.mui;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IHasBatterySlot;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.trait.feature.IAttachConfiguratorsTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
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
            if (machine instanceof IVoidable voidable) {
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

        return panel;
    }

    public static MachineUIPanelBuilder defaultSimpleSingleblockPanelBuilder(MetaMachine machine) {
        return new MachineUIPanelBuilder(machine).drawGTLogo(true);
    }

    public static MachineUIPanelBuilder defaultPanelBuilder(MetaMachine machine) {
        return new MachineUIPanelBuilder(machine);
    }

    public static MachineUIPanelBuilder defaultSteamMachineBuilder(MetaMachine machine) {
        return new MachineUIPanelBuilder(machine).drawGTLogo(true).addDefaultConfigurators(false)
                .addTraitConfigurators(false);
    }
}
