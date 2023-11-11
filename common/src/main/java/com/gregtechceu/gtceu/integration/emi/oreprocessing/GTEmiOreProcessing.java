package com.gregtechceu.gtceu.integration.emi.oreprocessing;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.integration.GTOreProcessingWidget;
import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustPure;

public class GTEmiOreProcessing extends ModularEmiRecipe<WidgetGroup> {
    final Material material;

    public GTEmiOreProcessing(Material material) {
        super(() -> new GTOreProcessingWidget(material));
        this.material = material;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTOreProcessingEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return GTCEu.id(material.getName());
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> inputs = new ArrayList<>();
        inputs.add(EmiIngredient.of(ChemicalHelper.getTag(ore,material)));
        inputs.add(EmiIngredient.of(ChemicalHelper.getTag(rawOre,material)));
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> outputs = new ArrayList<>();
        outputs.add(EmiStack.of(ChemicalHelper.get(crushed,material)));
        outputs.add(EmiStack.of(ChemicalHelper.get(crushedPurified,material)));
        outputs.add(EmiStack.of(ChemicalHelper.get(crushedRefined,material)));
        outputs.add(EmiStack.of(ChemicalHelper.get(dust,material)));
        outputs.add(EmiStack.of(ChemicalHelper.get(dustImpure,material)));
        outputs.add(EmiStack.of(ChemicalHelper.get(dustPure,material)));
        return outputs;
    }
}
