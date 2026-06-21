package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ListWidget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkableElectricMultiblockMachine extends WorkableMultiblockMachine
                                               implements IMuiMachine, ITieredMachine, IOverclockMachine, IVoidable {

    // runtime
    protected @Nullable EnergyContainerList energyContainer;
    @Getter
    protected int tier;
    @SaveField
    @Getter
    protected boolean batchEnabled;

    public WorkableElectricMultiblockMachine(BlockEntityCreationInfo info, RecipeLogic recipeLogic) {
        super(info, recipeLogic);
    }

    public WorkableElectricMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public WorkableElectricMultiblockMachine self() {
        return this;
    }

    //////////////////////////////////////
    // *** Multiblock Lifecycle ***//
    //////////////////////////////////////
    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.energyContainer = null;
        this.tier = 0;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.energyContainer = getEnergyContainer();
        this.tier = GTUtil.getFloorTierByVoltage(getMaxVoltage());
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        this.energyContainer = null;
        this.tier = 0;
    }

    @Override
    public void setBatchEnabled(boolean batch) {
        this.batchEnabled = batch;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    public static final int MULTI_UI_TEXT_PANEL_WIDTH = 172;
    public static final int MULTI_UI_TEXT_PANEL_HEIGHT = 136;

    public Widget<?> getMainTextPanel(PanelSyncManager syncManager) {
        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>()
                .width(MULTI_UI_TEXT_PANEL_WIDTH - 6)
                .height(MULTI_UI_TEXT_PANEL_HEIGHT - 6)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren()
                .posRel(Alignment.CenterLeft);
        parentWidget.size(MULTI_UI_TEXT_PANEL_WIDTH, MULTI_UI_TEXT_PANEL_HEIGHT).background(GuiTextures.DISPLAY);

        listWidget.children(getWidgetsForDisplay(syncManager));
        parentWidget.child(listWidget.left(3).top(3));
        return parentWidget;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(getMainTextPanel(syncManager).margin(4, 2));
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addEnergyTierLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addEnergyUsageLine(this, syncManager));
        widgets.addAll(super.getWidgetsForDisplay(syncManager));
        return widgets;
    }

    //////////////////////////////////////
    // ******** OVERCLOCK *********//
    //////////////////////////////////////
    @Override
    public int getOverclockTier() {
        return getTier();
    }

    @Override
    public int getMaxOverclockTier() {
        return getTier();
    }

    @Override
    public int getMinOverclockTier() {
        return getTier();
    }

    @Override
    public void setOverclockTier(int tier) {}

    @Override
    public long getOverclockVoltage() {
        if (this.energyContainer == null) {
            this.energyContainer = getEnergyContainer();
        }
        long voltage;
        long amperage;
        if (energyContainer.getInputVoltage() > energyContainer.getOutputVoltage()) {
            voltage = energyContainer.getInputVoltage();
            amperage = energyContainer.getInputAmperage();
        } else {
            voltage = energyContainer.getOutputVoltage();
            amperage = energyContainer.getOutputAmperage();
        }

        if (amperage == 1) {
            // amperage is 1 when the energy is not exactly on a tier
            // the voltage for recipe search is always on tier, so take the closest lower tier
            return GTValues.VEX[GTUtil.getFloorTierByVoltage(voltage)];
        } else {
            // amperage != 1 means the voltage is exactly on a tier
            // ignore amperage, since only the voltage is relevant for recipe search
            // amps are never > 3 in an EnergyContainerList
            return voltage;
        }
    }

    //////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    //////////////////////////////////////

    public EnergyContainerList getEnergyContainer() {
        List<IEnergyContainer> containers = new ArrayList<>();
        var handlers = getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
        if (handlers.isEmpty()) handlers = getCapabilitiesFlat(IO.OUT, EURecipeCapability.CAP);
        for (IRecipeHandler<?> handler : handlers) {
            if (handler instanceof IEnergyContainer container) {
                containers.add(container);
            }
        }
        return new EnergyContainerList(containers);
    }

    @Override
    public long getMaxVoltage() {
        if (this.energyContainer == null) {
            this.energyContainer = getEnergyContainer();
        }
        if (this.isGenerator()) {
            // Generators
            long voltage = energyContainer.getOutputVoltage();
            long amperage = energyContainer.getOutputAmperage();
            if (amperage == 1) {
                // Amperage is 1 when the energy is not exactly on a tier.
                // The voltage for recipe search is always on tier, so take the closest lower tier.
                // List check is done because single hatches will always be a "clean voltage," no need
                // for any additional checks.
                return GTValues.VEX[GTUtil.getFloorTierByVoltage(voltage)];
            } else {
                return voltage;
            }
        } else {
            // Machines
            long highestVoltage = energyContainer.getHighestInputVoltage();
            if (energyContainer.getNumHighestInputContainers() > 1) {
                // allow tier + 1 if there are multiple hatches present at the highest tier
                int tier = GTUtil.getTierByVoltage(highestVoltage);
                return GTValues.V[Math.min(tier + 1, GTValues.MAX)];
            } else {
                return highestVoltage;
            }
        }
    }

    @Override
    public long getDisplayRecipeVoltage() {
        long voltage = -1;
        var handlers = getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
        if (handlers.isEmpty()) handlers = getCapabilitiesFlat(IO.OUT, EURecipeCapability.CAP);
        for (IRecipeHandler<?> handler : handlers) {
            if (handler instanceof IEnergyContainer container) {
                voltage = Math.max(voltage, Math.max(container.getInputVoltage(), container.getOutputVoltage()));
            }
        }
        return voltage;
    }

    /**
     * Is this multiblock a generator?
     * Used for max voltage calculations.
     */
    public boolean isGenerator() {
        return getDefinition().isGenerator();
    }
}
