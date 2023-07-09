package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
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

    @Override
    public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
        // we never use overclock but parallel logic
        var EUt = RecipeHelper.getOutputEUt(recipe);
        if (EUt > 0) {
            var maxParallel = (int)(Math.min(energyContainer.getOutputVoltage(), GTValues.V[overclockTier]) / EUt);
            while (maxParallel > 0) {
                var copied = recipe.copy(ContentModifier.multiplier(maxParallel), false);
                if (copied.matchRecipe(this).isSuccessed()) {
                    return copied;
                }
                maxParallel /= 2;
            }
        }
        return null;
    }

    @Override
    public boolean alwaysTryModifyRecipe() {
        return true;
    }

    @Override
    public boolean dampingWhenWaiting() {
        return false;
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
            generatorMachine.recipeType.createEditableUITemplate(false, false).setupUI(template,
                    new GTRecipeType.RecipeHolder(generatorMachine.recipeLogic::getProgressPercent,
                            generatorMachine.importItems.storage,
                            generatorMachine.exportItems.storage,
                            generatorMachine.importFluids.storages,
                            generatorMachine.exportFluids.storages,
                            false, false));
            createEnergyBar().setupUI(template, generatorMachine);
        }
    }));

}
