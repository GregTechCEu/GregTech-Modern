package com.gregtechceu.gtceu.core.util.extensions;

public interface RenderTargetExt {

    default void gtceu$setShouldClearDepth(boolean shouldClearDepth) {
        throw new AssertionError("Mixin didn't apply");
    }

    default void gtceu$changeShouldClearDepth() {
        throw new AssertionError("Mixin didn't apply");
    }
}
