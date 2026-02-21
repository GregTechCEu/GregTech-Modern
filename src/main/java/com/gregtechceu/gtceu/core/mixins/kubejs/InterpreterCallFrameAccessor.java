package com.gregtechceu.gtceu.core.mixins.kubejs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "dev.latvian.mods.rhino.Interpreter$CallFrame", remap = false)
public interface InterpreterCallFrameAccessor {

    @Accessor("stack")
    Object[] gtceu$getStack();

    @Accessor("savedStackTop")
    int gtceu$getSavedStackTop();
}
