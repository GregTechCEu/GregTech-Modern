package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import net.minecraft.util.StringRepresentable;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface IVoidable extends IMachineFeature {

    default boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return getVoidingMode().canVoid(capability) ||
                self().getDefinition().getRecipeOutputLimits().getOrDefault(capability, -1) == 0;
    }

    default Reference2IntMap<RecipeCapability<?>> getOutputLimits() {
        return self().getDefinition().getRecipeOutputLimits();
    }

    default void setVoidingMode(VoidingMode mode) {}

    default VoidingMode getVoidingMode() {
        return VoidingMode.VOID_NONE;
    }

    enum VoidingMode implements StringRepresentable {

        VOID_NONE("gtceu.gui.no_voiding", cap -> false),
        VOID_ITEMS("gtceu.gui.item_voiding", cap -> cap == ItemRecipeCapability.CAP),
        VOID_FLUIDS("gtceu.gui.fluid_voiding", cap -> cap == FluidRecipeCapability.CAP),
        VOID_ITEMS_FLUIDS("gtceu.gui.all_voiding",
                cap -> cap == ItemRecipeCapability.CAP || cap == FluidRecipeCapability.CAP);

        public static final VoidingMode[] VALUES = values();

        @Getter
        private final String tooltip;
        private final Predicate<RecipeCapability<?>> canVoid;

        VoidingMode(String name, Predicate<RecipeCapability<?>> canVoid) {
            this.tooltip = name;
            this.canVoid = canVoid;
        }

        public boolean canVoid(RecipeCapability<?> capability) {
            return canVoid.test(capability);
        }

        @Override
        public @NotNull String getSerializedName() {
            return tooltip;
        }
    }
}
