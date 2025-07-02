package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IFusionCasingType;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.block.FusionCasingBlock;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2IntAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2IntSortedMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.PERFECT_HALF_DURATION_FACTOR;
import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.PERFECT_HALF_VOLTAGE_FACTOR;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FusionReactorMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FusionReactorMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    // Standard OC used for Fusion
    public static final OverclockingLogic FUSION_OC = OverclockingLogic.create(PERFECT_HALF_DURATION_FACTOR,
            PERFECT_HALF_VOLTAGE_FACTOR, false);

    // Max EU -> Tier map, used to find minimum tier needed for X EU to start
    private static final Long2IntSortedMap FUSION_ENERGY = new Long2IntAVLTreeMap();
    // Tier -> Suffix map, i.e. LuV -> MKI
    private static final Int2ObjectMap<String> FUSION_NAMES = new Int2ObjectArrayMap<>(4);
    // Minimum registered fusion reactor tier
    private static int MINIMUM_TIER = MAX;

    @Getter
    private final int tier;
    @Nullable
    protected EnergyContainerList inputEnergyContainers;
    @Persisted
    protected long heat = 0;
    @Persisted
    protected final NotifiableEnergyContainer energyContainer;
    @Getter
    @DescSynced
    private Integer color = -1;
    @Nullable
    protected TickableSubscription preHeatSubs;

    public FusionReactorMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
        this.energyContainer = createEnergyContainer();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public NotifiableEnergyContainer createEnergyContainer() {
        // create an internal energy container for temp storage. its capacity is decided when the structure formed.
        // it doesn't provide any capability of all sides, but null for the goggles mod to check it storages.
        var container = new NotifiableEnergyContainer(this, 0, 0, 0, 0, 0);
        container.setCapabilityValidator(Objects::isNull);
        return container;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            updatePreHeatSubscription();
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // capture all energy containers
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Long2ObjectMap<IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap",
                Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE || io == IO.OUT) continue;
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                if (!handlerList.isValid(io)) continue;

                handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .forEach(energyContainers::add);
                traitSubscriptions.add(handlerList.subscribe(this::updatePreHeatSubscription, EURecipeCapability.CAP));
            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
        energyContainer.resetBasicInfo(calculateEnergyStorageFactor(getTier(), energyContainers.size()), 0, 0, 0, 0);
        updatePreHeatSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.inputEnergyContainers = null;
        heat = 0;
        energyContainer.resetBasicInfo(0, 0, 0, 0, 0);
        energyContainer.setEnergyStored(0);
        updatePreHeatSubscription();
    }

    //////////////////////////////////////
    // ***** Recipe Logic ******//
    //////////////////////////////////////
    protected void updatePreHeatSubscription() {
        // do preheat logic for heat cool down and charge internal energy container
        if (heat > 0 || (inputEnergyContainers != null && inputEnergyContainers.getEnergyStored() > 0 &&
                energyContainer.getEnergyStored() < energyContainer.getEnergyCapacity())) {
            preHeatSubs = subscribeServerTick(preHeatSubs, this::updateHeat);
        } else if (preHeatSubs != null) {
            preHeatSubs.unsubscribe();
            preHeatSubs = null;
        }
    }

    /**
     * Recipe Modifier for <b>Fusion Reactors</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * If the Fusion Reactor has enough heat or can get enough heat to run the recipe based on the {@code eu_to_start}
     * data,
     * apply {@link FusionReactorMachine#FUSION_OC} to the recipe.
     * Otherwise, the recipe is rejected.
     * </p>
     * 
     * @param machine a {@link FusionReactorMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Fusion Reactor and recipe
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof FusionReactorMachine fusionReactorMachine)) {
            return RecipeModifier.nullWrongType(FusionReactorMachine.class, machine);
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > fusionReactorMachine.getTier() ||
                !recipe.data.contains("eu_to_start") ||
                recipe.data.getLong("eu_to_start") > fusionReactorMachine.energyContainer.getEnergyCapacity()) {
            return ModifierFunction.NULL;
        }

        long heatDiff = recipe.data.getLong("eu_to_start") - fusionReactorMachine.heat;

        // if the stored heat is >= required energy, recipe is okay to run
        if (heatDiff <= 0) {
            return FUSION_OC.getModifier(machine, recipe, fusionReactorMachine.getMaxVoltage(), false);
        }
        // if the remaining energy needed is more than stored, do not run
        if (fusionReactorMachine.energyContainer.getEnergyStored() < heatDiff) return ModifierFunction.NULL;

        // remove the energy needed
        fusionReactorMachine.energyContainer.removeEnergy(heatDiff);
        // increase the stored heat
        fusionReactorMachine.heat += heatDiff;
        fusionReactorMachine.updatePreHeatSubscription();
        return FUSION_OC.getModifier(machine, recipe, fusionReactorMachine.getMaxVoltage(), false);
    }

    @Override
    public boolean onWorking() {
        GTRecipe recipe = recipeLogic.getLastRecipe();
        assert recipe != null;
        if (recipe.data.contains("eu_to_start")) {
            long heatDiff = recipe.data.getLong("eu_to_start") - this.heat;
            // if the remaining energy needed is more than stored, do not run
            if (heatDiff > 0) {
                recipeLogic.setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_fuel"));

                // if the remaining energy needed is more than stored, do not run
                if (this.energyContainer.getEnergyStored() < heatDiff)
                    return super.onWorking();
                // remove the energy needed
                this.energyContainer.removeEnergy(heatDiff);
                // increase the stored heat
                this.heat += heatDiff;
                this.updatePreHeatSubscription();
            }
        }

        if (color == -1) {
            if (!recipe.getOutputContents(FluidRecipeCapability.CAP).isEmpty()) {
                var stack = FluidRecipeCapability.CAP
                        .of(recipe.getOutputContents(FluidRecipeCapability.CAP).get(0).getContent()).getStacks()[0];
                int newColor = 0xFF000000 | GTUtil.getFluidColor(stack);
                if (!Objects.equals(color, newColor)) {
                    color = newColor;
                }
            }
        }
        return super.onWorking();
    }

    public void updateHeat() {
        // Drain heat when the reactor is not active, is paused via soft mallet, or does not have enough energy and has
        // fully wiped recipe progress
        // Don't drain heat when there is not enough energy and there is still some recipe progress, as that makes it
        // doubly hard to complete the recipe
        // (Will have to recover heat and recipe progress)
        if ((getRecipeLogic().isIdle() || !isWorkingEnabled() ||
                (getRecipeLogic().isWaiting() && getRecipeLogic().getProgress() == 0)) && heat > 0) {
            heat = heat <= 10000 ? 0 : (heat - 10000);
        }
        // charge the internal energy storage
        var leftStorage = energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored();
        if (inputEnergyContainers != null && leftStorage > 0) {
            energyContainer.addEnergy(inputEnergyContainers.removeEnergy(leftStorage));
        }
        updatePreHeatSubscription();
    }

    @Override
    public void onWaiting() {
        super.onWaiting();
        color = -1;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        color = -1;
    }

    @Override
    public long getMaxVoltage() {
        return Math.min(GTValues.V[tier], super.getMaxVoltage());
    }

    //////////////////////////////////////
    // ******** GUI *********//
    //////////////////////////////////////
    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.energy",
                    this.energyContainer.getEnergyStored(), this.energyContainer.getEnergyCapacity()));
            textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.heat", heat));
        }
    }

    public static void addEUToStartLabel(GTRecipe recipe, WidgetGroup group) {
        long euToStart = recipe.data.getLong("eu_to_start");
        if (euToStart <= 0) return;
        int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
        int fusionTier = findCeilingTier(euToStart);
        int tier = Math.max(MINIMUM_TIER, Math.max(recipeTier, fusionTier));
        group.addWidget(new LabelWidget(-8, group.getSizeHeight() - 10,
                LocalizationUtils.format("gtceu.recipe.eu_to_start",
                        FormattingUtil.formatNumberReadable2F(euToStart, false),
                        FUSION_NAMES.get(tier))));
    }

    //////////////////////////////////////
    // ******** MISC *********//
    //////////////////////////////////////
    public static void registerFusionTier(int tier, @NotNull String name) {
        long maxEU = calculateEnergyStorageFactor(tier, 16);
        FUSION_ENERGY.put(maxEU, tier);
        FUSION_NAMES.put(tier, name);
        MINIMUM_TIER = Math.min(tier, MINIMUM_TIER);
    }

    private static int findCeilingTier(long euToStart) {
        long key;
        // tail = submap where all keys are >= EU to start
        // if tail is empty, then EU is greater than all the EU values, so we choose the last key
        // otherwise we want the first key in the tail map
        var tail = FUSION_ENERGY.tailMap(euToStart);
        if (tail.isEmpty()) key = FUSION_ENERGY.lastLongKey();
        else key = tail.firstLongKey();
        return FUSION_ENERGY.get(key);
    }

    public static long calculateEnergyStorageFactor(int tier, int energyInputAmount) {
        return energyInputAmount * (long) Math.pow(2, tier - LuV) * 10000000L;
    }

    public static Block getCasingState(int tier) {
        return switch (tier) {
            case LuV -> FUSION_CASING.get();
            case ZPM -> FUSION_CASING_MK2.get();
            default -> FUSION_CASING_MK3.get();
        };
    }

    public static Block getCoilState(int tier) {
        if (tier == GTValues.LuV)
            return SUPERCONDUCTING_COIL.get();

        return FUSION_COIL.get();
    }

    public static IFusionCasingType getCasingType(int tier) {
        return switch (tier) {
            case LuV -> FusionCasingBlock.CasingType.FUSION_CASING;
            case ZPM -> FusionCasingBlock.CasingType.FUSION_CASING_MK2;
            case UV -> FusionCasingBlock.CasingType.FUSION_CASING_MK3;
            default -> FusionCasingBlock.CasingType.FUSION_CASING;
        };
    }
}
