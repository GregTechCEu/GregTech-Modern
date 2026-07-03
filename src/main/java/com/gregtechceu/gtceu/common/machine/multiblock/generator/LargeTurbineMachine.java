package com.gregtechceu.gtceu.common.machine.multiblock.generator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.ITurbineMachine;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.RecipeElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeTurbineMachine extends RecipeElectricMultiblockMachine implements ITurbineMachine {

    public static final int MIN_DURABILITY_TO_WARN = 10;

    private final long BASE_EU_OUTPUT;
    @Getter
    private final int tier;

    public LargeTurbineMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
        this.BASE_EU_OUTPUT = GTValues.V[tier] * 2;
    }

    @Nullable
    private IRotorHolderMachine getRotorHolder() {
        for (IMultiPart part : getParts()) {
            if (part instanceof IRotorHolderMachine rotorHolder) {
                return rotorHolder;
            }
        }
        return null;
    }

    @Override
    public long getOverclockVoltage() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor())
            return BASE_EU_OUTPUT * rotorHolder.getTotalPower() / 100;
        return 0;
    }

    /**
     * @return EUt multiplier that should be applied to the turbine's output
     */
    protected double productionBoost() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            int maxSpeed = rotorHolder.getMaxRotorHolderSpeed();
            int currentSpeed = rotorHolder.getRotorSpeed();
            if (currentSpeed >= maxSpeed) return 1;
            return Math.pow(1.0 * currentSpeed / maxSpeed, 2);
        }
        return 0;
    }

    @Override
    public boolean hasRotor() {
        var rotorHolder = getRotorHolder();
        return rotorHolder != null && rotorHolder.hasRotor();
    }

    @Override
    public int getRotorSpeed() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getRotorSpeed();
        }
        return 0;
    }

    @Override
    public int getMaxRotorHolderSpeed() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getMaxRotorHolderSpeed();
        }
        return 0;
    }

    @Override
    public int getTotalEfficiency() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getTotalEfficiency();
        }
        return -1;
    }

    @Override
    public long getCurrentProduction() {
        return isActive() && recipeLogic.getLastRecipe() != null ?
                recipeLogic.getLastRecipe().getOutputEUt() : 0;
    }

    @Override
    public int getRotorDurabilityPercent() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getRotorDurabilityPercent();
        }
        return -1;
    }

    //////////////////////////////////////
    // ****** Recipe Logic *******//
    //////////////////////////////////////
    /**
     * Recipe Modifier for <b>Large Turbine Multiblocks</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is fast parallelized up to {@code (baseEUt * power) / recipeEUt} times.
     * Duration is then multiplied by the holder efficiency.
     * </p>
     *
     * @param machine a {@link LargeTurbineMachine}
     * @param recipe  recipe
     * @return the failure reason, or {@code null} on success
     */
    public static @Nullable Component recipeModifier(@NotNull MetaMachine machine, RecipeHandlerGroup group,
                                                     @NotNull GTRecipe recipe) {
        if (!(machine instanceof LargeTurbineMachine turbineMachine)) {
            return RecipeModifier.nullWrongType(LargeTurbineMachine.class, machine);
        }

        if (turbineMachine.getTier() <= recipe.tier)
            return Component.translatable("gtceu.recipe_modifier.insufficient_voltage");

        var rotorHolder = turbineMachine.getRotorHolder();
        if (rotorHolder == null) return Component.translatable("gtceu.recipe_modifier.missing_valid_turbine_rotor");

        double holderEfficiency = rotorHolder.getTotalEfficiency() / 100.0;
        if (holderEfficiency <= 0) return Component.translatable("gtceu.recipe_modifier.missing_valid_turbine_rotor");

        long EUt = recipe.getOutputEUt();
        if (EUt <= 0) return RecipeModifier.DEFAULT_FAILURE;

        // get the amount of parallel required to match the desired output voltage
        // Max Parallel is Ceilinged not Floored to ensure the output voltage is actually met,
        // at the cost of slightly increased fuel
        long turbineMaxVoltage = turbineMachine.getOverclockVoltage();
        int maxParallel = (int) (turbineMaxVoltage / EUt);
        if (turbineMaxVoltage % EUt != 0) maxParallel++;

        int actualParallel = ParallelLogic.getParallelAmountFast(group, recipe, maxParallel);
        double eutMultiplier = (maxParallel == actualParallel) ?
                turbineMachine.productionBoost() * turbineMaxVoltage / (EUt * actualParallel) :
                turbineMachine.productionBoost();

        recipe.multiplyAllContents(actualParallel);
        recipe.multiplyEUt(eutMultiplier);
        recipe.parallels *= actualParallel;
        recipe.multiplyDuration(holderEfficiency);
        return null;
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        // void both eu and fluid tick outputs
        return true;
    }

    //////////////////////////////////////
    // ******* GUI ********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            var rotorHolder = getRotorHolder();

            if (rotorHolder != null && rotorHolder.getRotorEfficiency() > 0) {
                textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_speed",
                        FormattingUtil.formatNumbers(rotorHolder.getRotorSpeed()),
                        FormattingUtil.formatNumbers(rotorHolder.getMaxRotorHolderSpeed())));
                textList.add(Component.translatable("gtceu.multiblock.turbine.efficiency",
                        rotorHolder.getTotalEfficiency()));

                long maxProduction = getOverclockVoltage();
                long currentProduction = getCurrentProduction();

                if (isActive()) {
                    textList.add(3, Component.translatable("gtceu.multiblock.turbine.energy_per_tick",
                            FormattingUtil.formatNumbers(currentProduction),
                            FormattingUtil.formatNumbers(maxProduction)));
                }

                int rotorDurability = rotorHolder.getRotorDurabilityPercent();
                if (rotorDurability > MIN_DURABILITY_TO_WARN) {
                    textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_durability", rotorDurability));
                } else {
                    textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_durability", rotorDurability)
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                }
            }
        }
    }
}
