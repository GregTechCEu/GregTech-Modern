package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RecipeRunner {

    private final GTRecipe recipe;
    private final IO io;
    private final RecipeHandlerGroup group;
    private final boolean simulated;
    private final Predicate<RecipeCapability<?>> outputVoid;

    public RecipeRunner(GTRecipe recipe, IO io, boolean isTick,
                        RecipeHandlerGroup group, boolean simulated) {
        this.recipe = recipe;
        this.io = io;
        this.group = group;

        this.simulated = simulated;
        this.outputVoid = group.getOutputVoid();
    }

    @NotNull
    public ActionResult handle(ContentListMap recipeContents) {
        if (recipeContents.isEmpty()) return ActionResult.SUCCESS;
        if (!hasCapabilitiesForIO()) {
            return ActionResult.fail(
                    Component.translatable("gtceu.recipe_logic.no_capabilities")
                            .append(Component.literal(": "))
                            .append(Component.translatable(io.tooltip)),
                    null, io);
        }

        group.handleRecipe(io, recipe, recipeContents, simulated);
        var result = getFailureResult(recipeContents);
        if (!result.isSuccess()) return result;
        return ActionResult.SUCCESS;
    }

    private boolean hasCapabilitiesForIO() {
        if (io == IO.IN) return !group.getInputHandlerMap().isEmpty();
        if (io == IO.OUT) return !group.getOutputHandlerMap().isEmpty();
        return false;
    }

    private ActionResult getFailureResult(ContentListMap contents) {
        for (var entry : contents.entrySet()) {
            // void excess real output contents if it can be voided
            if (!simulated && io == IO.OUT && this.outputVoid.test(entry.getKey())) {
                entry.getValue().clear();
            }
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                return ActionResult.fail(null, entry.getKey(), io);
            }
        }

        return ActionResult.SUCCESS;
    }

}
