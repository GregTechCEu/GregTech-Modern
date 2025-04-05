package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import net.minecraft.util.StringRepresentable;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;

public interface IVoidable extends IMachineFeature {

    default boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return self().getDefinition().getRecipeOutputLimits().containsKey(capability);
    }

    default Object2IntMap<RecipeCapability<?>> getOutputLimits() {
        return self().getDefinition().getRecipeOutputLimits();
    }

    enum VoidingMode implements StringRepresentable {

        VOID_NONE("gtceu.gui.multiblock_no_voiding"),
        VOID_ITEMS("gtceu.gui.multiblock_item_voiding"),
        VOID_FLUIDS("gtceu.gui.multiblock_fluid_voiding"),
        VOID_BOTH("gtceu.gui.multiblock_item_fluid_voiding");

        public static final VoidingMode[] VALUES = values();

        public final String localeName;

        VoidingMode(String name) {
            this.localeName = name;
        }

        @NotNull
        @Override
        public String getSerializedName() {
            return localeName;
        }
    }
}
