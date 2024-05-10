package com.gregtechceu.gtceu.api.machine;

import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;

/**
 * @author KilaBash
 * @date 2023/3/17
 * @implNote SimpleGeneratorMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleGeneratorMachine extends WorkableTieredMachine implements IFancyUIMachine {

    public SimpleGeneratorMachine(IMachineBlockEntity holder, int tier, Int2LongFunction tankScalingFunction, Object... args) {
        super(holder, tier, tankScalingFunction, args);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
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
    protected long getMaxInputOutputAmperage() {
        return 1L;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    //////////////////////////////////////
    //******     RECIPE LOGIC    *******//
    //////////////////////////////////////

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof SimpleGeneratorMachine generator) {
            var EUt = RecipeHelper.getOutputEUt(recipe);
            if (EUt > 0) {
                var maxParallel = (int)(Math.min(generator.getOverclockVoltage(), GTValues.V[generator.getOverclockTier()]) / EUt);
                return GTRecipeModifiers.fastParallel(generator, recipe, maxParallel, false).getFirst();
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
    //***********     GUI    ***********//
    //////////////////////////////////////

    @SuppressWarnings("UnstableApiUsage")
    public static BiFunction<ResourceLocation, GTRecipeType, EditableMachineUI> EDITABLE_UI_CREATOR = Util.memoize((path, recipeType)-> new EditableMachineUI("generator", path, () -> {
        WidgetGroup template = recipeType.getRecipeUI().createEditableUITemplate(false, false).createDefault();
        WidgetGroup group = new WidgetGroup(0, 0, template.getSize().width + 4 + 8, template.getSize().height + 8);
        Size size = group.getSize();
        template.setSelfPosition(new Position(
            (size.width - 4 - template.getSize().width) / 2 + 4,
            (size.height - template.getSize().height) / 2));
        group.addWidget(template);
        return group;
    }, (template, machine) -> {
        if (machine instanceof SimpleGeneratorMachine generatorMachine) {
            var storages = Tables.newCustomTable(new EnumMap<>(IO.class), LinkedHashMap<RecipeCapability<?>, Object>::new);
            storages.put(IO.IN, ItemRecipeCapability.CAP, generatorMachine.importItems.storage);
            storages.put(IO.OUT, ItemRecipeCapability.CAP, generatorMachine.exportItems.storage);
            storages.put(IO.IN, FluidRecipeCapability.CAP, generatorMachine.importFluids);
            storages.put(IO.OUT, FluidRecipeCapability.CAP, generatorMachine.exportFluids);

            generatorMachine.getRecipeType().getRecipeUI().createEditableUITemplate(false, false).setupUI(template,
                    new GTRecipeTypeUI.RecipeHolder(generatorMachine.recipeLogic::getProgressPercent,
                        storages,
                        new CompoundTag(),
                        Collections.emptyList(),
                        false, false));
            createEnergyBar().setupUI(template, generatorMachine);
        }
    }));
}
