package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;
import java.util.Map;

public interface IVoidable extends IMachineFeature {

    default boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return self().getDefinition().getRecipeOutputLimits().containsKey(capability);
    }

    // -1 or empty is taken into account as a skip case.
    default Map<RecipeCapability<?>, Integer> getOutputLimits() {
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

        @Nonnull
        @Override
        public String getSerializedName() {
            return localeName;
        }
    }
}
