package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardEmitter;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.Tables;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleGeneratorMachine extends WorkableTieredMachine
                                    implements IFancyUIMachine, IEnvironmentalHazardEmitter {

    @Getter
    private final float hazardStrengthPerOperation;

    public SimpleGeneratorMachine(IMachineBlockEntity holder, int tier,
                                  float hazardStrengthPerOperation, Int2IntFunction tankScalingFunction,
                                  Object... args) {
        super(holder, tier, tankScalingFunction, args);
        this.hazardStrengthPerOperation = hazardStrengthPerOperation;
    }

    public SimpleGeneratorMachine(IMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction,
                                  Object... args) {
        this(holder, tier, 0.25f, tankScalingFunction, args);
    }
    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        var energyContainer = super.createEnergyContainer(args);
        energyContainer.setSideOutputCondition(side -> !hasFrontFacing() || side == getFrontFacing());
        return energyContainer;
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    //////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    //////////////////////////////////////

    /**
     * Recipe Modifier for <b>Simple Generator Machines</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is fast parallelized up to {@code desiredEUt / recipeEUt} times.
     * </p>
     * 
     * @param machine a {@link SimpleGeneratorMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Simple Generator
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof SimpleGeneratorMachine generator)) {
            return RecipeModifier.nullWrongType(SimpleGeneratorMachine.class, machine);
        }
        long EUt = recipe.getOutputEUt().getTotalEU();
        if (EUt <= 0) return ModifierFunction.NULL;

        int maxParallel = (int) (generator.getOverclockVoltage() / EUt);
        int parallels = ParallelLogic.getParallelAmountFast(generator, recipe, maxParallel);

        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallels))
                .outputModifier(ContentModifier.multiplier(parallels))
                .eutMultiplier(parallels)
                .parallels(parallels)
                .build();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return capability != EURecipeCapability.CAP;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        spreadEnvironmentalHazard();
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @SuppressWarnings("UnstableApiUsage")
    public static BiFunction<ResourceLocation, GTRecipeType, EditableMachineUI> EDITABLE_UI_CREATOR = Util
            .memoize((path, recipeType) -> new EditableMachineUI("generator", path, () -> {
                WidgetGroup template = recipeType.getRecipeUI().createEditableUITemplate(false, false).createDefault();
                WidgetGroup group = new WidgetGroup(0, 0, template.getSize().width + 4 + 8,
                        template.getSize().height + 8);
                Size size = group.getSize();
                template.setSelfPosition(new Position(
                        (size.width - 4 - template.getSize().width) / 2 + 4,
                        (size.height - template.getSize().height) / 2));
                group.addWidget(template);
                return group;
            }, (template, machine) -> {
                if (machine instanceof SimpleGeneratorMachine generatorMachine) {
                    var storages = Tables.newCustomTable(new EnumMap<>(IO.class),
                            LinkedHashMap<RecipeCapability<?>, Object>::new);
                    storages.put(IO.IN, ItemRecipeCapability.CAP, generatorMachine.importItems.storage);
                    storages.put(IO.OUT, ItemRecipeCapability.CAP, generatorMachine.exportItems.storage);
                    storages.put(IO.IN, FluidRecipeCapability.CAP, generatorMachine.importFluids);
                    storages.put(IO.OUT, FluidRecipeCapability.CAP, generatorMachine.exportFluids);

                    generatorMachine.getRecipeType().getRecipeUI().createEditableUITemplate(false, false).setupUI(
                            template,
                            new GTRecipeTypeUI.RecipeHolder(generatorMachine.recipeLogic::getProgressPercent,
                                    storages,
                                    new CompoundTag(),
                                    Collections.emptyList(),
                                    false, false));
                    createEnergyBar().setupUI(template, generatorMachine);
                }
            }));
}
