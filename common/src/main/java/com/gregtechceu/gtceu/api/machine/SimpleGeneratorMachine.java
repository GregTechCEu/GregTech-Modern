package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.utils.Position;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
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
    public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof SimpleGeneratorMachine generator) {
            var EUt = RecipeHelper.getOutputEUt(recipe);
            if (EUt > 0) {
                var maxParallel = (int)(Math.min(generator.getOverclockVoltage(), GTValues.V[generator.getOverclockTier()]) / EUt);
                return GTRecipeModifiers.fastParallel(generator, recipe, maxParallel, false).getA();
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

    public static BiFunction<ResourceLocation, GTRecipeType, EditableMachineUI> EDITABLE_UI_CREATOR = Util.memoize((path, recipeType)-> new EditableMachineUI("generator", path, () -> {
        var template =  recipeType.createEditableUITemplate(false, false).createDefault().setBackground(GuiTextures.BACKGROUND_INVERSE);
        var energyBar = createEnergyBar().createDefault();
        var group = new WidgetGroup(0, 0,
                Math.max(energyBar.getSize().width + template.getSize().width + 4 + 8, 172),
                Math.max(template.getSize().height + 8, energyBar.getSize().height + 8));
        var size = group.getSize();
        energyBar.setSelfPosition(new Position(3, (size.height - energyBar.getSize().height) / 2));
        template.setSelfPosition(new Position(
                (size.width - energyBar.getSize().width - 4 - template.getSize().width) / 2 + 2 + energyBar.getSize().width + 2,
                (size.height - template.getSize().height) / 2));
        group.addWidget(energyBar);
        group.addWidget(template);
        return group;
    }, (template, machine) -> {
        if (machine instanceof SimpleGeneratorMachine generatorMachine) {
            generatorMachine.getRecipeType().createEditableUITemplate(false, false).setupUI(template,
                    new GTRecipeType.RecipeHolder(generatorMachine.recipeLogic::getProgressPercent,
                            generatorMachine.importItems.storage,
                            generatorMachine.exportItems.storage,
                            generatorMachine.importFluids,
                            generatorMachine.exportFluids,
                            false, false));
            createEnergyBar().setupUI(template, generatorMachine);
        }
    }));
}
