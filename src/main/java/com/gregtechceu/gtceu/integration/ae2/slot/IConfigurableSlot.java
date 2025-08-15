package com.gregtechceu.gtceu.integration.ae2.slot;

import appeng.api.stacks.GenericStack;

/**
 * A slot that can be set to keep requesting.
 */
public interface IConfigurableSlot {

    GenericStack getConfig();

    GenericStack getStock();

    void setConfig(GenericStack val);

    void setStock(GenericStack val);

    IConfigurableSlot copy();
}
