package com.gregtechceu.gtceu.common.machine.multiblock.generator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.RecipeElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeCombustionEngineMachine extends RecipeElectricMultiblockMachine {

    private static final FluidStack OXYGEN_STACK = GTMaterials.Oxygen.getFluid(1);
    private static final FluidStack LIQUID_OXYGEN_STACK = GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 4);
    private static final FluidStack LUBRICANT_STACK = GTMaterials.Lubricant.getFluid(1);

    @Getter
    private final int tier;
    // runtime
    @DescSynced
    private boolean isOxygenBoosted = false;
    private int runningTimer = 0;

    public LargeCombustionEngineMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    private boolean isIntakesObstructed() {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                // Skip the controller block itself
                if (i == 0 && j == 0) continue;
                var blockPos = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(),
                        i, j, 1);
                var blockState = this.getLevel().getBlockState(blockPos);
                if (!blockState.isAir())
                    return true;
            }
        }
        return false;
    }

    private boolean isExtreme() {
        return getTier() > GTValues.EV;
    }

    public boolean isBoostAllowed() {
        return energyContainer.getTotalEUt() >= GTValues.V[getTier() + 1];
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
        return GTRecipeBuilder.ofRaw().inputFluids(LUBRICANT_STACK).buildRawRecipe().toRuntime();
    }

    protected GTRecipe getBoostRecipe() {
        return GTRecipeBuilder.ofRaw().inputFluids(isExtreme() ? LIQUID_OXYGEN_STACK : OXYGEN_STACK).buildRawRecipe()
                .toRuntime();
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
     * @return the failure reason, or {@code null} on success
     */
    public static @Nullable Component recipeModifier(@NotNull MetaMachine machine, RecipeHandlerGroup group,
                                                     @NotNull GTRecipe recipe) {
        if (!(machine instanceof LargeCombustionEngineMachine engineMachine)) {
            return RecipeModifier.nullWrongType(LargeCombustionEngineMachine.class, machine);
        }
        EnergyStack EUt = recipe.getOutputEUt();
        // has lubricant
        if (EUt.isEmpty()) return RecipeModifier.DEFAULT_FAILURE;
        if (engineMachine.isIntakesObstructed()) {
            return Component.translatable("gtceu.multiblock.large_combustion_engine.obstructed");
        }
        if(!RecipeHelper.matchRecipe(group, engineMachine.getLubricantRecipe()).isSuccess()) {
            return Component.translatable("gtceu.recipe_modifier.missing_lubricant");
        }

        int maxParallel = (int) (engineMachine.getOverclockVoltage() / EUt.getTotalEU()); // get maximum parallel
        int actualParallel = ParallelLogic.getParallelAmount(group, recipe, maxParallel);

        if(actualParallel <= 1) return null;

        recipe.multiplyAllContents(actualParallel);
        recipe.multiplyEUt(engineMachine.getProductionBoost());
        recipe.parallels *= actualParallel;
        return null;
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        // check lubricant

        if (runningTimer % 72 == 0) {
            // insufficient lubricant
            if (!RecipeHelper.handleRecipeIO(recipeLogic.getLastGroup(), getLubricantRecipe(), IO.IN)
                    .isSuccess()) {
                recipeLogic.interruptRecipe();
                return false;
            }
        }
        // check boost fluid
        if (isBoostAllowed()) {
            var boosterRecipe = getBoostRecipe();
            this.isOxygenBoosted = RecipeHelper.matchRecipe(recipeLogic.getLastGroup(), boosterRecipe).isSuccess() &&
                    RecipeHelper.handleRecipeIO(recipeLogic.getLastGroup(), boosterRecipe, IO.IN)
                            .isSuccess();
        }

        runningTimer++;
        if (runningTimer > 72000) runningTimer %= 72000; // reset once every hour of running

        return value;
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    //////////////////////////////////////
    // ******* GUI ********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.Builder builder = MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive());

        long lastEUt = recipeLogic.getLastRecipe() != null ?
                recipeLogic.getLastRecipe().getOutputEUt().getTotalEU() : 0;
        if (isExtreme()) {
            builder.addEnergyProductionLine(GTValues.V[tier + 1], lastEUt);
        } else {
            builder.addEnergyProductionAmpsLine(GTValues.V[tier] * 3, 3);
        }

        if (isActive() && isWorkingEnabled()) {
            builder.addCurrentEnergyProductionLine(lastEUt);
        }

        builder.addFuelNeededLine(getRecipeFluidInputInfo(), recipeLogic.getMaxProgress());

        if (isFormed && isOxygenBoosted) {
            final var key = isExtreme() ? "gtceu.multiblock.large_combustion_engine.liquid_oxygen_boosted" :
                    "gtceu.multiblock.large_combustion_engine.oxygen_boosted";
            builder.addCustom(tl -> tl.add(Component.translatable(key).withStyle(ChatFormatting.AQUA)));
        }

        builder.addWorkingStatusLine();
    }

    @Nullable
    public String getRecipeFluidInputInfo() {
        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        GTRecipe recipe = recipeLogic.getLastRecipe();
        if (recipe == null) {
            return null;
        }
        FluidStack requiredFluidInput = RecipeHelper.getInputFluids(recipe, false).get(0);

        long ocAmount = getOverclockVoltage() / recipe.getOutputEUt().getTotalEU();
        int neededAmount = GTMath.saturatedCast(ocAmount * requiredFluidInput.getAmount());
        return ChatFormatting.RED + FormattingUtil.formatNumbers(neededAmount) + "mB";
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        super.attachTooltips(tooltipsPanel);
        tooltipsPanel.attachTooltips(new IFancyTooltip.Basic(
                () -> GuiTextures.INDICATOR_NO_STEAM.get(false),
                () -> List.of(Component.translatable("gtceu.multiblock.large_combustion_engine.obstructed")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))),
                this::isIntakesObstructed,
                () -> null));
    }
}
