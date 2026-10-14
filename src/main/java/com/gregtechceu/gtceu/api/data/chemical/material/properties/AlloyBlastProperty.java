package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.AlloyBlastRecipeProducer;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class AlloyBlastProperty implements IMaterialProperty {

    @Getter
    @Setter
    private @NotNull AlloyBlastRecipeProducer recipeProducer = AlloyBlastRecipeProducer.DEFAULT_PRODUCER;

    public AlloyBlastProperty() {}

    @Override
    public void verifyProperty(MaterialProperties materialProperties) {
        materialProperties.ensureSet(PropertyKey.BLAST);
        materialProperties.ensureSet(PropertyKey.FLUID);
    }
}
