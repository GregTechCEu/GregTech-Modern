package com.gregtechceu.gtceu.integration.ae2.util;

import appeng.api.stacks.GenericStack;
import lombok.val;

/**
 * @Author GlodBlock
 * @Description A slot that can be set to keep requesting.
 * @Date 2023/4/21-0:34
 */
public interface IConfigurableSlot {

    GenericStack getConfig();

    GenericStack getStock();

    void setConfig(GenericStack val);

    void setStock(GenericStack val);

    IConfigurableSlot copy();

}