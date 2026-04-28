package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import com.google.common.collect.Tables;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;

final class SimpleGeneratorMachineUI {

    private SimpleGeneratorMachineUI() {}

    static Object createEditableUI(Identifier path, GTRecipeType recipeType) {
        return new EditableMachineUI("generator", path, () -> {
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
                TieredEnergyMachineUI.createEnergyBar().setupUI(template, generatorMachine);
            }
        });
    }
}
