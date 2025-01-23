package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * @param isSuccess      is action success
 * @param reasonSupplier if fail, fail reasonSupplier
 */
public record ActionResult(boolean isSuccess, Supplier<@Nullable Component> reasonSupplier) {

    public final static ActionResult SUCCESS = new ActionResult(true, () -> null);
    public final static ActionResult FAIL_NO_REASON = new ActionResult(false, () -> null);
    public final static ActionResult PASS_NO_CONTENTS = new ActionResult(true,
            () -> Component.translatable("gtceu.recipe_logic.no_contents"));
    public final static ActionResult FAIL_NO_CAPABILITIES = new ActionResult(false,
            () -> Component.translatable("gtceu.recipe_logic.no_capabilities"));

    public static ActionResult fail(Supplier<@Nullable Component> component) {
        return new ActionResult(false, component);
    }

    public Component reason() {
        var comp = reasonSupplier.get();
        if (comp == null) return Component.empty();
        return comp;
    }
}
