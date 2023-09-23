package com.gregtechceu.gtceu.common.machine.multiblock.electric;


import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.hpca.IHPCAComponentHatch;
import com.gregtechceu.gtceu.api.machine.trait.hpca.IHPCAComputationProvider;
import com.gregtechceu.gtceu.api.machine.trait.hpca.IHPCACoolantProvider;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class HPCAMachine extends WorkableElectricMultiblockMachine implements IOpticalComputationProvider, IControllable {

    private static final double IDLE_TEMPERATURE = 200;
    private static final double DAMAGE_TEMPERATURE = 1000;

    private IEnergyContainer energyContainer;
    private IFluidTransfer coolantHandler;
    private final HPCAGridHandler hpcaHandler = new HPCAGridHandler();

    @Persisted
    private double temperature = IDLE_TEMPERATURE; // start at idle temperature
    @Nullable
    protected TickableSubscription heatSubs;

    public HPCAMachine(IMachineBlockEntity holder) {
        super(holder);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        List<IFluidTransfer> fluidTanks = new ArrayList<>();
        List<IHPCAComponentHatch> componentHatches = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                var handlerIO = io == IO.BOTH ? handler.getHandlerIO() : io;
                if (handlerIO == IO.IN && handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                    traitSubscriptions.add(handler.addChangedListener(this::updateHeatSubscription));
                } else if (handlerIO == IO.IN && handler.getCapability() == FluidRecipeCapability.CAP && handler instanceof IFluidTransfer fluidTransfer) {
                    fluidTanks.add(fluidTransfer);
                    traitSubscriptions.add(handler.addChangedListener(this::updateHeatSubscription));
                }
            }
            if (part instanceof IHPCAComponentHatch hatch) {
                componentHatches.add(hatch);
            }
        }

        this.energyContainer = new EnergyContainerList(energyContainers);
        this.coolantHandler = new FluidTransferList(fluidTanks);
        this.hpcaHandler.onStructureForm(componentHatches);

        updateHeatSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.hpcaHandler.onStructureInvalidate();
        updateHeatSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            updateHeatSubscription();
        }
    }

    protected void updateHeatSubscription() {
        // do preheat logic for heat cool down and charge internal energy container
        if (temperature > 0 || (energyContainer.getEnergyStored() < energyContainer.getEnergyCapacity())) {
            heatSubs = subscribeServerTick(heatSubs, this::tickHeat);
        } else if (heatSubs != null) {
            heatSubs.unsubscribe();
            heatSubs = null;
        }
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isActive() ? hpcaHandler.allocateCWUt(cwut, simulate) : 0;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isActive() ? hpcaHandler.getMaxCWUt() : 0;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        // don't show a problem if the structure is not yet formed
        return !isFormed() || hpcaHandler.hasHPCABridge();
    }

    protected void tickHeat() {
        consumeEnergy();
        if (isActive()) {
            // forcibly use active coolers at full rate if temperature is half-way to damaging temperature
            double midpoint = (DAMAGE_TEMPERATURE - IDLE_TEMPERATURE) / 2;
            double temperatureChange = hpcaHandler.calculateTemperatureChange(coolantHandler, temperature >= midpoint) / 2.0;
            if (temperature + temperatureChange <= IDLE_TEMPERATURE) {
                temperature = IDLE_TEMPERATURE;
            } else {
                temperature += temperatureChange;
            }
            if (temperature >= DAMAGE_TEMPERATURE) {
                hpcaHandler.attemptDamageHPCA();
            }
            hpcaHandler.tick();
        } else {
            hpcaHandler.clearComputationCache();
            // passively cool (slowly) if not active
            temperature = Math.max(IDLE_TEMPERATURE, temperature - 0.25);
        }
        updateHeatSubscription();
    }

    private void consumeEnergy() {
        int energyToConsume = hpcaHandler.getCurrentEUt();

        if (this.energyContainer.getEnergyStored() >= energyToConsume) {
            if (!false) {
                long consumed = this.energyContainer.removeEnergy(energyToConsume);
                if (consumed == -energyToConsume) {
                    getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
                } else {
                    getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
                }
            }
        } else {
            getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.hpca.computation",
                    hpcaHandler.cachedCWUt, hpcaHandler.getMaxCWUt()));
            textList.add(Component.translatable("gtceu.multiblock.hpca.energy",
                    FormattingUtil.formatNumbers(hpcaHandler.cachedEUt),
                    FormattingUtil.formatNumbers(hpcaHandler.getMaxEUt()),
                    GTValues.VNF[GTUtil.getTierByVoltage(hpcaHandler.getMaxEUt())]));

            int coolantDemand = hpcaHandler.getMaxCoolantDemand();
            if (coolantDemand > 0 && hpcaHandler.getCoolant() != null) {
                textList.add(Component.translatable("gtceu.multiblock.hpca.coolant", coolantDemand));
            }

            int coolingDemand = hpcaHandler.getMaxCoolingDemand();
            int coolingProvided = hpcaHandler.getMaxCoolingAmount();
            textList.add(Component.translatable("gtceu.multiblock.hpca.cooling")
                    .append(getDisplayCoolingColor(coolingProvided, coolingDemand) + " " + coolingProvided + " / " + coolingDemand));

            textList.add(Component.translatable("gtceu.multiblock.hpca.temperature")
                    .append(getDisplayTemperatureColor() + " " + Math.round(temperature / 10.0D) + "°C"));

            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));
            } else if (isActive() && hpcaHandler.cachedCWUt > 0) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }
        }
    }

    private ChatFormatting getDisplayTemperatureColor() {
        if (temperature < 500) {
            return ChatFormatting.GREEN;
        } else if (temperature < 750) {
            return ChatFormatting.YELLOW;
        }
        return ChatFormatting.RED;
    }

    private ChatFormatting getDisplayCoolingColor(int provided, int demand) {
        if (provided >= demand) {
            return ChatFormatting.GREEN;
        } else if (demand - provided >= 2) {
            return ChatFormatting.YELLOW;
        }
        return ChatFormatting.RED;
    }

