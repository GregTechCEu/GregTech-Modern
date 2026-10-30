package com.gregtechceu.gtceu.integration.ae2.slot;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

/**
 * A slot that can be set to keep requesting.
 */
public interface IConfigurableSlot {

    @Nullable GenericStack getConfig();

    @Nullable GenericStack getStock();

    void setConfig(@Nullable GenericStack val);

    void setStock(@Nullable GenericStack val);

    IConfigurableSlot copy();
}
