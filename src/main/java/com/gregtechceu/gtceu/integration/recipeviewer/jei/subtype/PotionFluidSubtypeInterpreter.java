package com.gregtechceu.gtceu.integration.recipeviewer.jei.subtype;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* From JEI's Potion item subtype interpreter */
public class PotionFluidSubtypeInterpreter implements ISubtypeInterpreter<FluidStack> {

    public static final PotionFluidSubtypeInterpreter INSTANCE = new PotionFluidSubtypeInterpreter();

    private PotionFluidSubtypeInterpreter() {}

    @Override
    public @Nullable Object getSubtypeData(@NotNull FluidStack ingredient, @NotNull UidContext context) {
        PotionContents contents = ingredient.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return null;
        }
        return contents.potion()
                .orElse(null);
    }

    @Override
    public @NotNull String getLegacyStringSubtypeInfo(FluidStack ingredient, @NotNull UidContext context) {
        if (ingredient.getComponentsPatch().isEmpty()) {
            return "";
        }
        PotionContents contents = ingredient.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        String itemDescriptionId = ingredient.getFluidType().getDescriptionId();
        String potionEffectId = contents.potion().map(Holder::getRegisteredName).orElse("none");
        return itemDescriptionId + ".effect_id." + potionEffectId;
    }
}
