package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

/**
 * @param isSuccess is action success
 * @param reason    if fail, fail reason
 * @param score     how close the recipe was to succeeding, in {@code [0, 1]}. {@code 1} means fully satisfied
 *                  (success), {@code 0} means nothing matched. Used to pick the most-relevant failure reason to
 *                  display. Failures that don't measure contents (conditions, missing capabilities) use {@code 0}.
 */
public record ActionResult(boolean isSuccess, @Nullable Component reason, @Nullable RecipeCapability<?> capability,
                           @Nullable IO io, double score) {

    public final static ActionResult SUCCESS = new ActionResult(true, null, null, null, 1.0);
    public final static ActionResult FAIL_NO_REASON = new ActionResult(false, null, null, null, 0.0);
    public final static ActionResult PASS_NO_CONTENTS = new ActionResult(true,
            Component.translatable("gtceu.recipe_logic.no_contents"), null, null, 1.0);
    public final static ActionResult FAIL_NO_CAPABILITIES = new ActionResult(false,
            Component.translatable("gtceu.recipe_logic.no_capabilities"), null, null, 0.0);

    public static ActionResult fail(@Nullable Component component, @Nullable RecipeCapability<?> capability, IO io) {
        return fail(component, capability, io, 0.0);
    }

    public static ActionResult fail(@Nullable Component component, @Nullable RecipeCapability<?> capability, IO io,
                                    double score) {
        return new ActionResult(false, component, capability, io, score);
    }

    public Component reason() {
        if (reason == null) return Component.empty();
        return reason;
    }
}
