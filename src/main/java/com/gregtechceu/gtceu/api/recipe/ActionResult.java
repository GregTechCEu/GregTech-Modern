package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

/**
 * @param isSuccess is action success
 * @param reason    if fail, fail reason
 */
public record ActionResult(boolean isSuccess, @Nullable Component reason, @Nullable RecipeCapability<?> capability) {

    public final static ActionResult SUCCESS = new ActionResult(true, null, null);
    public final static ActionResult FAIL_NO_REASON = new ActionResult(false, null, null);
    public final static ActionResult PASS_NO_CONTENTS = new ActionResult(true,
            Component.translatable("gtceu.recipe_logic.no_contents"), null);
    public final static ActionResult FAIL_NO_CAPABILITIES = new ActionResult(false,
            Component.translatable("gtceu.recipe_logic.no_capabilities"), null);

    public static ActionResult fail(@Nullable Component component, @Nullable RecipeCapability<?> capability) {
        return new ActionResult(false, component, capability);
    }

    public Component reason() {
        if (reason == null) return Component.empty();
        return reason;
    }
}
