package com.gregtechceu.gtceu.common.machine.multiblock.generator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeCombustionEngineMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    private static final FluidStack OXYGEN_STACK = GTMaterials.Oxygen.getFluid(1);
    private static final FluidStack LIQUID_OXYGEN_STACK = GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 4);
    private static final FluidStack LUBRICANT_STACK = GTMaterials.Lubricant.getFluid(1);

    @Getter
    private final int tier;
    @Getter
    private boolean isOxygenBoosted = false;
    private int runningTimer = 0;

    public LargeCombustionEngineMachine(BlockEntityCreationInfo info, int tier) {
        super(info);
        this.tier = tier;
        recipeLogic.setRegressWhenWaiting(false);
    }

    private boolean isIntakesObstructed() {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                // Skip the controller block itself
                if (i == 0 && j == 0) continue;
                var blockPos = RelativeDirection.offsetPos(getBlockPos(), getFrontFacing(), getUpwardsFacing(),
                        isFlipped(),
                        i, j, 1);
                var blockState = this.getLevel().getBlockState(blockPos);
                if (!blockState.isAir())
                    return true;
            }
        }
        return false;
    }

    public boolean isExtreme() {
        return getTier() > GTValues.EV;
    }

    public boolean isBoostAllowed() {
        return getMaxVoltage() >= GTValues.V[getTier() + 1];
    }

    //////////////////////////////////////
    // ****** Recipe Logic *******//
    //////////////////////////////////////

    @Override
    public long getOverclockVoltage() {
        if (isOxygenBoosted) return GTValues.V[tier] * 2;
        else return GTValues.V[tier];
    }

    protected GTRecipe getLubricantRecipe() {
        return GTRecipeBuilder.ofRaw().inputFluids(LUBRICANT_STACK).buildRawRecipe();
    }

    protected GTRecipe getBoostRecipe() {
        return GTRecipeBuilder.ofRaw().inputFluids(isExtreme() ? LIQUID_OXYGEN_STACK : OXYGEN_STACK).buildRawRecipe();
    }

    public long getCurrentProduction() {
        return isActive() && recipeLogic.getLastUnrolledRecipe() != null ?
                recipeLogic.getLastUnrolledRecipe().getOutputEUt().voltage() : 0;
    }

    /**
     * @return EUt multiplier that should be applied to the engine's output
     */
    protected double getProductionBoost() {
        if (!isOxygenBoosted) return 1;
        return isExtreme() ? 2.0 : 1.5;
    }

    /**
     * Recipe Modifier for <b>Combustion Engine Multiblocks</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is rejected if the machine's intakes are obstructed or if it doesn't have lubricant<br>
     * Recipe is parallelized up to {@code desiredEUt / recipeEUt} times.
     * EUt is further multiplied by the production boost of the engine.
     *
     * @param machine a {@link LargeCombustionEngineMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Combustion Engine
     */
    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof LargeCombustionEngineMachine engineMachine)) {
            return RecipeModifier.nullWrongType(LargeCombustionEngineMachine.class, machine);
        }
        if (engineMachine.isIntakesObstructed()) {
            return ModifierFunction
                    .cancel(Component.translatable("gtceu.multiblock.large_combustion_engine.obstructed"));
        }
        if (!RecipeHelper.matchRecipe(engineMachine, engineMachine.getLubricantRecipe()).isSuccess()) {
            return ModifierFunction
                    .cancel(Component.translatable("gtceu.multiblock.large_combustion_engine.no_lubricant"));
        }

        EnergyStack EUt = recipe.getOutputEUt();
        if (!EUt.isEmpty()) {
            int maxParallel = (int) (engineMachine.getOverclockVoltage() / EUt.getTotalEU()); // get maximum parallel
            int actualParallel = ParallelLogic.getParallelAmount(engineMachine, recipe, maxParallel);
            double eutMultiplier = actualParallel * engineMachine.getProductionBoost();

            return ModifierFunction.builder()
                    .inputModifier(ContentModifier.multiplier(actualParallel))
                    .outputModifier(ContentModifier.multiplier(actualParallel))
                    .eutMultiplier(eutMultiplier)
                    .parallels(actualParallel)
                    .build();
        }
        return ModifierFunction.NULL;
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        // check lubricant

        if (runningTimer % 72 == 0) {
            // insufficient lubricant
            if (!RecipeHelper.handleRecipeIO(this, getLubricantRecipe(), IO.IN, this.recipeLogic.getChanceCaches())
                    .isSuccess()) {
                recipeLogic.interruptRecipe();
                return false;
            }
        }
        // check boost fluid
        if (isBoostAllowed()) {
            var boosterRecipe = getBoostRecipe();
            this.isOxygenBoosted = RecipeHelper.matchRecipe(this, boosterRecipe).isSuccess() &&
                    RecipeHelper.handleRecipeIO(this, boosterRecipe, IO.IN, this.recipeLogic.getChanceCaches())
                            .isSuccess();
        }

        runningTimer++;
        if (runningTimer > 72000) runningTimer %= 72000; // reset once every hour of running

        return value;
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();

        widgets.add(GTMultiblockTextUtil.addEnergyTierLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));

        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));
        BooleanSyncValue isActive = syncManager.getOrCreateSyncHandler("isActive", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isActive));
        BooleanSyncValue isBoostAllowed = syncManager.getOrCreateSyncHandler("canBoost",
                BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isBoostAllowed));
        BooleanSyncValue isOxygenBoosted = syncManager.getOrCreateSyncHandler("isOxygenBoosted",
                BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isOxygenBoosted));
        BooleanSyncValue isExtreme = syncManager.getOrCreateSyncHandler("isExtreme", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isExtreme));
        LongSyncValue engineOutput = syncManager.getOrCreateSyncHandler("engineOutput", LongSyncValue.class,
                () -> new LongSyncValue(this::getCurrentProduction));

        var engineOutputDisplay = Text.dynamic(() -> Component.translatable(
                "gtceu.multiblock.large_combustion_engine.output", engineOutput.getLongValue())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue() && isActive.getBoolValue());
        var boostDisallowed = Text.dynamic(() -> Component.translatable(
                "gtceu.multiblock.large_combustion_engine.boost_disallowed"))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue() && !isBoostAllowed.getBoolValue());
        var canBoost = Text.dynamic(() -> Component.translatable(
                isExtreme.getValue() ?
                        "gtceu.multiblock.large_combustion_engine.supply_liquid_oxygen_to_boost" :
                        "gtceu.multiblock.large_combustion_engine.supply_oxygen_to_boost"))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue() && isBoostAllowed.getBoolValue() &&
                        !isOxygenBoosted.getBoolValue());
        var isBoosted = Text.dynamic(() -> Component.translatable(
                isExtreme.getValue() ?
                        "gtceu.multiblock.large_combustion_engine.liquid_oxygen_boosted" :
                        "gtceu.multiblock.large_combustion_engine.oxygen_boosted"))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue() && isBoostAllowed.getBoolValue() &&
                        isOxygenBoosted.getBoolValue());

        widgets.add(GTMultiblockTextUtil.addProgressLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addRecipeTypeField(this, syncManager));

        widgets.add(engineOutputDisplay);
        widgets.add(boostDisallowed);
        widgets.add(canBoost);
        widgets.add(isBoosted);

        widgets.addAll(getDefinition().getAdditionalDisplay().apply(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addTotalRunsLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addOutputLines(this, syncManager));
        widgets.addAll(GTMultiblockTextUtil.addRecipeFailReasonLines(this, syncManager));

        return widgets;
    }
}
