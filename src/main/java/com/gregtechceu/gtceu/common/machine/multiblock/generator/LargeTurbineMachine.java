package com.gregtechceu.gtceu.common.machine.multiblock.generator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/7/9
 * @implNote LargeCombustionEngineMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeTurbineMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {
    public static final int MIN_DURABILITY_TO_WARN = 10;

    private final int BASE_EU_OUTPUT;
    @Getter
    private final int tier;
    private int excessVoltage;

    public LargeTurbineMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
        this.BASE_EU_OUTPUT = (int) GTValues.V[tier] * 2;
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
            return (long) BASE_EU_OUTPUT * rotorHolder.getTotalPower() / 100;
        return 0;
    }

    protected long boostProduction(long production) {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            int maxSpeed = rotorHolder.getMaxRotorHolderSpeed();
            int currentSpeed = rotorHolder.getRotorSpeed();
            if (currentSpeed >= maxSpeed)
                return production;
            return (long) (production * Math.pow(1.0 * currentSpeed / maxSpeed, 2));
        }
        return 0;
    }

    //////////////////////////////////////
    //******     Recipe Logic    *******//
    //////////////////////////////////////
    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof LargeTurbineMachine turbineMachine) {
            var rotorHolder = turbineMachine.getRotorHolder();
            var EUt = RecipeHelper.getOutputEUt(recipe);
            if (rotorHolder != null && EUt > 0) {
                var turbineMaxVoltage = (int)turbineMachine.getOverclockVoltage();
                if (turbineMachine.excessVoltage >= turbineMaxVoltage) {
                    turbineMachine.excessVoltage -= turbineMaxVoltage;
                } else {
                    double holderEfficiency = rotorHolder.getTotalEfficiency() / 100.0;
                    //get the amount of parallel required to match the desired output voltage
                    var maxParallel = (int) ((turbineMaxVoltage - turbineMachine.excessVoltage) / (EUt * holderEfficiency));
                    //this is necessary to prevent over-consumption of fuel
                    turbineMachine.excessVoltage += (int) (maxParallel * EUt * holderEfficiency - turbineMaxVoltage);
                    var parallelResult = GTRecipeModifiers.fastParallel(turbineMachine, recipe, Math.max(1, maxParallel), false);
                    recipe = parallelResult.getA() == recipe ? recipe.copy() : parallelResult.getA();
                    long eut = turbineMachine.boostProduction((long) (EUt * holderEfficiency * parallelResult.getB()));
                    recipe.tickOutputs.put(EURecipeCapability.CAP, List.of(new Content(eut, 1.0f, 0.0f, null, null)));
                    return recipe;
                }
            }
        }
        return null;
    }

    @Override
    public boolean dampingWhenWaiting() {
        return false;
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return capability != EURecipeCapability.CAP;
    }

    //////////////////////////////////////
    //*******        GUI        ********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            var rotorHolder = getRotorHolder();

            if (rotorHolder != null && rotorHolder.getRotorEfficiency() > 0) {
                textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_speed", FormattingUtil.formatNumbers(rotorHolder.getRotorSpeed()), FormattingUtil.formatNumbers(rotorHolder.getMaxRotorHolderSpeed())));
                textList.add(Component.translatable("gtceu.multiblock.turbine.efficiency", rotorHolder.getTotalEfficiency()));

                long maxProduction = getOverclockVoltage();
                long currentProduction = isActive() ? boostProduction((int) maxProduction) : 0;
                String voltageName = GTValues.VNF[GTUtil.getTierByVoltage(currentProduction)];

                if (isActive()) {
                    textList.add(3, Component.translatable("gtceu.multiblock.turbine.energy_per_tick", FormattingUtil.formatNumbers(currentProduction), voltageName));
                }

                int rotorDurability = rotorHolder.getRotorDurabilityPercent();
                if (rotorDurability > MIN_DURABILITY_TO_WARN) {
                    textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_durability", rotorDurability));
                } else {
                    textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_durability", rotorDurability).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                }
            }
        }
    }


}
