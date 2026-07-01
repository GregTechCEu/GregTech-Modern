package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IHPCAComponentHatch;
import com.gregtechceu.gtceu.api.capability.IHPCAComputationProvider;
import com.gregtechceu.gtceu.api.capability.IHPCACoolantProvider;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.computation.ComputationProducer;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.util.TimedProgressSupplier;
import com.gregtechceu.gtceu.api.gui.widget.ExtendedProgressWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.data.recipe.CustomTags.HPCA_COOLANTS;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAMachine extends WorkableElectricMultiblockMachine implements ComputationProducer {

    private static final double IDLE_TEMPERATURE = 200;
    private static final double DAMAGE_TEMPERATURE = 1000;

    private IMaintenanceMachine maintenance;
    private IFluidHandler coolantHandler;

    private final HPCAGridHandler hpcaHandler;

    @Persisted
    private double temperature = IDLE_TEMPERATURE; // start at idle temperature

    private final TimedProgressSupplier progressSupplier;

    private boolean isActiveBefore = false;

    public HPCAMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.progressSupplier = new TimedProgressSupplier(200, 47, false);
        this.hpcaHandler = new HPCAGridHandler(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IFluidHandler> coolantContainers = new ArrayList<>();
        List<IHPCAComponentHatch> componentHatches = new ArrayList<>();

        for (IMultiPart part : getParts()) {

            if (part instanceof IHPCAComponentHatch componentHatch) {
                componentHatches.add(componentHatch);
            }
            if (part instanceof IMaintenanceMachine maintenanceMachine) {
                this.maintenance = maintenanceMachine;
            }

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {

                handlerList.getCapability(FluidRecipeCapability.CAP).stream()
                        .filter(h -> h.getHandlerIO().support(IO.IN))
                        .filter(IFluidHandler.class::isInstance)
                        .map(IFluidHandler.class::cast)
                        .forEach(coolantContainers::add);
            }
        }
        this.coolantHandler = new FluidHandlerList(coolantContainers);
        this.hpcaHandler.onStructureForm(componentHatches);

    }

    @Override
    public void onStructureInvalid() {
        this.updateActive(false);
        super.onStructureInvalid();
        this.coolantHandler = new FluidHandlerList(new ArrayList<>());
        this.hpcaHandler.reset();
    }

    @Override
    public int getOfferedCWUt() {
        return getWorkLogic().isWorking() ? hpcaHandler.getMaxCWUt() : 0;
    }

    @Override
    public void applyProducedCWUt(int allocatedCWUt) {
        hpcaHandler.setAllocatedCWUt(allocatedCWUt);
    }

    public boolean hasHPCABridge() {
        return hpcaHandler.hasHPCABridge();
    }

    @Override
    public boolean canBridgeComputation() {
        return hasHPCABridge();
    }

    @Override
    public void serverRunningTick() {
        long energyToConsume = hpcaHandler.getCurrentEUt();
        boolean hasMaintenance = ConfigHolder.INSTANCE.machines.enableMaintenance && this.maintenance != null;
        if (hasMaintenance) {
            // 10% more energy per maintenance problem
            energyToConsume += maintenance.getNumMaintenanceProblems() * energyToConsume / 10;
        }
        if(energyContainer.getEnergyStored() >= energyToConsume &&
                energyContainer.removeEnergy(energyToConsume) >= energyToConsume) {
            getWorkLogic().setStatus(RecipeLogic.Status.WORKING);
            updateActive(true);
        }
        else {
            setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_in").append(": ")
                    .append(EURecipeCapability.CAP.getName()));
            updateActive(false);
        }

        // forcibly use active coolers at full rate if temperature is half-way to damaging temperature
        double midpoint = (DAMAGE_TEMPERATURE - IDLE_TEMPERATURE) / 2;
        double temperatureChange = hpcaHandler.calculateTemperatureChange(coolantHandler, temperature >= midpoint) /
                2.0;
        if (temperature + temperatureChange <= IDLE_TEMPERATURE) {
            temperature = IDLE_TEMPERATURE;
        } else {
            temperature += temperatureChange;
        }
        if (temperature >= DAMAGE_TEMPERATURE) {
            hpcaHandler.attemptDamageHPCA();
        }
    }

    private void updateActive(boolean active) {
        if(active != isActiveBefore) {
            isActiveBefore = active;
            for (var part : getParts()) {
                if (part instanceof IHPCAComponentHatch hpcaPart) {
                    hpcaPart.setActive(active);
                }
            }
        }
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        updateActive(isWorkingAllowed);
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup builder = (WidgetGroup) super.createUIWidget();
        // Create the hover grid
        builder.addWidget(new ExtendedProgressWidget(
                () -> hpcaHandler.getAllocatedCWUt() > 0 ? progressSupplier.getAsDouble() : 0,
                74, 57, 47, 47, GuiTextures.HPCA_COMPONENT_OUTLINE)
                .setServerTooltipSupplier(hpcaHandler::addInfo)
                .setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT));
        int startX = 76;
        int startY = 59;

        // we need to know what components we have on the client
        if (getLevel().isClientSide) {
            if (isFormed) {
                hpcaHandler.tryGatherClientComponents(this.getLevel(), this.getPos(), this.getFrontFacing(),
                        this.getUpwardsFacing(), this.isFlipped);
            } else {
                hpcaHandler.clearClientComponents();
            }
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    final int index = i * 3 + j;
                    Supplier<IGuiTexture> textureSupplier = () -> hpcaHandler.getComponentTexture(index);
                    builder.addWidget(new ImageWidget(startX + (15 * j), startY + (15 * i), 13, 13, textureSupplier));
                }
            }
        }

        return builder;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(getWorkLogic().isWorkingEnabled(), getWorkLogic().isActive()) // transform into two-state system for
                                                                            // display
                .setWorkingStatusKeys(
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.work_paused",
                        "gtceu.multiblock.data_bank.providing")
                .addCustom(tl -> {
                    if (isFormed()) {
                        // Energy Usage
                        tl.add(Component.translatable(
                                "gtceu.multiblock.hpca.energy",
                                FormattingUtil.formatNumbers(hpcaHandler.getCurrentEUt()),
                                FormattingUtil.formatNumbers(hpcaHandler.getMaxEUt()),
                                GTValues.VNF[GTUtil.getTierByVoltage(hpcaHandler.getMaxEUt())])
                                .withStyle(ChatFormatting.GRAY));

                        // Provided Computation
                        Component cwutInfo = Component.literal(
                                hpcaHandler.allocatedCWUt + " / " + hpcaHandler.getMaxCWUt() + " CWU/t")
                                .withStyle(ChatFormatting.AQUA);
                        tl.add(Component.translatable(
                                "gtceu.multiblock.hpca.computation",
                                cwutInfo).withStyle(ChatFormatting.GRAY));
                    }
                })
                .addWorkingStatusLine();
    }

    private ChatFormatting getDisplayTemperatureColor() {
        if (temperature < 500) {
            return ChatFormatting.GREEN;
        } else if (temperature < 750) {
            return ChatFormatting.YELLOW;
        }
        return ChatFormatting.RED;
    }

    /*
     * @Override
     * protected void addWarningText(List<Component> textList) {
     * MultiblockDisplayText.builder(textList, isFormed(), false)
     * .addLowPowerLine(hasNotEnoughEnergy)
     * .addCustom(tl -> {
     * if (isStructureFormed()) {
     * if (temperature > 500) {
     * // Temperature warning
     * tl.add(TextComponentUtil.translationWithColor(
     * TextFormatting.YELLOW,
     * "gtceu.multiblock.hpca.warning_temperature"));
     *
     * // Active cooler overdrive warning
     * tl.add(TextComponentUtil.translationWithColor(
     * TextFormatting.GRAY,
     * "gtceu.multiblock.hpca.warning_temperature_active_cool"));
     * }
     *
     * // Structure warnings
     * hpcaHandler.addWarnings(tl);
     * }
     * })
     * .addMaintenanceProblemLines(getMaintenanceProblems());
     * }
     *
     * @Override
     * protected void addErrorText(List<Component> textList) {
     * super.addErrorText(textList);
     * if (isFormed()) {
     * if (temperature > 1000) {
     * textList.add(Component.translatable("gtceu.multiblock.hpca.error_temperature").withStyle(ChatFormatting.RED));
     * }
     * hpcaHandler.addErrors(textList);
     * }
     * }
     *
     * @Override
     * public void addBarHoverText(List<Component> hoverList, int index) {
     * if (index == 0) {
     * Component cwutInfo = Component.literal(
     * hpcaHandler.cachedCWUt + " / " + hpcaHandler.getMaxCWUt() + " CWU/t").withStyle(ChatFormatting.AQUA);
     * hoverList.add(Component.translatable(
     * "gtceu.multiblock.hpca.computation",
     * cwutInfo).withStyle(ChatFormatting.GRAY));
     * } else {
     * Component tempInfo = Component.literal(,
     * Math.round(temperature / 10.0D) + "°C").withStyle(getDisplayTemperatureColor());
     * hoverList.add(TextComponentUtil.translationWithColor(
     * TextFormatting.GRAY,
     * "gtceu.multiblock.hpca.temperature",
     * tempInfo));
     * }
     * }
     */

    // Handles the logic of this structure's specific HPCA component grid
    public static class HPCAGridHandler{
        @Nullable // for testing
        private final HPCAMachine controller;

        // structure info
        private final List<IHPCAComponentHatch> components = new ObjectArrayList<>();
        private final Set<IHPCACoolantProvider> coolantProviders = new ObjectOpenHashSet<>();
        private final Set<IHPCAComputationProvider> computationProviders = new ObjectOpenHashSet<>();
        private int numBridges;

        // transaction info
        /** How much CWU/t is currently allocated for this tick. */
        @Getter
        private int allocatedCWUt;

        public HPCAGridHandler(@Nullable HPCAMachine controller) {
            this.controller = controller;
        }

        public void onStructureForm(Collection<IHPCAComponentHatch> components) {
            reset();
            for (var component : components) {
                this.components.add(component);
                if (component instanceof IHPCACoolantProvider coolantProvider) {
                    this.coolantProviders.add(coolantProvider);
                }
                if (component instanceof IHPCAComputationProvider computationProvider) {
                    this.computationProviders.add(computationProvider);
                }
                if (component.isBridge()) {
                    this.numBridges++;
                }
            }
        }

        private void reset() {
            clearComputationCache();
            components.clear();
            coolantProviders.clear();
            computationProviders.clear();
            numBridges = 0;
        }

        private void clearComputationCache() {
            allocatedCWUt = 0;
        }

        public void setAllocatedCWUt(int allocatedCWUt) {
            this.allocatedCWUt = Math.max(0, Math.min(allocatedCWUt, getMaxCWUt()));
        }

        /**
         * Calculate the temperature differential this tick given active computation and consume coolant.
         *
         * @param coolantTank         The tank to drain coolant from.
         * @param forceCoolWithActive Whether active coolers should forcibly cool even if temperature is already
         *                            decreasing due to passive coolers. Used when the HPCA is running very hot.
         * @return The temperature change, can be positive or negative.
         */
        public double calculateTemperatureChange(IFluidHandler coolantTank, boolean forceCoolWithActive) {
            // calculate temperature increase
            int maxCWUt = Math.max(1, getMaxCWUt()); // avoids dividing by 0 and the behavior is no different
            int maxCoolingDemand = getMaxCoolingDemand();

            // temperature increase is proportional to the amount of actively used computation
            // a * (b / c)
            int temperatureIncrease = (int) Math.round(1.0 * maxCoolingDemand * allocatedCWUt / maxCWUt);

            // calculate temperature decrease
            long maxPassiveCooling = 0;
            long maxActiveCooling = 0;
            int maxCoolantDrain = 0;

            for (var coolantProvider : coolantProviders) {
                if (coolantProvider.isActiveCooler()) {
                    maxActiveCooling += coolantProvider.getCoolingAmount();
                    maxCoolantDrain += coolantProvider.getMaxCoolantPerTick();
                } else {
                    maxPassiveCooling += coolantProvider.getCoolingAmount();
                }
            }

            double temperatureChange = temperatureIncrease - maxPassiveCooling;
            // quick exit if no active cooling/coolant drain is present
            if (maxActiveCooling == 0 && maxCoolantDrain == 0) {
                return temperatureChange;
            }
            if (forceCoolWithActive || maxActiveCooling <= temperatureChange) {
                // try to fully utilize active coolers
                int remainingCoolant = maxCoolantDrain;
                for (var fluid : BuiltInRegistries.FLUID.getTagOrEmpty(HPCA_COOLANTS)) {
                    FluidStack drained = GTTransferUtils.drainFluidAccountNotifiableList(coolantTank,
                            new FluidStack(fluid.get(), remainingCoolant), IFluidHandler.FluidAction.EXECUTE);
                    remainingCoolant -= drained.getAmount();
                    if (remainingCoolant <= 0) break;
                }
                if (remainingCoolant <= 0) {
                    // coolant requirement was fully met
                    temperatureChange -= maxActiveCooling;
                } else {
                    // coolant requirement was only partially met, cool proportional to fluid amount drained
                    // a * (b / c)
                    int coolantDrained = maxCoolantDrain - remainingCoolant;
                    temperatureChange -= maxActiveCooling * (1.0 * coolantDrained / maxCoolantDrain);
                }
            } else if (temperatureChange > 0) {
                // try to partially utilize active coolers to stabilize to zero
                double temperatureToDecrease = Math.min(temperatureChange, maxActiveCooling);
                int coolantToDrain = Math.max(1, (int) (maxCoolantDrain * (temperatureToDecrease / maxActiveCooling)));
                int remainingCoolant = coolantToDrain;
                for (var fluid : BuiltInRegistries.FLUID.getTagOrEmpty(HPCA_COOLANTS)) {
                    FluidStack drained = GTTransferUtils.drainFluidAccountNotifiableList(coolantTank,
                            new FluidStack(fluid.get(), remainingCoolant), IFluidHandler.FluidAction.EXECUTE);
                    remainingCoolant -= drained.getAmount();
                    if (remainingCoolant <= 0) break;
                }
                if (remainingCoolant <= 0) {
                    // successfully stabilized to zero
                    return 0;
                } else {
                    // coolant requirement was only partially met, cool proportional to fluid amount drained
                    // a * (b / c)
                    int coolantDrained = (coolantToDrain - remainingCoolant);
                    temperatureChange -= temperatureToDecrease * (1.0 * coolantDrained / coolantToDrain);
                }
            }
            return temperatureChange;
        }

        /**
         * Roll a 1/200 chance to damage a HPCA component marked as damageable. Randomly selects the component.
         * If called every tick, this succeeds on average once every 10 seconds.
         */
        public void attemptDamageHPCA() {
            // 1% chance each tick to damage a component if running too hot
            if (GTValues.RNG.nextInt(200) == 0) {
                // randomize which component is actually damaged
                List<IHPCAComponentHatch> candidates = new ArrayList<>();
                for (var component : components) {
                    if (component.canBeDamaged()) {
                        candidates.add(component);
                    }
                }
                if (!candidates.isEmpty()) {
                    candidates.get(GTValues.RNG.nextInt(candidates.size())).setDamaged(true);
                }
            }
        }

        /** The maximum amount of CWUs (Compute Work Units) created per tick. */
        public int getMaxCWUt() {
            int maxCWUt = 0;
            for (var computationProvider : computationProviders) {
                maxCWUt += computationProvider.getCWUPerTick();
            }
            return maxCWUt;
        }

        /** The current EU/t this HPCA should use, considering passive drain, current computation, etc.. */
        public long getCurrentEUt() {
            long maximumCWUt = Math.max(1, getMaxCWUt()); // behavior is no different setting this to 1 if it is 0
            long maximumEUt = getMaxEUt();
            long upkeepEUt = getUpkeepEUt();

            if (maximumEUt == upkeepEUt) {
                return maximumEUt;
            }

            // energy draw is proportional to the amount of actively used computation
            // a + c(b - a) / d
            return upkeepEUt + ((maximumEUt - upkeepEUt) * allocatedCWUt / maximumCWUt);
        }

        /** The amount of EU/t this HPCA uses just to stay on with 0 output computation. */
        public long getUpkeepEUt() {
            long upkeepEUt = 0;
            for (var component : components) {
                upkeepEUt += component.getUpkeepEUt();
            }
            return upkeepEUt;
        }

        /** The maximum EU/t that this HPCA could ever use with the given configuration. */
        public long getMaxEUt() {
            long maximumEUt = 0;
            for (var component : components) {
                maximumEUt += component.getMaxEUt();
            }
            return maximumEUt;
        }

        /** Whether this HPCA has a Bridge to allow connecting to other HPCA's */
        public boolean hasHPCABridge() {
            return numBridges > 0;
        }

        /** Whether this HPCA has any cooling providers which are actively cooled. */
        public boolean hasActiveCoolers() {
            for (var coolantProvider : coolantProviders) {
                if (coolantProvider.isActiveCooler()) return true;
            }
            return false;
        }

        /** How much cooling this HPCA can provide. NOT related to coolant fluid consumption. */
        public int getMaxCoolingAmount() {
            int maxCooling = 0;
            for (var coolantProvider : coolantProviders) {
                maxCooling += coolantProvider.getCoolingAmount();
            }
            return maxCooling;
        }

        /** How much cooling this HPCA can require. NOT related to coolant fluid consumption. */
        public int getMaxCoolingDemand() {
            int maxCooling = 0;
            for (var computationProvider : computationProviders) {
                maxCooling += computationProvider.getCoolingPerTick();
            }
            return maxCooling;
        }

        /** How much coolant this HPCA can consume in a tick, in mB/t. */
        public int getMaxCoolantDemand() {
            int maxCoolant = 0;
            for (var coolantProvider : coolantProviders) {
                maxCoolant += coolantProvider.getMaxCoolantPerTick();
            }
            return maxCoolant;
        }

        public void addInfo(List<Component> textList) {
            // Max Computation
            MutableComponent data = Component.literal(Integer.toString(getMaxCWUt())).withStyle(ChatFormatting.AQUA);
            textList.add(Component.translatable("gtceu.multiblock.hpca.info_max_computation", data)
                    .withStyle(ChatFormatting.GRAY));

            // Cooling
            ChatFormatting coolingColor = getMaxCoolingAmount() < getMaxCoolingDemand() ? ChatFormatting.RED :
                    ChatFormatting.GREEN;
            data = Component.literal(Integer.toString(getMaxCoolingDemand())).withStyle(coolingColor);
            textList.add(Component.translatable("gtceu.multiblock.hpca.info_max_cooling_demand", data)
                    .withStyle(ChatFormatting.GRAY));

            data = Component.literal(Integer.toString(getMaxCoolingAmount())).withStyle(coolingColor);
            textList.add(Component.translatable("gtceu.multiblock.hpca.info_max_cooling_available", data)
                    .withStyle(ChatFormatting.GRAY));

            // Coolant Required
            if (getMaxCoolantDemand() > 0) {
                data = Component.translatable("gtceu.universal.liters", getMaxCoolantDemand())
                        .withStyle(ChatFormatting.YELLOW).append(" ");
                Component coolantName = Component.translatable("gtceu.multiblock.hpca.info_coolant_name")
                        .withStyle(ChatFormatting.YELLOW);
                data.append(coolantName);
            } else {
                data = Component.literal("0").withStyle(ChatFormatting.GREEN);
            }
            textList.add(Component.translatable("gtceu.multiblock.hpca.info_max_coolant_required", data)
                    .withStyle(ChatFormatting.GRAY));

            // Bridging
            if (numBridges > 0) {
                textList.add(Component.translatable("gtceu.multiblock.hpca.info_bridging_enabled")
                        .withStyle(ChatFormatting.GREEN));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.hpca.info_bridging_disabled")
                        .withStyle(ChatFormatting.RED));
            }

            if (numBridges > 1) {
                textList.add(Component.translatable("gtceu.multiblock.hpca.warning_multiple_bridges")
                        .withStyle(ChatFormatting.GRAY));
            }
            if (computationProviders.isEmpty()) {
                textList.add(Component.translatable("gtceu.multiblock.hpca.warning_no_computation")
                        .withStyle(ChatFormatting.GRAY));
            }
            if (getMaxCoolingDemand() > getMaxCoolingAmount()) {
                textList.add(Component.translatable("gtceu.multiblock.hpca.warning_low_cooling")
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        public void addWarnings(List<Component> textList) {
            List<Component> warnings = new ArrayList<>();

        }

        public void addErrors(List<Component> textList) {
            if (components.stream().anyMatch(IHPCAComponentHatch::isDamaged)) {
                textList.add(
                        Component.translatable("gtceu.multiblock.hpca.error_damaged").withStyle(ChatFormatting.RED));
            }
        }

        public ResourceTexture getComponentTexture(int index) {
            if (components.size() <= index) {
                return GuiTextures.BLANK_TRANSPARENT;
            }
            return components.get(index).getComponentIcon();
        }

        public void tryGatherClientComponents(Level world, BlockPos pos, Direction frontFacing,
                                              Direction upwardsFacing, boolean flip) {
            Direction relativeUp = RelativeDirection.UP.getRelative(frontFacing, upwardsFacing, flip);
            clearClientComponents();
            if (components.isEmpty()) {
                BlockPos testPos = pos
                        .relative(frontFacing.getOpposite(), 3)
                        .relative(relativeUp, 3);

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        BlockPos tempPos = testPos.relative(frontFacing, j).relative(relativeUp.getOpposite(), i);
                        BlockEntity be = world.getBlockEntity(tempPos);
                        if (be instanceof IHPCAComponentHatch hatch) {
                            components.add(hatch);
                        } else if (be instanceof IMachineBlockEntity machineBE) {
                            MetaMachine machine = machineBE.getMetaMachine();
                            if (machine instanceof IHPCAComponentHatch hatch) {
                                components.add(hatch);
                            }
                        }
                        // if here without a hatch, something went wrong, better to skip than add a null into the mix.
                    }
                }
            }
        }

        public void clearClientComponents() {
            components.clear();
        }
    }
}