//    protected void addWarningText(List<Component> textList) {
//        if (isFormed()) {
//            if (temperature > 500) {
//                textList.add(Component.translatable("gtceu.multiblock.hpca.warning_temperature")
//                        .withStyle(ChatFormatting.RED));
//                if (hpcaHandler.hasActiveCoolers()) {
//                    textList.add(Component.translatable("gtceu.multiblock.hpca.warning_temperature_active_cool")
//                            .withStyle(ChatFormatting.GRAY));
//                }
//            }
//            hpcaHandler.addWarnings(textList);
//        }
//    }
//
//    @Override
//    protected void addErrorText(List<Component> textList) {
//        super.addErrorText(textList);
//        if (isStructureFormed()) {
//            if (temperature > 1000) {
//                textList.add(Component.translatable("gtceu.multiblock.hpca.error_temperature"));
//            }
//            hpcaHandler.addErrors(textList);
//        }
//    }

    // TODO add sound
//    public SoundEvent getSound() {
//        return GTSoundEntries.COMPUTATION;
//    }

    // Handles the logic of this structure's specific HPCA component grid
    public static class HPCAGridHandler {

        // structure info
        private final Set<IHPCAComponentHatch> components = new ObjectOpenHashSet<>();
        private final Set<IHPCACoolantProvider> coolantProviders = new ObjectOpenHashSet<>();
        private final Set<IHPCAComputationProvider> computationProviders = new ObjectOpenHashSet<>();
        private int numBridges;

        // transaction info
        private int allocatedCWUt;

        // cached gui info
        // holding these values past the computation clear because GUI is too "late" to read the state in time
        private int cachedEUt;
        private int cachedCWUt;

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

        private void onStructureInvalidate() {
            reset();
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

        public void tick() {
            cachedCWUt = allocatedCWUt;
            cachedEUt = getCurrentEUt();
            if (allocatedCWUt != 0) {
                allocatedCWUt = 0;
            }
        }

        /**
         * Calculate the temperature differential this tick given active computation and consume coolant.
         *
         * @param coolantTank         The tank to drain coolant from.
         * @param forceCoolWithActive Whether active coolers should forcibly cool even if temperature is already
         *                            decreasing due to passive coolers. Used when the HPCA is running very hot.
         * @return The temperature change, can be positive or negative.
         */
        public double calculateTemperatureChange(IFluidTransfer coolantTank, boolean forceCoolWithActive) {
            // calculate temperature increase
            int maxCWUt = Math.max(1, getMaxCWUt()); // behavior is no different setting this to 1 if it is 0
            int maxCoolingDemand = getMaxCoolingDemand();

            // temperature increase is proportional to the amount of actively used computation
            // a * (b / c)
            int temperatureIncrease = (int) Math.round(1.0 * maxCoolingDemand * allocatedCWUt / maxCWUt);

            // calculate temperature decrease
            int maxPassiveCooling = 0;
            int maxActiveCooling = 0;
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
                FluidStack coolantStack = coolantTank.drain(getCoolantStack(maxCoolantDrain), true);
                if (coolantStack != FluidStack.empty()) {
                    long coolantDrained = coolantStack.getAmount();
                    if (coolantDrained == maxCoolantDrain) {
                        // coolant requirement was fully met
                        temperatureChange -= maxActiveCooling;
                    } else {
                        // coolant requirement was only partially met, cool proportional to fluid amount drained
                        // a * (b / c)
                        temperatureChange -= maxActiveCooling * (1.0 * coolantDrained / maxCoolantDrain);
                    }
                }
            } else if (temperatureChange > 0) {
                // try to partially utilize active coolers to stabilize to zero
                double temperatureToDecrease = Math.min(temperatureChange, maxActiveCooling);
                int coolantToDrain = Math.max(1, (int) (maxCoolantDrain * (temperatureToDecrease / maxActiveCooling)));
                FluidStack coolantStack = coolantTank.drain(getCoolantStack(coolantToDrain), true);
                if (coolantStack != FluidStack.empty()) {
                    long coolantDrained = coolantStack.getAmount();
                    if (coolantDrained == coolantToDrain) {
                        // successfully stabilized to zero
                        return 0;
                    } else {
                        // coolant requirement was only partially met, cool proportional to fluid amount drained
                        // a * (b / c)
                        temperatureChange -= temperatureToDecrease * (1.0 * coolantDrained / coolantToDrain);
                    }
                }
            }
            return temperatureChange;
        }

        /**
         * Get the coolant stack for this HPCA. Eventually this could be made more diverse with different
         * coolants from different Active Cooler components, but currently it is just a fixed Fluid.
         */
        public FluidStack getCoolantStack(int amount) {
            return FluidStack.create(getCoolant(), amount);
        }

        private Fluid getCoolant() {
            return GTMaterials.PCBCoolant.getFluid();
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

        /** Allocate computation on a given request. Allocates for one tick. */
        public int allocateCWUt(int cwut, boolean simulate) {
            int maxCWUt = getMaxCWUt();
            int availableCWUt = maxCWUt - this.allocatedCWUt;
            int toAllocate = Math.min(cwut, availableCWUt);
            if (!simulate) {
                this.allocatedCWUt += toAllocate;
            }
            return toAllocate;
        }

        /** How much CWU/t is currently allocated for this tick. */
        public int getAllocatedCWUt() {
            return allocatedCWUt;
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
        public int getCurrentEUt() {
            int maximumCWUt = Math.max(1, getMaxCWUt()); // behavior is no different setting this to 1 if it is 0
            int maximumEUt = getMaxEUt();
            int upkeepEUt = getUpkeepEUt();

            if (maximumEUt == upkeepEUt) {
                return maximumEUt;
            }

            // energy draw is proportional to the amount of actively used computation
            // a + c(b - a) / d
            return upkeepEUt + ((maximumEUt - upkeepEUt) * allocatedCWUt / maximumCWUt);
        }

        /** The amount of EU/t this HPCA uses just to stay on with 0 output computation. */
        public int getUpkeepEUt() {
            int upkeepEUt = 0;
            for (var component : components) {
                upkeepEUt += component.getUpkeepEUt();
            }
            return upkeepEUt;
        }

        /** The maximum EU/t that this HPCA could ever use with the given configuration. */
        public int getMaxEUt() {
            int maximumEUt = 0;
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

        /** How much coolant this HPCA can consume in a tick, in L/t. */
        public int getMaxCoolantDemand() {
            int maxCoolant = 0;
            for (var coolantProvider : coolantProviders) {
                maxCoolant += coolantProvider.getMaxCoolantPerTick();
            }
            return maxCoolant;
        }

        public void addWarnings(List<Component> textList) {
            List<Component> warnings = new ArrayList<>();
            if (numBridges > 1) {
                warnings.add(Component.translatable("gtceu.multiblock.hpca.warning_multiple_bridges"));
            }
            if (computationProviders.isEmpty()) {
                warnings.add(Component.translatable("gtceu.multiblock.hpca.warning_no_computation"));
            }
            if (getMaxCoolingDemand() > getMaxCoolingAmount()) {
                warnings.add(Component.translatable("gtceu.multiblock.hpca.warning_low_cooling"));
            }
            if (!warnings.isEmpty()) {
                textList.add(Component.translatable("gtceu.multiblock.hpca.warning_structure_header"));
                textList.addAll(warnings);
            }
        }

        public void addErrors(List<Component> textList) {
            if (components.stream().anyMatch(IHPCAComponentHatch::isDamaged)) {
                textList.add(Component.translatable("gtceu.multiblock.hpca.error_damaged"));
            }
        }
    }
}
