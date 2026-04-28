package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class WorkableElectricMultiblockMachine extends WorkableMultiblockMachine implements IFancyUIMachine,
                                               IDisplayUIMachine, ITieredMachine, IOverclockMachine {

    // runtime
    protected EnergyContainerList energyContainer;
    @Getter
    protected int tier;
    @SaveField
    @Getter
    protected boolean batchEnabled;

    public WorkableElectricMultiblockMachine(BlockEntityCreationInfo info,
                                             Function<WorkableMultiblockMachine, RecipeLogic> recipeLogicSupplier) {
        super(info, recipeLogicSupplier);
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

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        int numParallels;
        int subtickParallels;
        int batchParallels;
        int totalRuns;
        boolean exact = false;
        if (recipeLogic.isActive() && recipeLogic.getLastRecipe() != null) {
            numParallels = recipeLogic.getLastRecipe().parallels;
            subtickParallels = recipeLogic.getLastRecipe().subtickParallels;
            batchParallels = recipeLogic.getLastRecipe().batchParallels;
            totalRuns = recipeLogic.getLastRecipe().getTotalRuns();
            exact = true;
        } else {
            numParallels = getParallelHatch()
                    .map(ParallelHatchPartMachine::getCurrentParallel)
                    .orElse(0);
            subtickParallels = 0;
            batchParallels = 0;
            totalRuns = 0;
        }

        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addEnergyUsageLine(energyContainer)
                .addEnergyTierLine(tier)
                .addMachineModeLine(getRecipeType(), getRecipeTypes().length > 1)
                .addTotalRunsLine(totalRuns)
                .addParallelsLine(numParallels, exact)
                .addSubtickParallelsLine(subtickParallels)
                .addBatchModeLine(isBatchEnabled(), batchParallels)
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic)
                .addRecipeFailReasonLine(recipeLogic)
                .addOutputLines(recipeLogic.getLastRecipe());
        getDefinition().getAdditionalDisplay().accept(this, textList);
        IDisplayUIMachine.super.addDisplayText(textList);
    }

    @Override
    public Object createUIWidget() {
        return WorkableElectricMultiblockMachineUI.createUIWidget(this);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return WorkableElectricMultiblockMachineUI.createUI(this, entityPlayer);
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return getParts().stream().filter(Objects::nonNull).map(IFancyUIProvider.class::cast).toList();
    }

    @Override
    public void attachConfigurators(Object configuratorPanelObject) {
        WorkableElectricMultiblockMachineUI.attachConfigurators(this, configuratorPanelObject);
        IFancyUIMachine.super.attachConfigurators(configuratorPanelObject);
    }

    @Override
    public void attachTooltips(Object tooltipsPanelObject) {
        WorkableElectricMultiblockMachineUI.attachTooltips(this, tooltipsPanelObject);
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
        return Math.max(this.getEnergyContainer().getHighestInputVoltage(),
                this.getEnergyContainer().getOutputVoltage());
    }

    /**
     * Is this multiblock a generator?
     * Used for max voltage calculations.
     */
    public boolean isGenerator() {
        return getDefinition().isGenerator();
    }
}
