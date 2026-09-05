package com.gregtechceu.gtceu.common.machine.multiblock.generator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeTurbineMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    public static final int MIN_DURABILITY_TO_WARN = 10;

    private final long BASE_EU_OUTPUT;
    @Getter
    private final int tier;

    public LargeTurbineMachine(BlockEntityCreationInfo info, int tier) {
        super(info);
        this.tier = tier;
        this.BASE_EU_OUTPUT = GTValues.V[tier] * 2;
        recipeLogic.setRegressWhenWaiting(false);
    }

    @Nullable
    private RotorHolderPartMachine getRotorHolder() {
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof RotorHolderPartMachine rotorHolder) {
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

    public boolean hasRotor() {
        var rotorHolder = getRotorHolder();
        return rotorHolder != null && rotorHolder.hasRotor();
    }

    public int getRotorSpeed() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getRotorSpeed();
        }
        return 0;
    }

    public int getMaxRotorHolderSpeed() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getMaxRotorHolderSpeed();
        }
        return 0;
    }

    public int getTotalEfficiency() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getTotalEfficiency();
        }
        return -1;
    }

    public long getCurrentProduction() {
        return isActive() && recipeLogic.getLastUnrolledRecipe() != null ?
                recipeLogic.getLastUnrolledRecipe().getOutputEUt().voltage() : 0;
    }

    public int getRotorDurabilityPercent() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getRotorDurabilityPercent();
        }
        return -1;
    }

    //////////////////////////////////////
    // ******* GUI ********//
    //////////////////////////////////////

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();

        widgets.add(GTMultiblockTextUtil.addEnergyTierLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));

        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));
        var rotorHolder = getRotorHolder();
        if (!(rotorHolder != null && rotorHolder.hasRotor())) {
            widgets.add(
                    Text.dynamic(() -> (Component.translatable("gtceu.multiblock.turbine.no_rotor"))
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)))
                            .asWidget()
                            .setEnabledIf(w -> isFormed.getBoolValue()));
            return widgets;
        }

        BooleanSyncValue isActive = syncManager.getOrCreateSyncHandler("isActive",
                BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isActive));
        IntSyncValue rotorSpeed = syncManager.getOrCreateSyncHandler("rotorSpeed",
                IntSyncValue.class,
                () -> new IntSyncValue(rotorHolder::getRotorSpeed));
        IntSyncValue maxRotorSpeed = syncManager.getOrCreateSyncHandler("maxRotorSpeed",
                IntSyncValue.class,
                () -> new IntSyncValue(rotorHolder::getMaxRotorHolderSpeed));
        IntSyncValue totalEfficiency = syncManager.getOrCreateSyncHandler("totalEfficiency",
                IntSyncValue.class,
                () -> new IntSyncValue(rotorHolder::getTotalEfficiency));
        LongSyncValue currentOutput = syncManager.getOrCreateSyncHandler("currentOutput",
                LongSyncValue.class,
                () -> new LongSyncValue(this::getCurrentProduction));
        LongSyncValue maxOutput = syncManager.getOrCreateSyncHandler("maxOutput",
                LongSyncValue.class,
                () -> new LongSyncValue(this::getOverclockVoltage));
        IntSyncValue rotorDurability = syncManager.getOrCreateSyncHandler("rotorDurability",
                IntSyncValue.class,
                () -> new IntSyncValue(rotorHolder::getRotorDurabilityPercent));

        var rotorSpeedDisplay = Text.dynamic(() -> Component.translatable("gtceu.multiblock.turbine.rotor_speed",
                FormattingUtil.formatNumbers(rotorSpeed.getIntValue()),
                FormattingUtil.formatNumbers(maxRotorSpeed.getIntValue()))
                .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue());
        var turbineEfficiencyDisplay = Text.dynamic(() -> Component.translatable("gtceu.multiblock.turbine.efficiency",
                totalEfficiency.getIntValue())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue());
        var turbinePowerDisplay = Text.dynamic(() -> Component.translatable("gtceu.multiblock.turbine.energy_per_tick",
                FormattingUtil.formatNumbers(currentOutput.getIntValue()),
                FormattingUtil.formatNumbers(maxOutput.getIntValue()))
                .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue() && isActive.getBoolValue());
        var rotorDurabilityDisplay = Text
                .dynamic(() -> Component.translatable("gtceu.multiblock.turbine.rotor_durability",
                        rotorDurability.getIntValue())
                        .setStyle(rotorDurability.getIntValue() > MIN_DURABILITY_TO_WARN ?
                                Style.EMPTY.withColor(ChatFormatting.WHITE) :
                                Style.EMPTY.withColor(ChatFormatting.RED)))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue());

        widgets.add(GTMultiblockTextUtil.addProgressLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addRecipeTypeField(this, syncManager));

        widgets.add(rotorSpeedDisplay);
        widgets.add(turbineEfficiencyDisplay);
        widgets.add(turbinePowerDisplay);
        widgets.add(rotorDurabilityDisplay);

        widgets.addAll(getDefinition().getAdditionalDisplay().apply(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addTotalRunsLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addOutputLines(this, syncManager));
        widgets.addAll(GTMultiblockTextUtil.addRecipeFailReasonLines(this, syncManager));

        return widgets;
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
     * @return A {@link ModifierFunction} for the given Turbine Multiblock and recipe
     */
    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof LargeTurbineMachine turbineMachine)) {
            return RecipeModifier.nullWrongType(LargeTurbineMachine.class, machine);
        }

        var rotorHolder = turbineMachine.getRotorHolder();
        if (rotorHolder == null)
            return ModifierFunction.cancel(Component.translatable("gtceu.multiblock.turbine.no_rotor"));

        EnergyStack EUt = recipe.getOutputEUt();
        long turbineMaxVoltage = turbineMachine.getOverclockVoltage();
        double holderEfficiency = rotorHolder.getTotalEfficiency() / 100.0;

        if (EUt.isEmpty() || turbineMaxVoltage <= EUt.voltage() || holderEfficiency <= 0)
            return ModifierFunction.cancel(Component.translatable("gtceu.multiblock.turbine.no_rotor"));

        // get the amount of parallel required to match the desired output voltage
        // Max Parallel is Ceilinged not Floored to ensure the output voltage is actually met,
        // at the cost of slightly increased fuel
        int maxParallel = (int) (turbineMaxVoltage / EUt.getTotalEU());
        if (turbineMaxVoltage % EUt.getTotalEU() != 0) maxParallel++;

        int actualParallel = ParallelLogic.getParallelAmountFast(turbineMachine, recipe, maxParallel);
        double eutMultiplier = (maxParallel == actualParallel) ?
                turbineMachine.productionBoost() * turbineMaxVoltage / EUt.voltage() :
                turbineMachine.productionBoost() * actualParallel;

        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(actualParallel))
                .outputModifier(ContentModifier.multiplier(actualParallel))
                .eutMultiplier(eutMultiplier)
                .parallels(actualParallel)
                .durationMultiplier(holderEfficiency)
                .build();
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        // void both eu and fluid tick outputs
        return true;
    }
}
