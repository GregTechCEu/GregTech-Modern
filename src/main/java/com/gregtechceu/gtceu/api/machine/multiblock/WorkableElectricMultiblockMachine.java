package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/6
 * @implNote WorkableElectricMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkableElectricMultiblockMachine extends WorkableMultiblockMachine implements IFancyUIMachine,
                                               IDisplayUIMachine, ITieredMachine, IOverclockMachine {

    // runtime
    protected EnergyContainerList energyContainer;
    @Getter
    protected int tier;

    public WorkableElectricMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
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

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        int numParallels = 0;
        Optional<IParallelHatch> optional = this.getParts().stream().filter(IParallelHatch.class::isInstance)
                .map(IParallelHatch.class::cast).findAny();
        if (optional.isPresent()) {
            IParallelHatch parallelHatch = optional.get();
            numParallels = parallelHatch.getCurrentParallel();
        }
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addEnergyUsageLine(energyContainer)
                .addEnergyTierLine(tier)
                .addMachineModeLine(getRecipeType(), getRecipeTypes().length > 1)
                .addParallelsLine(numParallels)
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic.getProgress(), recipeLogic.getMaxProgress(),
                        recipeLogic.getProgressPercent())
                .addOutputLines(recipeLogic.getLastRecipe());
        getDefinition().getAdditionalDisplay().accept(this, textList);
        IDisplayUIMachine.super.addDisplayText(textList);
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(getScreenTexture())
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .textSupplier(this.getLevel().isClientSide ? null : this::addDisplayText)
                        .setMaxWidthLimit(200)
                        .clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return getParts().stream().filter(Objects::nonNull).map(IFancyUIProvider.class::cast).toList();
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        for (IMultiPart part : getParts()) {
            part.attachFancyTooltipsToController(this, tooltipsPanel);
        }
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
        var capabilities = capabilitiesProxy.get(IO.IN, EURecipeCapability.CAP);
        if (capabilities != null) {
            for (IRecipeHandler<?> handler : capabilities) {
                if (handler instanceof IEnergyContainer container) {
                    containers.add(container);
                }
            }
        } else {
            capabilities = capabilitiesProxy.get(IO.OUT, EURecipeCapability.CAP);
            if (capabilities != null) {
                for (IRecipeHandler<?> handler : capabilities) {
                    if (handler instanceof IEnergyContainer container) {
                        containers.add(container);
                    }
                }
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
                return GTValues.V[GTUtil.getFloorTierByVoltage(voltage)];
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

    /**
     * Is this multiblock a generator?
     * Used for max voltage calculations.
     */
    public boolean isGenerator() {
        return getDefinition().isGenerator();
    }
}
